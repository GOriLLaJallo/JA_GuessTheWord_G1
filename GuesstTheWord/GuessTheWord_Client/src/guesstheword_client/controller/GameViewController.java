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
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.util.Duration;

/**
 * Controller per la schermata principale di Gioco (GameView.fxml).
 * Gestisce l'interfaccia durante una partita attiva: mostra la parola cifrata,
 * gestisce il countdown (timer), invia le risposte (guess) dell'utente al server
 * ed elabora gli esiti finali (Vittoria, Sconfitta, Timeout, Disconnessione).
 * 
 * @author William Menza
 */
public class GameViewController implements Initializable {

    @FXML
    private Label timerLabel;
    @FXML
    private Label encryptedWordLabel;
    @FXML
    private TextField answerField;
    @FXML
    private Button guessButton;
    @FXML
    private Label statusLabel;

    @FXML
    private javafx.scene.layout.HBox postGameBox;

    private ListenerTask listenerTask;
    private Timeline countdownTimeline;
    private int secondsRemaining = 0;

    /**
     * Inizializzazione base del controller. Chiamata automaticamente da JavaFX.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Inizializzazione base
    }    
    
    /**
     * Aggancia il listener di rete (creato in precedenza dalla WaitingRoom) a questo controller,
     * permettendo di continuare a ricevere i messaggi del server senza interruzioni.
     * 
     * @param listener il task in background responsabile della ricezione messaggi
     */
    public void setListener(ListenerTask listener) {
        this.listenerTask = listener;
        this.listenerTask.messageProperty().addListener((obs, oldMsg, newMsg) -> {
            if (newMsg != null) {
                Platform.runLater(() -> handleServerMessage(newMsg));
            }
        });
    }

    /**
     * Analizza ed elabora un messaggio in arrivo dal server.
     * Risponde dinamicamente ai comandi di protocollo (es. GAME_START, GAME_WIN, AUTH_FAIL).
     * 
     * @param message il messaggio formattato ricevuto dal server
     */
    private void handleServerMessage(String message) {
        String[] parts = MessageProtocol.parse(message);
        String command = parts[0];

        switch (command) {
            case MessageProtocol.GAME_START:
                if (parts.length >= 3) {
                    encryptedWordLabel.setText(parts[1]);
                    secondsRemaining = Integer.parseInt(parts[2]);
                    startCountdown();
                    answerField.setDisable(false);
                    guessButton.setDisable(false);
                    postGameBox.setVisible(false);
                    postGameBox.setManaged(false);
                    statusLabel.setText("Partita iniziata! Indovina la parola.");
                    statusLabel.setTextFill(javafx.scene.paint.Color.web("#a3a3a3"));
                }
                break;
            case MessageProtocol.GAME_WIN:
                stopGame("Complimenti! Hai indovinato per primo!", "#4cd964"); // verde
                break;
            case MessageProtocol.GAME_LOSE:
                stopGame("L'avversario ha indovinato! Hai perso.", "#ff3b30"); // rosso
                break;
            case MessageProtocol.GAME_TIMEOUT:
                stopGame("Tempo scaduto! Partita terminata.", "#ff3b30");
                break;
            case MessageProtocol.OPPONENT_DISCONNECTED:
                stopGame("L'avversario si è disconnesso.", "#ffca28"); // giallo
                break;
            case MessageProtocol.AUTH_FAIL:
                // Il server riutilizza AUTH_FAIL per indicare una risposta errata
                if (parts.length > 1) {
                    statusLabel.setText(parts[1]);
                } else {
                    statusLabel.setText("Risposta errata. Riprova!");
                }
                statusLabel.setTextFill(javafx.scene.paint.Color.web("#ffca28")); // giallo/arancio
                break;
        }
    }

