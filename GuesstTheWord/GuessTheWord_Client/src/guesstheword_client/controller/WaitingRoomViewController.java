package guesstheword_client.controller;

import guesstheword_client.network.ListenerTask;
import guesstheword_client.network.MessageProtocol;
import guesstheword_client.network.ServerConnection;
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/guesstheword_client/resources/view/GameView.fxml"));
            Parent viewParent = loader.load();
            
            // Passa il listener al GameViewController
            guesstheword_client.controller.GameViewController gameController = loader.getController();
            gameController.setListener(listenerTask);
            
            Scene scene = new Scene(viewParent);
            Stage window = (Stage) waitingLabel.getScene().getWindow();
            window.setScene(scene);
            window.centerOnScreen();
            window.show();
        } catch (IOException e) {
            e.printStackTrace();
            waitingLabel.setText("Errore caricamento gioco.");
        }
    }
}
