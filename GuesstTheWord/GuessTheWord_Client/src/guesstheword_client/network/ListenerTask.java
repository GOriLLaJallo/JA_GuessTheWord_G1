package guesstheword_client.network;

import guesstheword_client.protocol.MessageProtocol;
import javafx.concurrent.Task;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Task in background per l'ascolto asincrono dei messaggi provenienti dal server.
 * Gestisce l'intercettazione degli errori di rete e dello shutdown, notificando la UI
 * tramite proprietà reattive JavaFX.
 *
 * @author Sabrina Soriano
 */
public class ListenerTask extends Task<Void> {

    private final ServerConnection connection;
    private final ObjectProperty<ClientNetworkEvent> networkEventProperty = new SimpleObjectProperty<>();
    private volatile boolean disconnected = false;

    /**
     * Il costruttore riceve il riferimento alla ServerConnection aperta.
     * 
     * @param connection la connessione aperta con il server
     */
    public ListenerTask(ServerConnection connection) {
        this.connection = connection;
    }

    /**
     * Restituisce la proprietà per ascoltare gli eventi di rete locali (TIMEOUT, disconnessione, spegnimento).
     *
     * @return la proprietà dell'evento di rete
     */
    public ObjectProperty<ClientNetworkEvent> networkEventProperty() {
        return networkEventProperty;
    }
    
    /**
     * Esegue la lettura continua dei messaggi dal server in modo asincrono.
     * Invia i messaggi alla UI tramite updateMessage() e intercetta le eccezioni di socket
     * impostando networkEventProperty.
     */
    @Override
    protected Void call() throws Exception {
        String message;
        try {
            while (!disconnected && !isCancelled()) {
                message = connection.receiveMessage();
                if (message == null) {
                    handleDisconnection(ClientNetworkEvent.CONNECTION_LOST);
                    break;
                }
                if (message.equals(MessageProtocol.SERVER_SHUTDOWN)) {
                    handleDisconnection(ClientNetworkEvent.SERVER_SHUTDOWN);
                    break;
                }
                // Imposta a null e poi al messaggio ricevuto per forzare l'attivazione dei listener
                updateMessage(null);
                updateMessage(message);
            }
        } catch (java.net.SocketTimeoutException e) {
            System.err.println("[ListenerTask] Timeout di lettura socket superato.");
            handleDisconnection(ClientNetworkEvent.TIMEOUT);
        } catch (java.io.IOException e) {
            System.err.println("[ListenerTask] Errore di rete o disconnessione dal server: " + e.getMessage());
            handleDisconnection(ClientNetworkEvent.CONNECTION_LOST);
        }
        return null;
    }

    private synchronized void handleDisconnection(ClientNetworkEvent event) {
        if (!disconnected) {
            disconnected = true;
            javafx.application.Platform.runLater(() -> networkEventProperty.set(event));
        }
    }
}