/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package guesstheword_client.controller;

import guesstheword_client.network.ServerConnection;
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
 * Controller principale per la gestione dell'autenticazione utente (Login e Registrazione).
 * Gestisce l'interazione con l'interfaccia grafica (LoginView.fxml), la validazione dei
 * campi di input, e l'invio delle credenziali cifrate tramite hash SHA-256 al server.
 * 
 * @author William Menza
 */
public class LoginViewController implements Initializable {

    @FXML
    private Label titleLabel;

    @FXML
    private Label subtitleLabel;

    @FXML
    private Label usernameLabel;

    @FXML
    private TextField usernameField;

    @FXML
    private Label passwordLabel;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField showPasswordField;

    @FXML
    private ToggleButton togglePasswordButton;
    
    @FXML
    private Label confirmPasswordLabel;
    
    @FXML
    private PasswordField confirmPasswordField;
    
    @FXML
    private TextField showConfirmPasswordField;
    
    @FXML
    private ToggleButton toggleConfirmPasswordButton;

    @FXML
    private Label errorLabel;

    @FXML
    private Button loginButton;

    @FXML
    private Button registerButton;

    private boolean isLoginMode = true;
    private guesstheword_client.service.AuthService authService;

    /**
     * Inizializza il controller. Viene chiamato automaticamente dopo il caricamento del file FXML.
     * Configura le icone per mostrare/nascondere le password, imposta il font FontAwesome
     * e prepara i binding di visualizzazione.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        authService = new guesstheword_client.service.AuthService();
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
        
        // Sincronizzazione bidirezionale del testo inserito per la password
        showPasswordField.textProperty().bindBidirectional(passwordField.textProperty());
        
        // Imposta l'icona dell'occhio aperto come iniziale per la conferma password
        toggleConfirmPasswordButton.setText("\uf06e");

        // Sincronizzazione bidirezionale del testo inserito per la conferma password
        showConfirmPasswordField.textProperty().bindBidirectional(confirmPasswordField.textProperty());
    }

    /**
     * Alterna la visibilità del testo della password (chiaro/nascosto) modificando l'icona
     * del pulsante "occhio" per il campo password principale.
     * 
     * @param event l'evento generato dal click sul ToggleButton
     */
    @FXML
    private void handleTogglePassword(ActionEvent event) {
        if (togglePasswordButton.isSelected()) {
            togglePasswordButton.setText("\uf070"); // fa-eye-slash
        } else {
            togglePasswordButton.setText("\uf06e"); // fa-eye
        }
    }

    /**
     * Alterna la visibilità del testo della password (chiaro/nascosto) modificando l'icona
     * del pulsante "occhio" per il campo conferma password. Mostra il campo appropriato
     * in base allo stato del toggle.
     * 
     * @param event l'evento generato dal click sul ToggleButton
     */
    @FXML
    private void handleToggleConfirmPassword(ActionEvent event) {
        if (toggleConfirmPasswordButton.isSelected()) {
            toggleConfirmPasswordButton.setText("\uf070"); // fa-eye-slash
            showConfirmPasswordField.setVisible(true);
            confirmPasswordField.setVisible(false);
        } else {
            toggleConfirmPasswordButton.setText("\uf06e"); // fa-eye
            showConfirmPasswordField.setVisible(false);
            confirmPasswordField.setVisible(true);
        }
    }

