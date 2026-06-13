/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package guesstheword_client.network;

/**
 * Definisce tutti i comandi scambiati tra Client e Server
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

    // messaggio client in attesa dell'avversario
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

    public static String build(String command, String... params) {
        if (params == null || params.length == 0) return command;
        return command + ":" + String.join(":", params);
    }

    public static String[] parse(String message) {
        return message.split(":");
    }

    private MessageProtocol() {
        throw new UnsupportedOperationException("Utility class");
    }
}
