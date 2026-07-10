package guesstheword_client.controller;

import guesstheword_client.model.GameState;
import guesstheword_client.network.ListenerTask;
import guesstheword_client.network.MessageProtocol;
import guesstheword_client.network.ServerConnection;
import guesstheword_client.network.ClientNetworkEvent;
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
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

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
    private TextFlow encryptedWordLabel;
    
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
    private static final String NORMAL_STYLE = "-fx-font-family: 'Monospaced'; -fx-font-size: 18; -fx-fill: #6747cd;";
    private static final String BOLD_STYLE = "-fx-font-family: 'Monospaced'; -fx-font-size: 18; -fx-fill: #6747cd; -fx-font-weight: bold;";

    /**
     * Inizializzazione base del controller. Chiamata automaticamente da JavaFX.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        gameState = new GameState();
        gameService = new guesstheword_client.service.GameService();
        
        // Permetti l'inserimento di lettere (inclusi i caratteri accentati italiani) e spazi nel campo di risposta
        answerField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[a-zA-ZàèéìòùÀÈÉÌÒÙ\\s]*")) {
                answerField.setText(newValue.replaceAll("[^a-zA-ZàèéìòùÀÈÉÌÒÙ\\s]", ""));
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
        this.listenerTask.networkEventProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                handleNetworkEvent(newVal);
            }
        });
    }
    
    
    /**
     * Popola il TextFlow spezzando il testo sui delimitatori "**" e applicando
    * lo stile grassetto solo alla porzione centrale (la parola cifrata).
    * 
    * @param text
    */
    private void setEncryptedText(String text) {
        encryptedWordLabel.getChildren().clear();
        if (text == null) return;

        int boldStart = text.indexOf("**");
        int boldEnd = boldStart != -1 ? text.indexOf("**", boldStart + 2) : -1;

        if (boldStart != -1 && boldEnd != -1) {
            Text before = new Text(text.substring(0, boldStart));
            before.setStyle(NORMAL_STYLE);

            Text bold = new Text(text.substring(boldStart + 2, boldEnd));
            bold.setStyle(BOLD_STYLE);

            Text after = new Text(text.substring(boldEnd + 2));
            after.setStyle(NORMAL_STYLE);

            encryptedWordLabel.getChildren().addAll(before, bold, after);
        } else {
            Text plain = new Text(text);
            plain.setStyle(NORMAL_STYLE);
            encryptedWordLabel.getChildren().add(plain);
        }
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
            // Imposta timeout dinamico di 90 secondi (60s + 30s)
            try {
                guesstheword_client.network.ServerConnection.getInstance().setSoTimeout(90000);
            } catch (IOException e) {
                System.err.println("[GameViewController] Errore nell'impostare il timeout sulla socket: " + e.getMessage());
            }

            // GAME_START:testoCifrato:shiftCesare:durataSecondi
            String encryptedWord = "???";
            if (parts.length >= 4) {
                // Ricostruisce il testo cifrato (che può contenere il delimitatore)
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i < parts.length - 2; i++) {
                    sb.append(parts[i]);
                    if (i < parts.length - 3) sb.append("\u001F");
                }
                encryptedWord = sb.toString();
        
                try {
                    int shift = Integer.parseInt(parts[parts.length - 2]);
                    gameState.setCaesarShift(shift);
                } catch (NumberFormatException e) {
                    gameState.setCaesarShift(0);
                }
            } else {
                encryptedWord = parts.length > 1 ? parts[1] : "???";
                gameState.setCaesarShift(0);
            }
    
            // Il timer parte sempre da 60 secondi, indipendentemente da quanto manda il server
            secondsRemaining = 60;
    
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

    private boolean alertGiaMostrato = false;

    private void handleNetworkEvent(ClientNetworkEvent event) {
        if (event == ClientNetworkEvent.SERVER_SHUTDOWN) {
            gameState.setStatus("SERVER_SHUTDOWN");
            stopGame("Il server è stato arrestato dall'amministratore", "#ff3b30", "");
            javafx.application.Platform.runLater(() -> showErrorAndExit("Il server è stato arrestato dall'amministratore."));
        } else if (event == ClientNetworkEvent.TIMEOUT || event == ClientNetworkEvent.CONNECTION_LOST) {
            gameState.setStatus("CONNECTION_LOST");
            stopGame("Connessione al server persa, riprova più tardi", "#ff3b30", "");
            javafx.application.Platform.runLater(() -> showErrorAndExit("Connessione al server persa, riprova più tardi."));
        }
    }

    private void showErrorAndExit(String message) {
        if (alertGiaMostrato) return;
        alertGiaMostrato = true;

        try {
            ServerConnection.getInstance().close();
        } catch (IOException e) {
            System.err.println("[GameView] Errore nella chiusura della socket: " + e.getMessage());
        }

        try {
            Stage window = (Stage) timerLabel.getScene().getWindow();
            LoginViewController loginController = guesstheword_client.utils.SceneManager.switchScene(window, "/guesstheword_client/resources/view/LoginView.fxml");
            loginController.setErrorText("Attenzione. Server disconnesso al momento, attendere il ripristino da parte dell'amministratore.");
        } catch (Exception e) {
            System.err.println("[GameView] Errore nel ritorno alla schermata di Login: " + e.getMessage());
        }
    }
    
    /**
     * Aggiorna le etichette dell'interfaccia basandosi su GameState
     */
    private void updateUIFromState() {
        setEncryptedText(gameState.getWordPattern());
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
        // Ripristina timeout a 0
        try {
            guesstheword_client.network.ServerConnection.getInstance().setSoTimeout(0);
        } catch (IOException e) {
            System.err.println("[GameViewController] Errore nel ripristinare il timeout della socket a 0: " + e.getMessage());
        }

        if (countdownTimeline != null) countdownTimeline.stop();
        answerField.setDisable(true);
        guessButton.setDisable(true);
        statusLabel.setText(message);
        statusLabel.setTextFill(javafx.scene.paint.Color.web(colorHex));
        
        //Mostra ai giocatori la parola in chiaro a fine partita
        if (clearWord != null && !clearWord.isEmpty()) {
            encryptedWordLabel.getChildren().clear();
            Text clear = new Text(clearWord);
            clear.setStyle("-fx-fill: #34c759; -fx-font-family: 'Monospaced'; -fx-font-size: 18;");
            encryptedWordLabel.getChildren().add(clear);
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
