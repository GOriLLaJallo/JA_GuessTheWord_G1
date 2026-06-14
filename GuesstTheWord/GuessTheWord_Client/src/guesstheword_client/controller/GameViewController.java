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
import javafx.scene.control.TextArea;
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
    private Label infoLabel;

    @FXML
    private TextArea encryptedWordLabel;
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
    private javafx.beans.value.ChangeListener<String> messageListener;

    /**
     * Inizializzazione base del controller. Chiamata automaticamente da JavaFX.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        gameState = new GameState();
        gameService = new guesstheword_client.service.GameService();
        
        // Permetti l'inserimento di solo testo (lettere e spazi) nel campo di risposta
        answerField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[a-zA-Z\\s]*")) {
                answerField.setText(newValue.replaceAll("[^a-zA-Z\\s]", ""));
            }
        });
    }    
    
    /**
     * Aggancia il listener di rete (creato in precedenza dalla WaitingRoom) a questo controller,
     * permettendo di continuare a ricevere i messaggi del server senza interruzioni.
     * 
     * @param listener il task in background responsabile della ricezione messaggi
     */
    public void setListener(ListenerTask listener) {
        this.listenerTask = listener;
        
        messageListener = (obs, oldMsg, newMsg) -> {
            if (newMsg != null) {
                handleServerMessage(newMsg);
            }
        };
        
        // Verifica se c'è già un messaggio GAME_START memorizzato nella property del task
        String currentMsg = listener.getMessage();
        if (currentMsg != null && currentMsg.startsWith(MessageProtocol.GAME_START)) {
            handleServerMessage(currentMsg);
        }
        
        this.listenerTask.messageProperty().addListener(messageListener);
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
            // GAME_START:testoCifrato:shiftCesare:durataSecondi
            String encryptedWord = "???";
            if (parts.length >= 4) {
                // Ricostruisce il testo cifrato (che può contenere i due punti)
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i < parts.length - 2; i++) {
                    sb.append(parts[i]);
                    if (i < parts.length - 3) sb.append(":");
                }
                encryptedWord = sb.toString();
                
                try {
                    int shift = Integer.parseInt(parts[parts.length - 2]);
                    gameState.setCaesarShift(shift);
                } catch (NumberFormatException e) {
                    gameState.setCaesarShift(0);
                }
                
                try {
                    secondsRemaining = Integer.parseInt(parts[parts.length - 1]);
                } catch (NumberFormatException e) {
                    secondsRemaining = 60;
                }
            } else {
                encryptedWord = parts.length > 1 ? parts[1] : "???";
                gameState.setCaesarShift(0);
                secondsRemaining = 60;
            }
            
            // Inizializza lo stato del gioco con i valori base o ipotizzati
            gameState.setStatus("PLAYING");
            gameState.setWordPattern(encryptedWord);
            gameState.setAttemptsLeft(3);

            updateUIFromState();
            startCountdown();

        } else if (command.equals(MessageProtocol.GAME_WIN)) {
            gameState.setStatus("WON");
            String clearWord = parts.length > 2 ? parts[2] : "";
            stopGame("Hai Vinto!", "#34c759", clearWord); // Verde
        } else if (command.equals(MessageProtocol.GAME_LOSE)) {
            gameState.setStatus("LOST");
            String clearWord = parts.length > 3 ? parts[3] : "";
            stopGame("Hai Perso!", "#ff3b30", clearWord); // Rosso
        } else if (command.equals(MessageProtocol.GAME_TIMEOUT)) {
            gameState.setStatus("TIMEOUT");
            String clearWord = parts.length > 1 ? parts[1] : "";
            stopGame("Tempo Scaduto!", "#ff9500", clearWord); // Arancione
        } else if (command.equals(MessageProtocol.OPPONENT_DISCONNECTED)) {
            gameState.setStatus("DISCONNECTED");
            String clearWord = parts.length > 1 ? parts[1] : "";
            stopGame("Avversario Disconnesso!", "#a3a3a3", clearWord); // Grigio
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
        
        if (gameState.getAttemptsLeft() > 0) {
            infoLabel.setText("Sii il più veloce a indovinare!");
            infoLabel.setTextFill(javafx.scene.paint.Color.web("#6747cd")); // Viola
            answerField.setDisable(false);
            guessButton.setDisable(false);
        } else {
            infoLabel.setText("Tentativi esauriti, aspetta l'esito della partita");
            infoLabel.setTextFill(javafx.scene.paint.Color.web("#ff9500")); // Arancione
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
     * @param clearWord la parola in chiaro da mostrare a fine partita
     */
    private void stopGame(String message, String colorHex, String clearWord) {
        if (countdownTimeline != null) countdownTimeline.stop();
        answerField.setDisable(true);
        guessButton.setDisable(true);
        statusLabel.setText(message);
        statusLabel.setTextFill(javafx.scene.paint.Color.web(colorHex));
        
        //Mostra ai giocatori la parola in chiaro a fine partita
        if (clearWord != null && !clearWord.isEmpty()) {
            encryptedWordLabel.setText(clearWord);
            encryptedWordLabel.setStyle("-fx-text-fill: #34c759; -fx-font-family: 'Monospaced'; -fx-font-size: 18;");
        }
        
        // Mostra i bottoni di navigazione a fine partita
        postGameBox.setVisible(true);
        postGameBox.setManaged(true);

        // Pre-fetch dello storico per averlo pronto all'istante
        try {
            new guesstheword_client.service.HistoryService().requestHistory();
        } catch (java.io.IOException e) {
            System.err.println("[GameViewController] Errore nel pre-fetch dello storico: " + e.getMessage());
        }
    }

    /**
     * Raccoglie la risposta (guess) digitata dall'utente e la invia al server per la verifica.
     * 
     * @param event l'evento generato dalla pressione del tasto "Indovina"
     */
    @FXML
    private void handleGuess(ActionEvent event) {
        if (gameState.getAttemptsLeft() <= 0) return;
        
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
        // Rimuove il listener per evitare duplicazioni
        if (listenerTask != null && messageListener != null) {
            listenerTask.messageProperty().removeListener(messageListener);
        }
        
        try {
            guesstheword_client.utils.SceneManager.switchScene(event, "/guesstheword_client/resources/view/DifficultyView.fxml");
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
        // Rimuove il listener per evitare duplicazioni
        if (listenerTask != null && messageListener != null) {
            listenerTask.messageProperty().removeListener(messageListener);
        }
        
        try {
            guesstheword_client.controller.HistoryViewController historyController = guesstheword_client.utils.SceneManager.switchScene(event, "/guesstheword_client/resources/view/HistoryView.fxml");
            historyController.setListener(listenerTask);
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Errore caricamento storico.");
        }
    }
}
