package guesstheword_server.network;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Registro thread-safe (singleton) dei ClientHandler attivi connessi al Server.
 * Consente la gestione centralizzata delle connessioni e delle notifiche di spegnimento.
 *
 * @author Sabrina Soriano
 */
public class ClientRegistry {

    private static ClientRegistry instance;
    private final List<ClientHandler> activeClients = new CopyOnWriteArrayList<>();
    
    //Mappa username -> handler autenticato, per garantire al massimo un handler per username
    private final ConcurrentHashMap<String, ClientHandler> loggedUsers = new ConcurrentHashMap<>();

    private ClientRegistry() {}

    /**
     * Restituisce l'istanza singleton di ClientRegistry.
     *
     * @return l'istanza unica del registro
     */
    public static synchronized ClientRegistry getInstance() {
        if (instance == null) {
            instance = new ClientRegistry();
        }
        return instance;
    }

    /**
     * Registra un nuovo ClientHandler nel registro.
     *
     * @param client il ClientHandler da aggiungere
     */
    public void register(ClientHandler client) {
        if (client != null && !activeClients.contains(client)) {
            activeClients.add(client);
            System.out.println("[ClientRegistry] Client registrato. Totale connessi: " + activeClients.size());
        }
    }

    /**
     * Rimuove un ClientHandler dal registro.
     *
     * @param client il ClientHandler da rimuovere
     */
    public void unregister(ClientHandler client) {
        if (client != null) {
            activeClients.remove(client);
            System.out.println("[ClientRegistry] Client rimosso. Totale connessi: " + activeClients.size());
        }
    }
    
    /**
    * Prova a registrare in modo atomico un utente come autenticato.
    * Se l'username è già associato a un altro handler attivo, l'operazione fallisce
    * senza sovrascrivere la sessione esistente.
    *
    * @param username lo username da autenticare
     * @param handler  l'handler che richiede l'autenticazione
    * @return true se la registrazione è riuscita, false se l'utente è già loggato
    */
    public boolean tryBindAuthenticatedUser(String username, ClientHandler handler) {
        ClientHandler existing = loggedUsers.putIfAbsent(username, handler);
        return existing == null;
    }

    /**
    * Deregistra un utente autenticato solo se l'handler passato è il proprietario
    * corrente della sessione, evitando che un handler obsoleto cancelli per errore
    * la sessione di un login più recente dello stesso username.
    *
    * @param username lo username da deregistrare
    * @param handler  l'handler che richiede la deregistrazione
    */
    public void unbindAuthenticatedUser(String username, ClientHandler handler) {
        if (username != null) {
            loggedUsers.remove(username, handler);
        }
    }

    /**
     * Invia un messaggio a tutti i client registrati.
     *
     * @param message il messaggio da inviare
     */
    public void broadcast(String message) {
        for (ClientHandler client : activeClients) {
            client.sendMessage(message);
        }
    }

    /**
     * Chiude ordinatamente tutte le connessioni dei client attivi, inviando prima
     * un messaggio di notifica di spegnimento del server.
     */
    public synchronized void closeAllConnections() {
        System.out.println("[ClientRegistry] Chiusura di tutte le connessioni in corso (totale: " + activeClients.size() + ")...");
        for (ClientHandler client : activeClients) {
            try {
                // Invia la notifica di shutdown controllato
                client.sendMessage(guesstheword_server.protocol.MessageProtocol.build(
                    guesstheword_server.protocol.MessageProtocol.SERVER_SHUTDOWN
                ));
                // Chiude la socket
                if (client.getSocket() != null && !client.getSocket().isClosed()) {
                    client.getSocket().close();
                }
            } catch (IOException e) {
                System.err.println("[ClientRegistry] Errore nella chiusura della socket di un client: " + e.getMessage());
            }
        }
        activeClients.clear();
        loggedUsers.clear();
    }
}
