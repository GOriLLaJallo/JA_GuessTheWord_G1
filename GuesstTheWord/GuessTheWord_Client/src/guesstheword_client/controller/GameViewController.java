package guesstheword_client.controller;

import guesstheword_client.model.GameState;
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
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
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
    private Label attemptsLabel;
    
    @FXML
    private Label turnLabel;

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
    
    private GameState gameState;
    private guesstheword_client.service.GameService gameService;

    /**
     * Inizializzazione base del controller. Chiamata automaticamente da JavaFX.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        gameState = new GameState();
        gameService = new guesstheword_client.service.GameService();
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

        if (command.equals(MessageProtocol.GAME_START)) {
            // GAME_START:testoCifrato:durataSecondi
            String encryptedWord = parts.length > 1 ? parts[1] : "???";
            if (parts.length > 2) {
                try {
                    secondsRemaining = Integer.parseInt(parts[2]);
                } catch (NumberFormatException e) {
                    secondsRemaining = 60;
                }
            } else {
                secondsRemaining = 60;
            }
            
            // Inizializza lo stato del gioco con i valori base o ipotizzati
            gameState.setStatus("PLAYING");
            gameState.setWordPattern(encryptedWord);
            gameState.setAttemptsLeft(3);
            gameState.setMyTurn(true); // Assumiamo sia il nostro turno

            updateUIFromState();
            startCountdown();

        } else if (command.equals(MessageProtocol.GAME_WIN)) {
            gameState.setStatus("WON");
            stopGame("Hai Vinto!", "#34c759"); // Verde
        } else if (command.equals(MessageProtocol.GAME_LOSE)) {
            gameState.setStatus("LOST");
            stopGame("Hai Perso!", "#ff3b30"); // Rosso
        } else if (command.equals(MessageProtocol.GAME_TIMEOUT)) {
            gameState.setStatus("TIMEOUT");
            stopGame("Tempo Scaduto!", "#ff9500"); // Arancione
        } else if (command.equals(MessageProtocol.OPPONENT_DISCONNECTED)) {
            gameState.setStatus("DISCONNECTED");
            stopGame("Avversario Disconnesso!", "#a3a3a3"); // Grigio
        } else if (command.equals(MessageProtocol.AUTH_FAIL)) {
            // Il server riutilizza AUTH_FAIL per indicare una risposta errata
            if (parts.length > 1) {
                statusLabel.setText(parts[1]);
            } else {
                statusLabel.setText("Risposta errata. Riprova!");
            }
            statusLabel.setTextFill(javafx.scene.paint.Color.web("#ffca28")); // giallo/arancio
        }
    }
    
    /**
     * Aggiorna le etichette dell'interfaccia basandosi su GameState
     */
    private void updateUIFromState() {
        encryptedWordLabel.setText(gameState.getWordPattern());
        attemptsLabel.setText("Tentativi: " + gameState.getAttemptsLeft());
        
        if (gameState.isMyTurn()) {
            turnLabel.setText("È il tuo turno!");
            turnLabel.setTextFill(javafx.scene.paint.Color.web("#34c759")); // Verde
            answerField.setDisable(false);
            guessButton.setDisable(false);
        } else {
            turnLabel.setText("Turno dell'avversario...");
            turnLabel.setTextFill(javafx.scene.paint.Color.web("#ff9500")); // Arancione
            answerField.setDisable(true);
            guessButton.setDisable(true);
        }
        
        statusLabel.setText("");
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
        if (!gameState.isMyTurn()) return;
        
        String guess = answerField.getText().trim();
        if (guess.isEmpty()) return;

        try {
            // Aggiorna tentativi localmente
            int attempts = gameState.getAttemptsLeft();
            if (attempts > 0) {
                gameState.setAttemptsLeft(attempts - 1);
                updateUIFromState();
            }
            
            // Invia al server tramite GameService
            gameService.sendGuess(guess);
            answerField.clear();
            statusLabel.setText("Risposta inviata, in attesa...");
            
            // Se finito tentativi
            if (gameState.getAttemptsLeft() <= 0) {
                gameState.setMyTurn(false);
                updateUIFromState();
            }
        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("Errore invio risposta.");
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
