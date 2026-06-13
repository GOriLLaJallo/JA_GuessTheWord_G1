package guesstheword_server.controller;

import guesstheword_server.service.AuthService;
import guesstheword_server.model.User;
import guesstheword_server.exception.DataAccessException;
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
 * Controller per la schermata di login dell'amministratore (AdminLoginView.fxml).
 * Gestisce l'inserimento delle credenziali dell'amministratore, la cifratura delle password
 * tramite SHA-256 e il reindirizzamento alla console principale (AdminMainView) in caso di successo.
 * 
 * @author Carmine Muollo
 */
public class AdminLoginViewController implements Initializable {

    /** Campo di testo per l'inserimento dell'username dell'admin. */
    @FXML
    private TextField usernameField;

    /** Campo per l'inserimento della password dell'admin. */
    @FXML
    private PasswordField passwordField;

    /** Campo di testo per visualizzare la password in chiaro. */
    @FXML
    private TextField showPasswordField;

    /** Pulsante toggle per mostrare/nascondere la password. */
    @FXML
    private ToggleButton togglePasswordButton;

    /** Label per la visualizzazione di messaggi di errore durante il login. */
    @FXML
    private Label errorLabel;

    /** Pulsante per l'invio delle credenziali e l'esecuzione del login. */
    @FXML
    private Button loginButton;

    private AuthService authService;

    /**
     * Inizializza il controller. Viene chiamato automaticamente dopo il caricamento del file FXML.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        authService = new AuthService();
        errorLabel.setText(""); // Resetta eventuali messaggi di errore iniziali

        // Caricamento programmatico del font FontAwesome per assicurarne la disponibilità
        try {
            Font.loadFont(getClass().getResourceAsStream("/guesstheword_server/resources/styles/fontawesome-webfont.ttf"), 16);
        } catch (Exception e) {
            System.err.println("[Login] Errore nel caricamento del font FontAwesome: " + e.getMessage());
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

        // Supporto per l'invio del form premendo il tasto Invio (Enter)
        usernameField.setOnAction(this::handleLogin);
        passwordField.setOnAction(this::handleLogin);
        showPasswordField.setOnAction(this::handleLogin);
    }

    /**
     * Gestisce la visibilità della password modificando l'icona ed il testo del pulsante toggle.
     *
     * @param event l'evento generato dal click
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
     * Gestisce l'evento di click sul pulsante Login.
     * Recupera le credenziali, calcola l'hash della password e autentica l'utente tramite AuthService.
     * In caso di esito positivo e ruolo 'admin', esegue la transizione alla dashboard principale.
     *
     * @param event l'evento generato dal click
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        errorLabel.setText(""); // Reset dell'etichetta dell'errore ad inizio tentativo
        
        String username = usernameField.getText().trim();
        // Acquisizione sicura del testo dal campo attivo per evitare problemi di sincronizzazione del binding
        String password = togglePasswordButton.isSelected() ? showPasswordField.getText() : passwordField.getText();
        if (password == null) {
            password = "";
        }

        // Validazione form
        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Inserisci username e password!");
            return;
        }

        // Autenticazione tramite database
        try {
            User user = authService.login(username, password);

            if (user == null) {
                errorLabel.setText("Username o password errati!");
                return;
            }

            // Controllo del ruolo: deve essere 'admin'
            if (!"admin".equalsIgnoreCase(user.getRuolo())) {
                errorLabel.setText("Accesso negato: autorizzazione insufficiente.");
                return;
            }

            // Transizione alla console dell'amministratore (AdminMainView.fxml)
            try {
                System.out.println("[Login] Login effettuato con successo. Benvenuto " + user.getUsername());
                
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/guesstheword_server/resources/view/AdminMainView.fxml"));
                Parent mainViewParent = loader.load();
                Scene mainScene = new Scene(mainViewParent);
                
                // Recupera lo Stage corrente
                Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
                window.setScene(mainScene);
                window.setTitle("GuessTheWord - Pannello di Amministrazione");
                window.centerOnScreen();
                window.show();
                
            } catch (IOException e) {
                System.err.println("[Login] Errore nel caricamento della vista AdminMainView: " + e.getMessage());
                e.printStackTrace();
                errorLabel.setText("Errore di caricamento della dashboard!");
            }
        } catch (DataAccessException e) {
            System.err.println("[Login] Errore del database durante l'accesso: " + e.getMessage());
            e.printStackTrace();
            errorLabel.setText("Errore di connessione al database!");
        }
    }
}
