/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * Controller per la schermata di login del client (LoginView.fxml).
 */
public class LoginViewController implements Initializable {

    @FXML
    private Label titleLabel;

    @FXML
    private Label subtitleLabel;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField showPasswordField;

    @FXML
    private ToggleButton togglePasswordButton;

    @FXML
    private Label errorLabel;

    @FXML
    private Button loginButton;

    @FXML
    private Button registerButton;

    private boolean isLoginMode = true;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        errorLabel.setText(""); // Resetta eventuali messaggi di errore iniziali

        // Caricamento programmatico del font FontAwesome per l'icona dell'occhio
        try {
            Font.loadFont(getClass().getResourceAsStream("/guesstheword_client/resources/styles/fontawesome-webfont.ttf"), 16);
        } catch (Exception e) {
            System.err.println("[Login Client] Errore nel caricamento del font FontAwesome: " + e.getMessage());
        }

        // Imposta l'icona dell'occhio aperto (Mostra password) come iniziale
        togglePasswordButton.setText("\uf06e");

        // Associa la visibilità ed il layout dei due campi in base alla selezione del ToggleButton
        showPasswordField.managedProperty().bind(togglePasswordButton.selectedProperty());
        showPasswordField.visibleProperty().bind(togglePasswordButton.selectedProperty());
        
        passwordField.managedProperty().bind(togglePasswordButton.selectedProperty().not());
        passwordField.visibleProperty().bind(togglePasswordButton.selectedProperty().not());
        
        // Sincronizzazione bidirezionale del testo inserito
        showPasswordField.textProperty().bindBidirectional(passwordField.textProperty());
    }

    @FXML
    private void handleTogglePassword(ActionEvent event) {
        if (togglePasswordButton.isSelected()) {
            togglePasswordButton.setText("\uf070"); // fa-eye-slash
        } else {
            togglePasswordButton.setText("\uf06e"); // fa-eye
        }
    }

    @FXML
    private void handleSwitchMode(ActionEvent event) {
        isLoginMode = !isLoginMode;
        errorLabel.setText(""); // resetta errori quando si cambia modalità
        
        if (isLoginMode) {
            titleLabel.setText("WELCOME, USER");
            subtitleLabel.setText("Accedi o registrati per continuare");
            loginButton.setText("Login");
            registerButton.setText("Nuovo utente? Registrati");
        } else {
            titleLabel.setText("REGISTRAZIONE");
            subtitleLabel.setText("Crea un nuovo account");
            loginButton.setText("Conferma Registrazione");
            registerButton.setText("Hai già un account? Login");
        }
    }

    @FXML
    private void handleAction(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = togglePasswordButton.isSelected() ? showPasswordField.getText() : passwordField.getText();
        
        if (password == null) {
            password = "";
        }

        // Validazione base
        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Inserisci username e/o password!");
            return;
        }

        try {
            // Usa il costruttore originale di ServerConnection
            guesstheword_client.network.ServerConnection serverConn = new guesstheword_client.network.ServerConnection();
            String response;
            String msg;

            if (isLoginMode) {
                System.out.println("[Client Login] Tentativo di login per: " + username);
                String hashedPassword = guesstheword_client.utils.HashUtil.sha256(password);
                msg = guesstheword_client.network.MessageProtocol.build(guesstheword_client.network.MessageProtocol.AUTH_LOGIN, username, hashedPassword);
            } else {
                System.out.println("[Client Register] Tentativo di registrazione per: " + username);
                String hashedPassword = guesstheword_client.utils.HashUtil.sha256(password);
                msg = guesstheword_client.network.MessageProtocol.build(guesstheword_client.network.MessageProtocol.AUTH_REGISTER, username, hashedPassword);
            }

            serverConn.sendMessage(msg);
            response = serverConn.receiveMessage();

            // Analizziamo la risposta del server usando il protocollo
            if (response != null) {
                String[] parts = guesstheword_client.network.MessageProtocol.parse(response);
                String command = parts[0];

                if (command.equals(guesstheword_client.network.MessageProtocol.AUTH_OK)) {
                    System.out.println("[Client] Successo! Benvenuto: " + parts[1]);
                    
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/guesstheword_client/resources/view/WaitingRoomView.fxml"));
                        Parent viewParent = loader.load();
                        
                        // Passa la connessione al controller della Waiting Room
                        guesstheword_client.controller.WaitingRoomViewController waitingController = loader.getController();
                        waitingController.setConnection(serverConn);
                        
                        Scene scene = new Scene(viewParent);
                        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
                        window.setScene(scene);
                        window.centerOnScreen();
                        window.show();
                    } catch (IOException e) {
                        errorLabel.setText("Errore caricamento schermata d'attesa.");
                        e.printStackTrace();
                        serverConn.close();
                    }
                } else if (command.equals(guesstheword_client.network.MessageProtocol.AUTH_FAIL)) {
                    String reason = parts.length > 1 ? parts[1] : "Errore sconosciuto.";
                    errorLabel.setText(reason);
                    serverConn.close(); // Chiudiamo solo se fallisce
                } else {
                    errorLabel.setText("Risposta del server non riconosciuta.");
                    serverConn.close(); // Chiudiamo solo se fallisce
                }
            } else {
                errorLabel.setText("Il server non ha risposto.");
                serverConn.close(); // Chiudiamo solo se fallisce
            }

        } catch (java.net.ConnectException e) {
            errorLabel.setText("Impossibile connettersi al server!");
        } catch (IOException e) {
            errorLabel.setText("Errore di rete durante la comunicazione.");
            e.printStackTrace();
        }
    }
}
