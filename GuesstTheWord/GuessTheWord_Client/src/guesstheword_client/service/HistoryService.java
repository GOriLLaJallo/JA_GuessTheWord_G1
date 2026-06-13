package guesstheword_client.service;

import guesstheword_client.network.MessageProtocol;
import guesstheword_client.network.ServerConnection;
import java.io.IOException;

/**
 * Servizio responsabile della richiesta dei dati storici lato client.
 * 
 * @author William Menza
 */
public class HistoryService {

    /**
     * Invia la richiesta al server per ottenere lo storico delle partite giocate.
     * 
     * @throws IOException In caso di problemi di rete
     */
    public void requestHistory() throws IOException {
        String msg = MessageProtocol.build(MessageProtocol.REQ_HISTORY);
        ServerConnection.getInstance().sendMessage(msg);
    }
}
