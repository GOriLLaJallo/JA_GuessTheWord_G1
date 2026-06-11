package guesstheword_server.game;

import guesstheword_server.db.ChallengeDAO;
import guesstheword_server.db.ResultDAO;
import guesstheword_server.model.Challenge;
import guesstheword_server.model.GameResult;
import guesstheword_server.network.ClientHandler;
import guesstheword_server.protocol.MessageProtocol;
import java.util.Timer;
import java.util.TimerTask;
import guesstheword_server.model.User;

/**
 * Rappresenta lo stato condiviso di una singola partita tra due giocatori connessi.
 *
 * Una GameSession aggrega i riferimenti ai due ClientHandler coinvolti e alla
 * Challenge preparata per quella sessione. Gestisce l'intero ciclo di vita della
 * partita: invio del messaggio di avvio, ricezione e validazione delle risposte,
 * determinazione del vincitore, notifica dell'esito, gestione del timeout e
 * persistenza dei risultati nel database.
 *
 * L'accesso ai metodi di ricezione risposta è sincronizzato per garantire la
 * correttezza in un contesto multi-thread, in cui ogni ClientHandler gira su un
 * thread separato.
 *
 * @author Davide Andrea Odierna
 */
public class GameSession {

    /** Durata massima del conto alla rovescia in secondi. */
    private static final int DEFAULT_TIMER_SECONDS = 60;

    /** Primo giocatore della sessione. */
    private final ClientHandler player1;

    /** Secondo giocatore della sessione. */
    private final ClientHandler player2;

    /** La sfida associata a questa sessione. */
    private final Challenge challenge;

    /** Flag che indica se la partita è già stata conclusa (vinta, persa o timeout). */
    private volatile boolean finished;

    /** Timestamp in millisecondi del momento in cui è stata inviata la GAME_START. */
    private long startTimeMs;

    /** Timer per la gestione del timeout. */
    private Timer timeoutTimer;

    /**
     * Crea una nuova sessione di gioco tra i due client specificati.
     *
     * @param player1   il ClientHandler del primo giocatore
     * @param player2   il ClientHandler del secondo giocatore
     * @param challenge la sfida preparata per questa sessione
     */
    public GameSession(ClientHandler player1, ClientHandler player2, Challenge challenge) {
        this.player1   = player1;
        this.player2   = player2;
        this.challenge = challenge;
        this.finished  = false;
    }

    // --- Avvio sessione ---

    /**
     * Avvia la sessione di gioco.
     * Invia il messaggio GAME_START a entrambi i client, registra il timestamp
     * di inizio e pianifica il timer di timeout.
     */
    public void start() {
        System.out.println("[GameSession] Partita avviata tra "
                + player1.getUsername() + " e " + player2.getUsername());

        String encryptedExcerpt = CaesarCipher.encrypt(
                challenge.getParolaNascosta(), challenge.getShiftCesare());

        String gameStartMsg = MessageProtocol.build(
                MessageProtocol.GAME_START,
                encryptedExcerpt,
                String.valueOf(challenge.getShiftCesare()),
                String.valueOf(DEFAULT_TIMER_SECONDS)
        );

        player1.sendMessage(gameStartMsg);
        player2.sendMessage(gameStartMsg);

        startTimeMs = System.currentTimeMillis();
        scheduleTimeout();
    }

    // --- Gestione risposte e disconnessioni ---

    /**
     * Elabora la risposta di un giocatore durante la partita.
     * Il metodo è sincronizzato: solo il primo thread che propone la risposta corretta
     * può vincere; le risposte successive vengono ignorate poiché finished sarà già true.
     *
     * @param handler il ClientHandler del giocatore che ha risposto
     * @param guess   la parola proposta dal giocatore
     */
    public synchronized void handleAnswer(ClientHandler handler, String guess) {
        if (finished) {
            return;
        }

        long    responseTimeMs = System.currentTimeMillis() - startTimeMs;
        boolean correct        = guess != null
                && guess.trim().equalsIgnoreCase(challenge.getParolaNascosta());

        if (correct) {
            finishWithWinner(handler, responseTimeMs);
        } else {
            handler.sendMessage(MessageProtocol.build(MessageProtocol.AUTH_FAIL, "Risposta errata. Riprova!"));
        }
    }

    /**
     * Gestisce la disconnessione improvvisa di uno dei due giocatori durante la partita.
     * Notifica l'avversario rimasto connesso e termina la sessione.
     *
     * @param disconnectedHandler il ClientHandler del giocatore disconnesso
     */
    public synchronized void handleDisconnection(ClientHandler disconnectedHandler) {
        if (finished) {
            return;
        }

        finished = true;
        cancelTimeout();

        ClientHandler opponent = getOpponent(disconnectedHandler);
        if (opponent != null) {
            opponent.sendMessage(MessageProtocol.build(MessageProtocol.OPPONENT_DISCONNECTED));
        }

        System.out.println("[GameSession] Il giocatore " + disconnectedHandler.getUsername()
                + " si è disconnesso. Sessione terminata.");

        GameManager.getInstance().removeSession(this);
    }

