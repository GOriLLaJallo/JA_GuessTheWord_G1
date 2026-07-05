package guesstheword_server.game;

import guesstheword_server.db.ChallengeDAO;
import guesstheword_server.db.ResultDAO;
import guesstheword_server.model.Challenge;
import guesstheword_server.model.GameResult;
import guesstheword_server.network.ClientHandler;
import guesstheword_server.protocol.MessageProtocol;
import java.util.regex.Pattern;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
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
    public static final int DEFAULT_TIMER_SECONDS = 60;

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
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4, runnable -> {
        Thread t = new Thread(runnable);
        t.setDaemon(true);
        return t;
    });
    private ScheduledFuture<?> timeoutFuture;

    /** Tentativi rimasti per il primo giocatore. */
    private int player1Attempts = 3;

    /** Tentativi rimasti per il secondo giocatore. */
    private int player2Attempts = 3;

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
     * 1) Notifica il GameManager che la sessione di gioco è iniziata
     * 2) Fa il pairing dei 2 player
     * 3) Si prende la parola cifrata e l'estratto
     * 4) Sostituisce (replaceAll) la parola in chiaro con quella cifrata
     * 5) Comunica ai giocatori l'inizio del gioco
     * 6) Parte il countdown
     */
    public void start() {
        
        System.out.println("[GameSession] Partita avviata tra "
                + player1.getUsername() + " e " + player2.getUsername() + " Parola nascosta: " + challenge.getParolaNascosta());

        // Imposta il timeout dinamico per entrambi i client (timeout di gioco + 30s)
        try {
            if (player1.getSocket() != null) {
                player1.getSocket().setSoTimeout((DEFAULT_TIMER_SECONDS + 30) * 1000);
            }
            if (player2.getSocket() != null) {
                player2.getSocket().setSoTimeout((DEFAULT_TIMER_SECONDS + 30) * 1000);
            }
        } catch (java.net.SocketException e) {
            System.err.println("[GameSession] Errore nell'impostare il timeout sulla socket per i client: " + e.getMessage());
        }

        String parolaCifrata = CaesarCipher.encrypt(
            challenge.getParolaNascosta(), challenge.getShiftCesare());

        String estratto = challenge.getEstratto();

        // Controllo se l'estratto contiene la parola nascosta
        if (estratto == null || !guesstheword_server.analysis.DocumentAnalyzer.contieneParola(estratto, challenge.getParolaNascosta())) {
            System.err.println("[WARNING] [GameSession] L'estratto e nullo o non contiene la parola nascosta '" + challenge.getParolaNascosta() + "'. Rigenerazione sfida...");
            
            String diffStr = challenge.getDifficolta();
            Difficulty diffVal = Difficulty.MEDIUM;
            if (diffStr != null) {
                try {
                    diffVal = Difficulty.valueOf(diffStr.toUpperCase());
                } catch (IllegalArgumentException e) {
                    System.err.println("[WARNING] [GameSession] Difficolta sconosciuta '" + diffStr + "', fallback su MEDIUM.");
                }
            } else {
                System.err.println("[WARNING] [GameSession] Difficolta nulla, fallback su MEDIUM.");
            }

            Challenge newChallenge = GameManager.getInstance().regenerateChallenge(diffVal);
            
            challenge.setParolaNascosta(newChallenge.getParolaNascosta());
            challenge.setShiftCesare(newChallenge.getShiftCesare());
            challenge.setEstratto(newChallenge.getEstratto());
            challenge.setDataSfida(newChallenge.getDataSfida());
            challenge.setDifficolta(newChallenge.getDifficolta());
            
            parolaCifrata = CaesarCipher.encrypt(challenge.getParolaNascosta(), challenge.getShiftCesare());
            estratto = challenge.getEstratto();
        }

        if (estratto == null) {
            estratto = challenge.getParolaNascosta();
        }

        String testoCifrato = estratto.replaceAll(
            "(?i)" + Pattern.quote(challenge.getParolaNascosta()), "**" + parolaCifrata + "**"); 

        String gameStartMsg = MessageProtocol.build(
                MessageProtocol.GAME_START,
                testoCifrato,
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
            // Decrementa tentativi
            if (handler == player1) {
                player1Attempts--;
            } else if (handler == player2) {
                player2Attempts--;
            }

            // Se entrambi hanno esaurito i tentativi, termina istantaneamente la partita con un TIMEOUT anticipato
            if (player1Attempts <= 0 && player2Attempts <= 0) {
                System.out.println("[GameSession] Entrambi i giocatori hanno esaurito i tentativi. Termine anticipato.");
                handleTimeout();
                return;
            }

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

        resetSocketTimeouts();

        finished = true;
        cancelTimeout();

        ClientHandler opponent = getOpponent(disconnectedHandler);
        if (opponent != null) {
            opponent.sendMessage(MessageProtocol.build(MessageProtocol.OPPONENT_DISCONNECTED, challenge.getParolaNascosta()));
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
        resetSocketTimeouts();
        finished = true;
        cancelTimeout();

        ClientHandler loser = getOpponent(winner);

        // Salva i risultati nel database PRIMA di inviare i messaggi per evitare race condition
        persistResults(winner, loser, responseTimeMs);

        String clearWord = challenge.getParolaNascosta();
        winner.sendMessage(MessageProtocol.build(MessageProtocol.GAME_WIN, String.valueOf(responseTimeMs), clearWord));
        if (loser != null) {
            loser.sendMessage(MessageProtocol.build(MessageProtocol.GAME_LOSE, winner.getUsername(), String.valueOf(responseTimeMs), clearWord));
        }

        System.out.println("[GameSession] " + winner.getUsername()
                + " ha vinto in " + responseTimeMs + " ms.");

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
        resetSocketTimeouts();
        finished = true;

        // Salva i risultati nel database PRIMA di inviare i messaggi per evitare race condition
        persistTimeoutResults();

        String clearWord = challenge.getParolaNascosta();
        player1.sendMessage(MessageProtocol.build(MessageProtocol.GAME_TIMEOUT, clearWord));
        player2.sendMessage(MessageProtocol.build(MessageProtocol.GAME_TIMEOUT, clearWord));

        System.out.println("[GameSession] Timeout! Nessun vincitore.");

        GameManager.getInstance().removeSession(this);
    }

    /**
     * Pianifica il timer di timeout per la sessione corrente.
     */
    private void scheduleTimeout() {
        timeoutFuture = scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                handleTimeout();
            }
        }, DEFAULT_TIMER_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * Annulla il timer di timeout, se attivo.
     */
    private void cancelTimeout() {
        if (timeoutFuture != null) {
            timeoutFuture.cancel(false);
            timeoutFuture = null;
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
        guesstheword_server.service.MatchPersistenceService matchService = 
                new guesstheword_server.service.MatchPersistenceService();

        GameResult winResult = null;
        if (winner.getUser() != null) {
            winResult = new GameResult(
                    winner.getUser(), challenge, "WIN",
                    challenge.getParolaNascosta(), (int) responseTimeMs);
        }

        GameResult loseResult = null;
        if (loser != null && loser.getUser() != null) {
            loseResult = new GameResult(
                    loser.getUser(), challenge, "LOSE", null, null);
        }

        matchService.saveMatch(challenge, winResult, loseResult);
    }

    /**
     * Persiste nel database i risultati di tipo TIMEOUT per entrambi i giocatori.
     */
    private void persistTimeoutResults() {
        guesstheword_server.service.MatchPersistenceService matchService = 
                new guesstheword_server.service.MatchPersistenceService();

        GameResult r1 = null;
        if (player1.getUser() != null) {
            r1 = new GameResult(player1.getUser(), challenge, "TIMEOUT", null, null);
        }

        GameResult r2 = null;
        if (player2.getUser() != null) {
            r2 = new GameResult(player2.getUser(), challenge, "TIMEOUT", null, null);
        }

        matchService.saveMatch(challenge, r1, r2);
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

    private void resetSocketTimeouts() {
        try {
            if (player1 != null && player1.getSocket() != null) {
                player1.getSocket().setSoTimeout(0);
            }
            if (player2 != null && player2.getSocket() != null) {
                player2.getSocket().setSoTimeout(0);
            }
        } catch (java.net.SocketException e) {
            System.err.println("[GameSession] Errore nel ripristinare il timeout sulla socket: " + e.getMessage());
        }
    }

    /**
     * Termina lo scheduler in modo corretto durante lo spegnimento del server.
     */
    public static void shutdownScheduler() {
        if (scheduler != null) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}