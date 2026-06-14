package guesstheword_client.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 * Controller per la schermata di Selezione della Difficoltà.
 * Permette all'utente di scegliere il livello della sfida (Facile, Medio, Difficile)
 * o di visualizzare lo storico delle partite giocate.
 * 
 * @author William Menza
 */
public class DifficultyViewController implements Initializable {

    @FXML
    private Label errorLabel;

    /**
     * Inizializza il controller.
     * Viene chiamato automaticamente dopo il caricamento del file FXML.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Init
    }

    /**
     * Gestisce la selezione del livello Facile.
     * @param event l'evento del click sul bottone
     */
    @FXML
    private void handleEasy(ActionEvent event) {
        goToWaitingRoom(event, "EASY");
    }

    /**
     * Gestisce la selezione del livello Medio.
     * @param event l'evento del click sul bottone
     */
    @FXML
    private void handleMedium(ActionEvent event) {
        goToWaitingRoom(event, "MEDIUM");
    }

    /**
     * Gestisce la selezione del livello Difficile.
     * @param event l'evento del click sul bottone
     */
    @FXML
    private void handleHard(ActionEvent event) {
        goToWaitingRoom(event, "HARD");
    }

    /**
     * Gestisce la selezione dello Storico Partite.
     * @param event l'evento del click sul bottone
     */
    @FXML
    private void handleHistory(ActionEvent event) {
        try {
            HistoryViewController historyController = guesstheword_client.utils.SceneManager.switchScene(event, "/guesstheword_client/resources/view/HistoryView.fxml");
            
            // Passa il listener di rete per ricevere i dati
            guesstheword_client.network.ServerConnection conn = guesstheword_client.network.ServerConnection.getInstance();
            conn.startListener();
            historyController.setListener(conn.getListenerTask());
        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Errore caricamento Storico.");
        }
    }

    /**
     * Gestisce il passaggio alla schermata della Waiting Room.
     * Istanzia il controller della Waiting Room e gli passa la difficoltà scelta.
     * 
     * @param event l'evento associato al cambio vista
     * @param difficulty la difficoltà scelta ("EASY", "MEDIUM", "HARD")
     */
    private void goToWaitingRoom(ActionEvent event, String difficulty) {
        try {
            WaitingRoomViewController waitingController = guesstheword_client.utils.SceneManager.switchScene(event, "/guesstheword_client/resources/view/WaitingRoomView.fxml");
            waitingController.setDifficultyAndStart(difficulty);
        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Errore caricamento Lobby.");
        }
    }
}
