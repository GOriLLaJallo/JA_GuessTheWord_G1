package guesstheword_client.controller;

import guesstheword_client.network.ListenerTask;
import guesstheword_client.network.MessageProtocol;
import guesstheword_client.network.ServerConnection;
import guesstheword_client.network.ClientNetworkEvent;
import javafx.scene.control.Alert;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Controller per la schermata di Waiting Room (Attesa Avversario).
 * Gestisce l'animazione di caricamento e la ricezione asincrona
 * dell'evento di inizio partita dal server.
 * 
 * @author William Menza
 */
public class WaitingRoomViewController implements Initializable {

    @FXML
    private Label waitingLabel;

    private ServerConnection serverConn;
    private ListenerTask listenerTask;
    private guesstheword_client.service.GameService gameService;
    private javafx.beans.value.ChangeListener<String> messageListener;

    /**
     * Inizializza il controller.
     * Tenta di agganciarsi all'istanza Singleton della ServerConnection.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        gameService = new guesstheword_client.service.GameService();
        try {
            this.serverConn = ServerConnection.getInstance();
        } catch (IOException ex) {
            ex.printStackTrace();
            waitingLabel.setText("Errore di connessione al server.");
        }
    }

    /**
     * Imposta la difficoltà scelta dall'utente e inizia ad attendere un avversario.
     * Invia il comando WAITING al server e avvia il ListenerTask per ricevere
     * l'evento OPPONENT_FOUND.
     * 
     * @param difficulty il livello di difficoltà ("EASY", "MEDIUM", "HARD")
     */
    public void setDifficultyAndStart(String difficulty) {
        if (serverConn == null) return;

        // 1. Avvia/recupera l'ascoltatore asincrono condiviso PRIMA dell'invio
        try {
            serverConn.startListener();
            listenerTask = serverConn.getListenerTask();
        } catch (Exception e) {
            e.printStackTrace();
            waitingLabel.setText("Errore inizializzazione ascolto.");
            return;
        }
        
        // 2. Registra il listener PRIMA dell'invio per non perdere messaggi istantanei
        messageListener = (obs, oldMsg, newMsg) -> {
            if (newMsg == null) return;
            
            String[] parts = MessageProtocol.parse(newMsg);
            String command = parts[0];

            if (command.equals(MessageProtocol.OPPONENT_FOUND) || command.equals(MessageProtocol.GAME_START)) {
                // Avversario trovato o gioco già iniziato. Passa alla schermata di gioco
                System.out.println("[Client] Avversario trovato o inizio partita rilevato!");
                goToGameView();
            } else {
                System.out.println("[Client] Ricevuto in attesa: " + newMsg);
            }
        };
        listenerTask.messageProperty().addListener(messageListener);
        listenerTask.networkEventProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                handleNetworkEvent(newVal);
            }
        });

        // 3. Invia la richiesta al server dopo che l'ascoltatore è configurato
        try {
            gameService.joinWaitingRoom(difficulty);
        } catch (IOException e) {
            e.printStackTrace();
            waitingLabel.setText("Errore invio richiesta partita.");
            // Rimuove il listener in caso di errore
            listenerTask.messageProperty().removeListener(messageListener);
            return;
        }
    }

    /**
     * Passa alla schermata di Gioco (GameView) non appena il server
     * comunica di aver trovato un avversario.
     * Passa il ListenerTask al nuovo controller.
     */
    private void goToGameView() {
        // Rimuove il listener prima di cambiare schermata
        if (listenerTask != null && messageListener != null) {
            listenerTask.messageProperty().removeListener(messageListener);
        }
        
        try {
            Stage window = (Stage) waitingLabel.getScene().getWindow();
            guesstheword_client.controller.GameViewController gameController = guesstheword_client.utils.SceneManager.switchScene(window, "/guesstheword_client/resources/view/GameView.fxml");
            
            // Passa il listener al GameViewController
            gameController.setListener(listenerTask);
        } catch (IOException e) {
            e.printStackTrace();
            waitingLabel.setText("Errore caricamento gioco.");
        }
    }

    private boolean alertGiaMostrato = false;

    private void handleNetworkEvent(ClientNetworkEvent event) {
        if (event == ClientNetworkEvent.SERVER_SHUTDOWN) {
            javafx.application.Platform.runLater(() -> showErrorAndExit("Il server è stato arrestato dall'amministratore."));
        } else if (event == ClientNetworkEvent.TIMEOUT || event == ClientNetworkEvent.CONNECTION_LOST) {
            javafx.application.Platform.runLater(() -> showErrorAndExit("Connessione al server persa, riprova più tardi."));
        }
    }

    private void showErrorAndExit(String message) {
        if (alertGiaMostrato) return;
        alertGiaMostrato = true;
        
        // Visualizza il messaggio di errore nella label della waiting room
        waitingLabel.setText(message);
        
        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(2));
        pause.setOnFinished(event -> {
            try {
                ServerConnection.getInstance().close();
            } catch (IOException e) {
                System.err.println("[WaitingRoomView] Errore nella chiusura della socket: " + e.getMessage());
            }
            
            try {
                Stage window = (Stage) waitingLabel.getScene().getWindow();
                LoginViewController loginController = guesstheword_client.utils.SceneManager.switchScene(window, "/guesstheword_client/resources/view/LoginView.fxml");
                loginController.setErrorText("Attenzione. Server disconnesso al momento, attendere il ripristino da parte dell'amministratore.");
            } catch (Exception e) {
                System.err.println("[WaitingRoomView] Errore nel ritorno alla schermata di Login: " + e.getMessage());
            }
        });
        pause.play();
    }
}
