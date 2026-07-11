/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package guesstheword_server.protocol;

/**
 * Definisce tutti i comandi scambiati tra Client e Server
 * I messaggi sono stati implementati seguendo delle convenzioni per i protocolli testuali
 * Es:
 * AUTH LOGIN -> SMTP
 * Usando il separatore "\u001F" per evitare di confondersi con la punteggiatura (split("\u001F"))
 * 
 * @author Sabrina Soriano
 */
public class MessageProtocol {
    
    // messaggio di login: AUTH_LOGIN:username:password
    public static final String AUTH_LOGIN = "AUTH_LOGIN";

    // messagio di registrazione: AUTH_REGISTER:username:password
    public static final String AUTH_REGISTER = "AUTH_REGISTER";

    // messaggio di autenticazione riuscita: AUTH_OK:username
    public static final String AUTH_OK = "AUTH_OK";

    //messaggio di autenticazione fallita: AUTH_FAIL:motivo
    public static final String AUTH_FAIL = "AUTH_FAIL";
    
    //messaggio: username già autenticato su un'altra sessione
    public static final String ALREADY_LOGGED_IN = "ALREADY_LOGGED_IN";

    // Richiesta di ingresso in lobby con la difficoltà scelta: WAITING: difficulty (Es. WAITING: MEDIUM)
    public static final String WAITING = "WAITING";

    //messaggio avversario trovato, la partita sta per iniziare
    public static final String OPPONENT_FOUND = "OPPONENT_FOUND";

    //messaggio avversario disconnesso dopo inizio partita
    public static final String OPPONENT_DISCONNECTED = "OPPONENT_DISCONNECTED";

    //messaggi di inizio partita: GAME_START:testoCifrato:durataSecondi
    public static final String GAME_START = "GAME_START";

    //messaggio di risposta del client: GAME_ANSWER:parolaInserita 
    public static final String GAME_ANSWER = "GAME_ANSWER";

    //messaggio di vittoria: GAME_WIN
    public static final String GAME_WIN = "GAME_WIN";

    //messaggio di sconfitta: GAME_LOSE
    public static final String GAME_LOSE = "GAME_LOSE";

    //messaggio di tempo scaduto senza una vittoria: GAME_TIMEOUT
    public static final String GAME_TIMEOUT = "GAME_TIMEOUT";
    
    //messaggio inviato dal client a login avvenuto: REQ_HISTORY
    public static final String REQ_HISTORY = "REQ_HISTORY";
    
    //messaggio inviato dal server con lo storico formattato: HISTORY_DATA
    public static final String HISTORY_DATA = "HISTORY_DATA";

    // messaggio di spegnimento controllato del server
    public static final String SERVER_SHUTDOWN = "SERVER_SHUTDOWN";

    /**
     * Costruisco un messaggio
     *
     * @param command   costante del comando
     * @param params    String... params è un varargs: accetta zero o più stringhe come parametri.
     * @return messaggio formattato
     */
    public static String build(String command, String... params) {
        if (params == null || params.length == 0) return command;
        return command + "\u001F" + String.join("\u001F", params);
    }

    /**
     * Divide un messaggio ricevuto nei suoi componenti
     *
     * @param message
     * @return array di componenti
     */
    public static String[] parse(String message) {
        return message.split("\u001F");
    }

    // Classe di utilità, non istanziabile, serve solo a impedire l'istanza di un oggetto MessageProtocol
    private MessageProtocol() {
        throw new UnsupportedOperationException("Utility class");
    }
}
