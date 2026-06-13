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

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dateColumn.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        resultColumn.setCellValueFactory(cellData -> cellData.getValue().resultProperty());
        wordColumn.setCellValueFactory(cellData -> cellData.getValue().wordProperty());
        
        historyTable.setItems(historyData);
    }    

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
    
    private void requestHistory() {
        try {
            ServerConnection.getInstance().sendMessage(MessageProtocol.build(MessageProtocol.REQ_HISTORY));
        } catch (IOException e) {
            errorLabel.setText("Errore di connessione.");
        }
    }

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
