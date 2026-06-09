package guesstheword_server.controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TabPane;

/**
 * Controller per la vista contenitore delle schede principali (AdminMainView.fxml).
 * Gestisce il TabPane contenente le schede della dashboard e della leaderboard.
 * 
 * @author Carmine Muollo
 */
public class AdminMainViewController implements Initializable {

    /** Il TabPane che ospita le schede della console dell'amministratore. */
    @FXML
    private TabPane tabPane;

    /**
     * Inizializza il controller. Viene chiamato automaticamente dopo il caricamento del file FXML.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("[AdminMainView] Inizializzazione del TabPane principale completata.");
    }
}
