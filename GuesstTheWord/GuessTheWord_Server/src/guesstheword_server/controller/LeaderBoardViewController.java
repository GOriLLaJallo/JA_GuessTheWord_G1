package guesstheword_server.controller;

import guesstheword_server.service.LeaderboardService;
import guesstheword_server.model.LeaderboardEntry;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Controller per la schermata della classifica globale (LeaderBoardView.fxml).
 * Gestisce il popolamento e la formattazione della TableView contenente gli utenti
 * ordinati per le loro prestazioni medie di risposta.
 * 
 * @author Carmine Muollo
 */
public class LeaderBoardViewController implements Initializable {

    /** Tabella principale della classifica. */
    @FXML
    private TableView<LeaderboardEntry> leaderboardTable;

    /** Colonna per il nome utente del giocatore. */
    @FXML
    private TableColumn<LeaderboardEntry, String> usernameColumn;

    /** Colonna per il numero di partite vinte. */
    @FXML
    private TableColumn<LeaderboardEntry, Integer> victoriesColumn;

    /** Colonna per il tempo medio di risposta in millisecondi. */
    @FXML
    private TableColumn<LeaderboardEntry, Double> avgTimeColumn;

    private LeaderboardService leaderboardService;

    /**
     * Costruttore di default esplicito per la classe LeaderBoardViewController.
     */
    public LeaderBoardViewController() {
        // Costruttore vuoto di default
    }

    /**
     * Inizializza il controller. Configura le associazioni delle colonne e lega la tabella
     * alla classifica reattiva globale condivisa.
     *
     * @param url il percorso della risorsa FXML
     * @param rb le risorse localizzate per il caricamento
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        leaderboardService = new LeaderboardService();

        // Associazione delle colonne con i campi del modello LeaderboardEntry
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        victoriesColumn.setCellValueFactory(new PropertyValueFactory<>("vittorie"));
        avgTimeColumn.setCellValueFactory(new PropertyValueFactory<>("tempoMedio"));

        // Formattazione del tempo medio a due cifre decimali con suffisso "ms"
        avgTimeColumn.setCellFactory(column -> new TableCell<LeaderboardEntry, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f ms", item));
                }
            }
        });

        // Binding reattivo: lega la TableView direttamente alla lista osservabile globale
        leaderboardTable.setItems(LeaderboardService.getObservableLeaderboard());

        // Caricamento dei dati della classifica
        loadLeaderboardData();
    }

    /**
     * Carica i dati della classifica globale dal database aggiornando la lista condivisa.
     */
    public void loadLeaderboardData() {
        try {
            leaderboardService.refreshSharedLeaderboard();
            System.out.println("[Leaderboard] Dati della classifica aggiornati con successo.");
        } catch (Exception e) {
            System.err.println("[Leaderboard] Errore nel caricamento dei dati: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
