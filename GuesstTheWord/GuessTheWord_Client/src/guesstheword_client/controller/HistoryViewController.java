/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package guesstheword_client.controller;

import guesstheword_client.model.MatchRecord;
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
import javafx.scene.control.cell.PropertyValueFactory;
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
    private TableView<MatchRecord> historyTable;

    @FXML
    private TableColumn<MatchRecord, String> dateColumn;

    @FXML
    private TableColumn<MatchRecord, String> resultColumn;

    @FXML
    private TableColumn<MatchRecord, String> wordColumn;

    @FXML
    private TableColumn<MatchRecord, String> difficultyColumn;

    @FXML
    private Label errorLabel;

    private ListenerTask listenerTask;
    private ObservableList<MatchRecord> historyData = FXCollections.observableArrayList();
    private guesstheword_client.service.HistoryService historyService;
    private javafx.beans.value.ChangeListener<String> messageListener;

    /**
     * Inizializza il controller e configura le colonne della tabella associandole
     * alle Properties del modello HistoryItem.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        historyService = new guesstheword_client.service.HistoryService();
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("matchDate"));
        wordColumn.setCellValueFactory(new PropertyValueFactory<>("secretWord"));
        resultColumn.setCellValueFactory(new PropertyValueFactory<>("outcome"));
        difficultyColumn.setCellValueFactory(new PropertyValueFactory<>("difficulty"));

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
        messageListener = (obs, oldMsg, newMsg) -> {
            if (newMsg != null) {
                handleServerMessage(newMsg);
            }
        };
        
        // Controlla se il messaggio corrente nella property è già HISTORY_DATA
        String currentMsg = listener.getMessage();
        if (currentMsg != null && currentMsg.startsWith(MessageProtocol.HISTORY_DATA)) {
            handleServerMessage(currentMsg);
        }
        
        this.listenerTask.messageProperty().addListener(messageListener);
        
        // Richiedi lo storico al server
        requestHistory();
    }

    /**
     * Invia al server il comando REQ_HISTORY per chiedere i dati aggiornati dello storico.
     */
    private void requestHistory() {
        try {
            historyService.requestHistory();
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
                // Formato inviato dal server
                // Dati separati da punto e virgola: "Data,Esito,Parola;"
                String allRecords = parts[1];
                if (!allRecords.isEmpty()) {
                    String[] records = allRecords.split(";");
                    for (String record : records) {
                        String[] fields = record.split(",");
                        if (fields.length >= 4) {
                            String date = fields[0];
                            String outcome = fields[1];
                            String word = fields[2];
                            String difficulty = fields[3];
                            // Usiamo MatchRecord per memorizzare il dato internamente,
                            // omettendo avversario e tempo poiché non inviati dal server
                            historyData.add(new MatchRecord(word, null, outcome, null, date, difficulty));
                        } else if (fields.length >= 3) {
                            // Fallback per vecchi record senza difficoltà
                            String date = fields[0];
                            String outcome = fields[1];
                            String word = fields[2];
                            historyData.add(new MatchRecord(word, null, outcome, null, date, "N/D"));
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
        // Rimuove il listener prima di cambiare schermata
        if (listenerTask != null && messageListener != null) {
            listenerTask.messageProperty().removeListener(messageListener);
        }
        
        try {
            guesstheword_client.utils.SceneManager.switchScene(event, "/guesstheword_client/resources/view/DifficultyView.fxml");
        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Errore durante il caricamento della schermata Difficoltà.");
        }
    }
}
