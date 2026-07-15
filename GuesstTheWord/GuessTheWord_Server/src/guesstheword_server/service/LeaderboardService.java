package guesstheword_server.service;

import guesstheword_server.db.ResultDAO;
import guesstheword_server.model.LeaderboardEntry;
import guesstheword_server.model.UserStatsDTO;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Servizio per la gestione della classifica globale e delle statistiche degli
 * utenti. Aggrega le informazioni provenienti dal database fornendo report
 * strutturati.
 *
 * @author Carmine Muollo
 */
public class LeaderboardService {

    /** Lista osservabile globale per la classifica globale condivisa. */
    private static final ObservableList<LeaderboardEntry> OBS_LEADERBOARD = FXCollections.observableArrayList();

    /**
     * Il DAO per accedere ai risultati di gioco.
     */
    private final ResultDAO resultDAO;

    /**
     * Costruisce una nuova istanza di LeaderboardService con un ResultDAO
     * predefinito.
     */
    public LeaderboardService() {
        this.resultDAO = new ResultDAO();
    }

    /**
     * Costruisce una nuova istanza di LeaderboardService con un ResultDAO
     * personalizzato.
     *
     * @param resultDAO il DAO da utilizzare
     */
    public LeaderboardService(ResultDAO resultDAO) {
        this.resultDAO = resultDAO;
    }

    /**
     * Restituisce la classifica globale osservabile.
     *
     * @return la lista osservabile delle posizioni della classifica
     */
    public static ObservableList<LeaderboardEntry> getObservableLeaderboard() {
        return OBS_LEADERBOARD;
    }

    /**
     * Aggiorna i dati della classifica globale leggendoli dal database
     * e riversandoli nella lista osservabile condivisa.
     */
    public void refreshSharedLeaderboard() {
        List<LeaderboardEntry> freshData = getLeaderboard();
        OBS_LEADERBOARD.setAll(freshData);
    }

    /**
     * Recupera la classifica globale degli utenti, ordinati per tempo medio di
     * risposta in modo crescente.
     *
     * @return la lista di LeaderboardEntry per la classifica
     */
    public List<LeaderboardEntry> getLeaderboard() {
        List<LeaderboardEntry> leaderboard = resultDAO.getLeaderboard();
        guesstheword_server.db.UserDAO userDAO = new guesstheword_server.db.UserDAO();
        for (LeaderboardEntry entry : leaderboard) {
            guesstheword_server.model.User user = userDAO.findByUsername(entry.getUsername());
            if (user != null) {
                List<guesstheword_server.model.GameResult> history = resultDAO.getHistoryByUserId(user.getId());
                long sum = 0;
                int count = 0;
                for (guesstheword_server.model.GameResult r : history) {
                    if (r.getTempoRisposta() != null) {
                        sum += r.getTempoRisposta();
                        count++;
                    }
                }
                if (count > 0) {
                    entry.setTempoMedio((double) sum / count);
                } else {
                    entry.setTempoMedio(0.0);
                }
            }
        }
        leaderboard.sort((e1, e2) -> Double.compare(e1.getTempoMedio(), e2.getTempoMedio()));
        return leaderboard;
    }

    /**
     * Calcola e aggrega le statistiche individuali di un utente interrogando il
     * DAO. Questo metodo sfrutta una query consolidata per ridurre l'overhead
     * di connessione al database.
     *
     * @param userId l'identificativo dell'utente
     * @return un oggetto UserStatsDTO popolato con le statistiche aggregate
     * dell'utente
     */
    public UserStatsDTO getUserStats(int userId) {
        return resultDAO.getUserStats(userId);
    }
}