    // --- Metodi privati ---

    /**
     * Chiude la sessione decretando un vincitore.
     * Notifica entrambi i giocatori e persiste i risultati nel database.
     *
     * @param winner         il ClientHandler del giocatore vincente
     * @param responseTimeMs il tempo di risposta in millisecondi
     */
    private void finishWithWinner(ClientHandler winner, long responseTimeMs) {
        finished = true;
        cancelTimeout();

        ClientHandler loser = getOpponent(winner);

        winner.sendMessage(MessageProtocol.build(MessageProtocol.GAME_WIN, String.valueOf(responseTimeMs)));
        if (loser != null) {
            loser.sendMessage(MessageProtocol.build(MessageProtocol.GAME_LOSE, winner.getUsername(), String.valueOf(responseTimeMs)));
        }

        System.out.println("[GameSession] " + winner.getUsername()
                + " ha vinto in " + responseTimeMs + " ms.");

        persistResults(winner, loser, responseTimeMs);
        GameManager.getInstance().removeSession(this);
    }

    /**
     * Gestisce il timeout: nessun giocatore ha risposto entro il tempo limite.
     * Notifica entrambi i giocatori e persiste i risultati come TIMEOUT.
     */
    private synchronized void handleTimeout() {
        if (finished) {
            return;
        }
        finished = true;

        player1.sendMessage(MessageProtocol.build(MessageProtocol.GAME_TIMEOUT));
        player2.sendMessage(MessageProtocol.build(MessageProtocol.GAME_TIMEOUT));

        System.out.println("[GameSession] Timeout! Nessun vincitore.");

        persistTimeoutResults();
        GameManager.getInstance().removeSession(this);
    }

    /**
     * Pianifica il timer di timeout per la sessione corrente.
     */
    private void scheduleTimeout() {
        timeoutTimer = new Timer(true);
        timeoutTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handleTimeout();
            }
        }, DEFAULT_TIMER_SECONDS * 1000L);
    }

    /**
     * Annulla il timer di timeout, se attivo.
     */
    private void cancelTimeout() {
        if (timeoutTimer != null) {
            timeoutTimer.cancel();
            timeoutTimer = null;
        }
    }

    /**
     * Restituisce l'avversario di un dato ClientHandler in questa sessione.
     *
     * @param handler il handler del giocatore corrente
     * @return il handler dell'avversario, oppure null se non identificabile
     */
    private ClientHandler getOpponent(ClientHandler handler) {
        if (handler == player1) return player2;
        if (handler == player2) return player1;
        return null;
    }

    /**
     * Persiste nel database la sfida e i risultati WIN/LOSE di entrambi i giocatori.
     *
     * @param winner         handler del vincitore
     * @param loser          handler del perdente
     * @param responseTimeMs tempo di risposta del vincitore in millisecondi
     */
    private void persistResults(ClientHandler winner, ClientHandler loser, long responseTimeMs) {
        ChallengeDAO challengeDAO = new ChallengeDAO();
        ResultDAO    resultDAO    = new ResultDAO();

        challengeDAO.save(challenge);

        if (winner.getUser() != null) {
            GameResult winResult = new GameResult(
                    winner.getUser(), challenge, "WIN",
                    challenge.getParolaNascosta(), (int) responseTimeMs);
            resultDAO.save(winResult);
        }

        if (loser != null && loser.getUser() != null) {
            GameResult loseResult = new GameResult(
                    loser.getUser(), challenge, "LOSE", null, null);
            resultDAO.save(loseResult);
        }
    }

    /**
     * Persiste nel database i risultati di tipo TIMEOUT per entrambi i giocatori.
     */
    private void persistTimeoutResults() {
        ChallengeDAO challengeDAO = new ChallengeDAO();
        ResultDAO    resultDAO    = new ResultDAO();

        if (challenge.getId() == 0) {
            challengeDAO.save(challenge);
        }

        if (player1.getUser() != null) {
            resultDAO.save(new GameResult(player1.getUser(), challenge, "TIMEOUT", null, null));
        }
        if (player2.getUser() != null) {
            resultDAO.save(new GameResult(player2.getUser(), challenge, "TIMEOUT", null, null));
        }
    }

    // --- Getter ---

    /**
     * Restituisce la sfida associata a questa sessione.
     *
     * @return la Challenge di questa partita
     */
    public Challenge getChallenge() {
        return challenge;
    }

    /**
     * Indica se la sessione è terminata.
     *
     * @return true se la sessione è conclusa
     */
    public boolean isFinished() {
        return finished;
    }

    /**
     * Restituisce il primo giocatore della sessione.
     *
     * @return il ClientHandler del primo giocatore
     */
    public ClientHandler getPlayer1() {
        return player1;
    }

    /**
     * Restituisce il secondo giocatore della sessione.
     *
     * @return il ClientHandler del secondo giocatore
     */
    public ClientHandler getPlayer2() {
        return player2;
    }
}