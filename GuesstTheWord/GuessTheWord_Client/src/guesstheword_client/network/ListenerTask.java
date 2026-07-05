package guesstheword_client.network;

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
            while ((message = connection.receiveMessage()) != null) {
                if (message.equals(MessageProtocol.SERVER_SHUTDOWN)) {
                    javafx.application.Platform.runLater(() -> networkEventProperty.set(ClientNetworkEvent.SERVER_SHUTDOWN));
                    break;
                }
                // Imposta a null e poi al messaggio ricevuto per forzare l'attivazione dei listener
                updateMessage(null);
                updateMessage(message);
            }
        } catch (java.net.SocketTimeoutException e) {
            System.err.println("[ListenerTask] Timeout di lettura socket superato.");
            javafx.application.Platform.runLater(() -> networkEventProperty.set(ClientNetworkEvent.TIMEOUT));
        } catch (java.io.IOException e) {
            System.err.println("[ListenerTask] Errore di rete o disconnessione dal server: " + e.getMessage());
            javafx.application.Platform.runLater(() -> networkEventProperty.set(ClientNetworkEvent.CONNECTION_LOST));
        }
        return null;
    }
}