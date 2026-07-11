package guesstheword_client.service;

import guesstheword_client.protocol.MessageProtocol;
import guesstheword_client.network.ServerConnection;
import java.io.IOException;

/**
 * Servizio responsabile della gestione del gioco lato client.
 * Si occupa di inviare al server le richieste relative alle partite
 * (es. unione alla sala d'attesa e invio dei tentativi).
 * 
 * @author William Menza
 */
public class GameService {

    /**
     * Invia la richiesta al server per entrare in coda di matchmaking con
     * la difficoltà specificata.
     * 
     * @param difficulty La difficoltà scelta ("Facile", "Media", "Difficile")
     * @throws IOException In caso di problemi di rete
     */
    public void joinWaitingRoom(String difficulty) throws IOException {
        String msg = MessageProtocol.build(MessageProtocol.WAITING, difficulty);
        ServerConnection.getInstance().sendMessage(msg);
    }

    /**
     * Invia il tentativo di indovinare la parola al server.
     * 
     * @param guess La parola inserita dal giocatore
     * @throws IOException In caso di problemi di rete
     */
    public void sendGuess(String guess) throws IOException {
        String msg = MessageProtocol.build(MessageProtocol.GAME_ANSWER, guess);
        ServerConnection.getInstance().sendMessage(msg);
    }
}