    /**
     * Inizia il conto alla rovescia (countdown) aggiornando visivamente
     * la label del timer ogni secondo.
     */
    private void startCountdown() {
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }
        updateTimerLabel();
        countdownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            secondsRemaining--;
            updateTimerLabel();
            if (secondsRemaining <= 0) {
                countdownTimeline.stop();
            }
        }));
        countdownTimeline.setCycleCount(Timeline.INDEFINITE);
        countdownTimeline.play();
    }
    
    /**
     * Aggiorna la label del timer formattando i secondi residui nel formato MM:SS.
     */
    private void updateTimerLabel() {
        int min = secondsRemaining / 60;
        int sec = secondsRemaining % 60;
        timerLabel.setText(String.format("%02d:%02d", min, sec));
    }

    /**
     * Blocca l'interfaccia di gioco al termine di una partita.
     * Disabilita input e bottoni di gioco, mostra il messaggio di esito con
     * il colore appropriato e rende visibile la pulsantiera di navigazione post-partita.
     * 
     * @param message  il testo del messaggio finale (es. "Hai vinto!")
     * @param colorHex il colore esadecimale da applicare al testo
     */
    private void stopGame(String message, String colorHex) {
        if (countdownTimeline != null) countdownTimeline.stop();
        answerField.setDisable(true);
        guessButton.setDisable(true);
        statusLabel.setText(message);
        statusLabel.setTextFill(javafx.scene.paint.Color.web(colorHex));
        
        // Mostra i bottoni di navigazione a fine partita
        postGameBox.setVisible(true);
        postGameBox.setManaged(true);
    }

    /**
     * Raccoglie la risposta (guess) digitata dall'utente e la invia al server per la verifica.
     * 
     * @param event l'evento generato dalla pressione del tasto "Indovina"
     */
    @FXML
    private void handleGuess(ActionEvent event) {
        String guess = answerField.getText().trim();
        if (!guess.isEmpty()) {
            try {
                String msg = MessageProtocol.build(MessageProtocol.GAME_ANSWER, guess);
                ServerConnection.getInstance().sendMessage(msg);
                answerField.clear();
                statusLabel.setText("Risposta inviata... attesa verdetto.");
                statusLabel.setTextFill(javafx.scene.paint.Color.web("#a3a3a3"));
            } catch (IOException e) {
                e.printStackTrace();
                statusLabel.setText("Errore di rete nell'invio.");
                statusLabel.setTextFill(javafx.scene.paint.Color.web("#ff3b30"));
            }
        }
    }

    /**
     * Gestisce la volontà di giocare una nuova partita.
     * Reindirizza l'utente alla schermata di Selezione Difficoltà.
     * 
     * @param event l'evento generato dalla pressione del tasto "Gioca Ancora"
     */
    @FXML
    private void handlePlayAgain(ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/guesstheword_client/resources/view/DifficultyView.fxml"));
            javafx.scene.Parent viewParent = loader.load();
            
            // Ritornando alla selezione difficoltà, non serve passare il listener (in quanto WaitingRoom lo ricrea se necessario, ma dovremmo stare attenti al socket.
            // Poichè il progetto è semplice, il GC distruggerà il vecchio e il socket è condiviso.
            
            javafx.scene.Scene scene = new javafx.scene.Scene(viewParent);
            javafx.stage.Stage window = (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            window.setScene(scene);
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Errore ritorno alla lobby.");
        }
    }

    /**
     * Gestisce il passaggio alla schermata dello Storico.
     * 
     * @param event l'evento generato dalla pressione del tasto "Storico"
     */
    @FXML
    private void handleHistory(ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/guesstheword_client/resources/view/HistoryView.fxml"));
            javafx.scene.Parent viewParent = loader.load();
            
            guesstheword_client.controller.HistoryViewController historyController = loader.getController();
            historyController.setListener(listenerTask);
            
            javafx.scene.Scene scene = new javafx.scene.Scene(viewParent);
            javafx.stage.Stage window = (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            window.setScene(scene);
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Errore caricamento storico.");
        }
    }
}
