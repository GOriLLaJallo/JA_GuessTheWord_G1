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
 * Controller per la Waiting Room
 */
public class WaitingRoomViewController implements Initializable {

    @FXML
    private Label waitingLabel;

    private ServerConnection serverConn;
    private Timeline dotAnimation;
    private int dotCount = 0;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Animazione dei tre pallini
        dotAnimation = new Timeline(new KeyFrame(Duration.millis(500), e -> {
            dotCount = (dotCount + 1) % 4;
            String dots = "";
            for (int i = 0; i < dotCount; i++) {
                dots += ".";
            }
            waitingLabel.setText("In attesa di un avversario" + dots);
        }));
        dotAnimation.setCycleCount(Timeline.INDEFINITE);
        dotAnimation.play();

        // Inizializza rete usando il Singleton
        try {
            this.serverConn = ServerConnection.getInstance();
            startWaiting();
        } catch (IOException ex) {
            ex.printStackTrace();
            waitingLabel.setText("Errore di connessione al server.");
        }
    }

    private void startWaiting() {
        if (serverConn == null) return;

        // Invia il comando di attesa al server
        String waitingMsg = MessageProtocol.build(MessageProtocol.WAITING);
        serverConn.sendMessage(waitingMsg);

        // Avvia l'ascoltatore asincrono
        ListenerTask listener = new ListenerTask(serverConn);
        
        listener.messageProperty().addListener((obs, oldMsg, newMsg) -> {
            if (newMsg == null) return;
            
            Platform.runLater(() -> {
                String[] parts = MessageProtocol.parse(newMsg);
                String command = parts[0];

                if (command.equals(MessageProtocol.OPPONENT_FOUND)) {
                    // Avversario trovato! Passiamo alla schermata di gioco
                    System.out.println("[Client] Avversario trovato!");
                    goToGameView();
                } else {
                    System.out.println("[Client] Ricevuto in attesa: " + newMsg);
                }
            });
        });

        Thread listenerThread = new Thread(listener);
        listenerThread.setDaemon(true); // Termina se chiudiamo l'app
        listenerThread.start();
    }

    private void goToGameView() {
        try {
            if (dotAnimation != null) dotAnimation.stop();
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/guesstheword_client/resources/view/GameView.fxml"));
            Parent viewParent = loader.load();
            
            // TODO: In futuro, dovremo passare la serverConn anche a GameViewController
            
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
