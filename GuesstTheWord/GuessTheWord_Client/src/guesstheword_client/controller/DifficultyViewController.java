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

public class DifficultyViewController implements Initializable {

    @FXML
    private Label errorLabel;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Init
    }

    @FXML
    private void handleEasy(ActionEvent event) {
        goToWaitingRoom(event, "EASY");
    }

    @FXML
    private void handleMedium(ActionEvent event) {
        goToWaitingRoom(event, "MEDIUM");
    }

    @FXML
    private void handleHard(ActionEvent event) {
        goToWaitingRoom(event, "HARD");
    }

    private void goToWaitingRoom(ActionEvent event, String difficulty) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/guesstheword_client/resources/view/WaitingRoomView.fxml"));
            Parent viewParent = loader.load();
            
            WaitingRoomViewController waitingController = loader.getController();
            waitingController.setDifficultyAndStart(difficulty);
            
            Scene scene = new Scene(viewParent);
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setScene(scene);
            window.show();
        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Errore caricamento Lobby.");
        }
    }
}