    /**
     * Cambia la modalità della schermata tra "Login" e "Registrazione".
     * Modifica dinamicamente le label, i bottoni e sposta (shift) in alto o in basso
     * i campi di testo per fare spazio al campo "Conferma Password" quando necessario.
     * 
     * @param event l'evento generato dal click sul bottone di switch
     */
    @FXML
    private void handleSwitchMode(ActionEvent event) {
        isLoginMode = !isLoginMode;
        errorLabel.setText(""); // resetta errori quando si cambia modalità
        
        boolean showConfirm = !isLoginMode;
        confirmPasswordLabel.setVisible(showConfirm);
        
        // Rispetta lo stato del toggle quando mostra la conferma
        if (showConfirm) {
            if (toggleConfirmPasswordButton.isSelected()) {
                showConfirmPasswordField.setVisible(true);
                confirmPasswordField.setVisible(false);
            } else {
                showConfirmPasswordField.setVisible(false);
                confirmPasswordField.setVisible(true);
            }
        } else {
            showConfirmPasswordField.setVisible(false);
            confirmPasswordField.setVisible(false);
        }
        
        toggleConfirmPasswordButton.setVisible(showConfirm);

        // Alza o abbassa i textfields, mantenendo i bottoni fermi
        double offset = showConfirm ? -76.0 : 76.0;
        
        usernameLabel.setLayoutY(usernameLabel.getLayoutY() + offset);
        usernameField.setLayoutY(usernameField.getLayoutY() + offset);
        passwordLabel.setLayoutY(passwordLabel.getLayoutY() + offset);
        passwordField.setLayoutY(passwordField.getLayoutY() + offset);
        showPasswordField.setLayoutY(showPasswordField.getLayoutY() + offset);
        togglePasswordButton.setLayoutY(togglePasswordButton.getLayoutY() + offset);
        
        confirmPasswordLabel.setLayoutY(confirmPasswordLabel.getLayoutY() + offset);
        confirmPasswordField.setLayoutY(confirmPasswordField.getLayoutY() + offset);
        showConfirmPasswordField.setLayoutY(showConfirmPasswordField.getLayoutY() + offset);
        toggleConfirmPasswordButton.setLayoutY(toggleConfirmPasswordButton.getLayoutY() + offset);
        
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

    /**
     * Gestisce l'azione principale del form (effettua Login o Registrazione).
     * Recupera i dati dai campi di testo, esegue controlli stringenti di validazione
     * (lunghezza minima, caratteri vietati, coincidenza password) e invia le credenziali
     * cifrate al Server. Attende infine la risposta per concedere l'accesso o mostrare un errore.
     * 
     * @param event l'evento generato dal bottone di submit (Login o Conferma Registrazione)
     */
    @FXML
    private void handleAction(ActionEvent event) {
        String username = usernameField.getText().trim();
        // Acquisizione sicura del testo dal campo attivo
        String password = togglePasswordButton.isSelected() ? showPasswordField.getText() : passwordField.getText();
        if (password == null) {
            password = "";
        }
        
        String confirmPassword = toggleConfirmPasswordButton.isSelected() ? showConfirmPasswordField.getText() : confirmPasswordField.getText();
        if (confirmPassword == null) {
            confirmPassword = "";
        }

        // Validazione base
        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Inserisci username e/o password!");
            return;
        }

        // Controllo caratteri vietati
        if (username.contains(":") || password.contains(":") || username.contains("\u001F") || password.contains("\u001F")) {
            errorLabel.setText("Caratteri speciali non consentiti!");
            return;
        }
        
        // Controllo lunghezza minima
        if (username.length() < 5) {
            errorLabel.setText("L'username deve avere almeno 5 caratteri.");
            return;
        }
        
        if (password.length() < 7) {
            errorLabel.setText("La password deve avere almeno 7 caratteri.");
            return;
        }
        
        // Controllo corrispondenza password in fase di registrazione
        if (!isLoginMode) {
            if (!password.equals(confirmPassword)) {
                errorLabel.setText("Le password non coincidono!");
                return;
            }
        }

        try {
            String response;
            if (isLoginMode) {
                System.out.println("[Client] Invio richiesta di login per: " + username);
                response = authService.login(username, password);
            } else {
                System.out.println("[Client] Invio richiesta di registrazione per: " + username);
                response = authService.register(username, password);
            }

            if (response != null) {
                String[] parts = guesstheword_client.network.MessageProtocol.parse(response);
                String command = parts[0];

                if (command.equals(guesstheword_client.network.MessageProtocol.AUTH_OK)) {
                    System.out.println("[Client] Successo! Benvenuto: " + parts[1]);
                    
                    try {
                        guesstheword_client.utils.SceneManager.switchScene(event, "/guesstheword_client/resources/view/DifficultyView.fxml");
                    } catch (IOException e) {
                        errorLabel.setText("Errore caricamento schermata difficoltà.");
                        e.printStackTrace();
                        ServerConnection.getInstance().close();
                    }
                } else if (command.equals(guesstheword_client.network.MessageProtocol.AUTH_FAIL)) {
                    String reason = parts.length > 1 ? parts[1] : "Errore sconosciuto.";
                    errorLabel.setText(reason);
                    ServerConnection.getInstance().close(); // Chiudiamo solo se fallisce
                } else {
                    errorLabel.setText("Risposta del server non riconosciuta.");
                    ServerConnection.getInstance().close(); // Chiudiamo solo se fallisce
                }
            } else {
                errorLabel.setText("Il server non ha risposto.");
                ServerConnection.getInstance().close(); // Chiudiamo solo se fallisce
            }

        } catch (java.net.ConnectException e) {
            errorLabel.setText("Impossibile connettersi al server!");
        } catch (IOException e) {
            errorLabel.setText("Errore di rete durante la comunicazione.");
            e.printStackTrace();
        }
    }
}
