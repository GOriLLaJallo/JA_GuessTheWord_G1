package guesstheword_client.network;

import javafx.concurrent.Task;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Task in background per l'ascolto asincrono dei messaggi provenienti dal server.
 * Gestisce l'intercettazione degli errori di rete e dello shutdown, notificando la UI
 * tramite proprietà reattive JavaFX.
 * Utilizza un pattern di polling breve (2s) per prevenire race condition ed evitare blocchi
 * in readLine() al momento della variazione di setSoTimeout.
 *
 * @author Sabrina Soriano
 */
public class ListenerTask extends Task<Void> {

    private final ServerConnection connection;
    private final ObjectProperty<ClientNetworkEvent> networkEventProperty = new SimpleObjectProperty<>();
    private boolean gameActive = false;

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
        try {
            // Imposta timeout fisso e breve lato client per il polling
            connection.setSoTimeout(2000);
            long lastActivityTimestamp = System.currentTimeMillis();

            while (true) {
                String message = null;
                try {
                    message = connection.receiveMessage();
                } catch (java.net.SocketTimeoutException e) {
                    long elapsed = System.currentTimeMillis() - lastActivityTimestamp;
                    // Soglia di 90s (60s + 30s di margine) se la partita è in corso, infinita altrimenti
                    long threshold = gameActive ? 90000L : Long.MAX_VALUE;
                    if (elapsed > threshold) {
                        System.err.println("[ListenerTask] Soglia di inattività superata (" + elapsed + " ms) durante il gioco.");
                        javafx.application.Platform.runLater(() -> networkEventProperty.set(ClientNetworkEvent.TIMEOUT));
                        break;
                    }
                    continue;
                }

                if (message == null) {
                    break; // Connessione chiusa dal server
                }

                // Dati ricevuti con successo, aggiorna il timestamp di ultima attività
                lastActivityTimestamp = System.currentTimeMillis();

                if (message.equals(MessageProtocol.SERVER_SHUTDOWN)) {
                    javafx.application.Platform.runLater(() -> networkEventProperty.set(ClientNetworkEvent.SERVER_SHUTDOWN));
                    break;
                }

                String[] parts = MessageProtocol.parse(message);
                String command = parts[0];
                if (command.equals(MessageProtocol.GAME_START)) {
                    gameActive = true;
                } else if (command.equals(MessageProtocol.GAME_WIN) ||
                           command.equals(MessageProtocol.GAME_LOSE) ||
                           command.equals(MessageProtocol.GAME_TIMEOUT) ||
                           command.equals(MessageProtocol.OPPONENT_DISCONNECTED)) {
                    gameActive = false;
                }

                // Imposta a null e poi al messaggio ricevuto per forzare l'attivazione dei listener
                updateMessage(null);
                updateMessage(message);
            }
        } catch (java.io.IOException e) {
            System.err.println("[ListenerTask] Errore di rete o disconnessione dal server: " + e.getMessage());
            javafx.application.Platform.runLater(() -> networkEventProperty.set(ClientNetworkEvent.CONNECTION_LOST));
        }
        return null;
    }
}