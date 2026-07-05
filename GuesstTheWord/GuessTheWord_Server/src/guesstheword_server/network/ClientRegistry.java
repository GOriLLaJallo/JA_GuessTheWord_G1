package guesstheword_server.network;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Registro thread-safe (singleton) dei ClientHandler attivi connessi al Server.
 * Consente la gestione centralizzata delle connessioni e delle notifiche di spegnimento.
 *
 * @author Carmine Muollo
 */
public class ClientRegistry {

    private static ClientRegistry instance;
    private final List<ClientHandler> activeClients = new CopyOnWriteArrayList<>();

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
    public synchronized void register(ClientHandler client) {
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
    public synchronized void unregister(ClientHandler client) {
        if (client != null) {
            activeClients.remove(client);
            System.out.println("[ClientRegistry] Client rimosso. Totale connessi: " + activeClients.size());
        }
    }

    /**
     * Invia un messaggio a tutti i client registrati.
     *
     * @param message il messaggio da inviare
     */
    public synchronized void broadcast(String message) {
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
    }
}
