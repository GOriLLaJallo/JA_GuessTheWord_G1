package guesstheword_client.service;

import guesstheword_client.network.MessageProtocol;
import guesstheword_client.network.ServerConnection;
import guesstheword_client.utils.HashUtil;
import java.io.IOException;

/**
 * Servizio responsabile dell'autenticazione lato client.
 * Si occupa di cifrare la password in SHA-256 e di formattare e inviare i messaggi 
 * di rete per il Login e la Registrazione.
 * 
 * @author William Menza
 */
public class AuthService {
    
    /**
     * Invia la richiesta di login al server e attende la risposta.
     * @param username L'username inserito
     * @param password La password in chiaro inserita
     * @return La risposta del server
     * @throws IOException In caso di problemi di rete
     */
    public String login(String username, String password) throws IOException {
        String hashedPassword = HashUtil.sha256(password);
        String msg = MessageProtocol.build(MessageProtocol.AUTH_LOGIN, username, hashedPassword);
        ServerConnection.getInstance().sendMessage(msg);
        return ServerConnection.getInstance().receiveMessage();
    }
    
    /**
     * Invia la richiesta di registrazione al server e attende la risposta.
     * @param username L'username scelto
     * @param password La password in chiaro scelta
     * @return La risposta del server
     * @throws IOException In caso di problemi di rete
     */
    public String register(String username, String password) throws IOException {
        String hashedPassword = HashUtil.sha256(password);
        String msg = MessageProtocol.build(MessageProtocol.AUTH_REGISTER, username, hashedPassword);
        ServerConnection.getInstance().sendMessage(msg);
        return ServerConnection.getInstance().receiveMessage();
    }
}
