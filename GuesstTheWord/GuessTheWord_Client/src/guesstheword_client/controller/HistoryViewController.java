/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package guesstheword_client.controller;

import guesstheword_client.model.HistoryItem;
import guesstheword_client.network.ListenerTask;
import guesstheword_client.network.MessageProtocol;
import guesstheword_client.network.ServerConnection;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

/**
 * Controller per la schermata dello Storico Partite (HistoryView.fxml).
 * Gestisce la visualizzazione delle partite precedenti dell'utente all'interno
 * di una TableView, richiedendo i dati al server e parsando la risposta.
 * 
 * @author William Menza
 */
public class HistoryViewController implements Initializable {

    @FXML
    private TableView<HistoryItem> historyTable;
    @FXML
    private TableColumn<HistoryItem, String> dateColumn;
    @FXML
    private TableColumn<HistoryItem, String> resultColumn;
    @FXML
    private TableColumn<HistoryItem, String> wordColumn;
    @FXML
    private Label errorLabel;

    private ListenerTask listenerTask;
    private ObservableList<HistoryItem> historyData = FXCollections.observableArrayList();

    /**
     * Inizializza il controller e configura le colonne della tabella associandole
     * alle Properties del modello HistoryItem.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dateColumn.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        resultColumn.setCellValueFactory(cellData -> cellData.getValue().resultProperty());
        wordColumn.setCellValueFactory(cellData -> cellData.getValue().wordProperty());
        
        historyTable.setItems(historyData);
    }    

    /**
     * Aggancia il listener di rete a questo controller per catturare la risposta
     * asincrona del server contenente i dati dello storico.
     * Scatena automaticamente la richiesta dello storico.
     * 
     * @param listener il task di ascolto rete attivo
     */
    public void setListener(ListenerTask listener) {
        this.listenerTask = listener;
        this.listenerTask.messageProperty().addListener((obs, oldMsg, newMsg) -> {
            if (newMsg != null) {
                Platform.runLater(() -> handleServerMessage(newMsg));
            }
        });
        
        // Richiedi lo storico al server
        requestHistory();
    }
    
    /**
     * Invia al server il comando REQ_HISTORY per chiedere i dati aggiornati dello storico.
     */
    private void requestHistory() {
        try {
            ServerConnection.getInstance().sendMessage(MessageProtocol.build(MessageProtocol.REQ_HISTORY));
        } catch (IOException e) {
            errorLabel.setText("Errore di connessione.");
        }
    }

    /**
     * Elabora il messaggio in arrivo dal server, facendo parsing della stringa 
     * con i dati storici (formato: data,esito,parola;) e popolando la TableView.
     * 
     * @param message il messaggio formattato in arrivo dal server
     */
    private void handleServerMessage(String message) {
        String[] parts = MessageProtocol.parse(message);
        String command = parts[0];

        if (command.equals(MessageProtocol.HISTORY_DATA)) {
            historyData.clear();
            if (parts.length > 1) {
                String dataString = parts[1];
                if (dataString.equals("Nessuna partita giocata.")) {
                    errorLabel.setText(dataString);
                    errorLabel.setTextFill(javafx.scene.paint.Color.web("#a3a3a3"));
                } else {
                    // Formato: data1,esito1,parola1;data2,esito2,parola2;
                    String[] games = dataString.split(";");
                    for (String game : games) {
                        if (!game.trim().isEmpty()) {
                            String[] gameData = game.split(",");
                            if (gameData.length == 3) {
                                historyData.add(new HistoryItem(gameData[0], gameData[1], gameData[2]));
                            }
                        }
                    }
                }
            }
        } else if (command.equals(MessageProtocol.AUTH_FAIL)) {
            if (parts.length > 1) {
                errorLabel.setText(parts[1]);
            }
        }
    }

    /**
     * Gestisce la navigazione indietro alla schermata di Selezione Difficoltà.
     * 
     * @param event l'evento generato dalla pressione del bottone "Indietro"
     */
    @FXML
    private void handleBackToLobby(ActionEvent event) {
        try {
            // Torniamo alla selezione difficoltà invece che direttamente in attesa,
            // così l'utente può scegliere di nuovo il livello.
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/guesstheword_client/resources/view/DifficultyView.fxml"));
            Parent viewParent = loader.load();
            Scene scene = new Scene(viewParent);
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setScene(scene);
            window.show();
        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Errore durante il caricamento della schermata Difficoltà.");
        }
    }
}
