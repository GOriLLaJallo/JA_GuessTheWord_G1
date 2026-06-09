package guesstheword_server.controller;

import guesstheword_server.db.ResultDAO;
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

    private ResultDAO resultDAO;

    /**
     * Inizializza il controller. Configura le associazioni delle colonne e carica i dati dal database.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        resultDAO = new ResultDAO();

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

        // Caricamento dei dati dalla classifica
        loadLeaderboardData();
    }

    /**
     * Carica i dati della classifica globale dal database e li imposta nella TableView.
     */
    public void loadLeaderboardData() {
        try {
            ObservableList<LeaderboardEntry> data = FXCollections.observableArrayList(resultDAO.getLeaderboard());
            leaderboardTable.setItems(data);
            System.out.println("[Leaderboard] Dati della classifica caricati con successo: " + data.size() + " record.");
        } catch (Exception e) {
            System.err.println("[Leaderboard] Errore nel caricamento dei dati: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
