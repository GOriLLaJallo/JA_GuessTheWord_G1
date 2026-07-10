package guesstheword_server.service;

import guesstheword_server.db.ResultDAO;
import guesstheword_server.model.LeaderboardEntry;
import guesstheword_server.model.UserStatsDTO;
import java.util.List;

/**
 * Servizio per la gestione della classifica globale e delle statistiche degli utenti.
 * Aggrega le informazioni provenienti dal database fornendo report strutturati.
 * 
 * @author Carmine Muollo
 */
public class LeaderboardService {

    /** Il DAO per accedere ai risultati di gioco. */
    private final ResultDAO resultDAO;

    /**
     * Costruisce una nuova istanza di LeaderboardService con un ResultDAO predefinito.
     */
    public LeaderboardService() {
        this.resultDAO = new ResultDAO();
    }

    /**
     * Costruisce una nuova istanza di LeaderboardService con un ResultDAO personalizzato.
     * 
     * @param resultDAO il DAO da utilizzare
     */
    public LeaderboardService(ResultDAO resultDAO) {
        this.resultDAO = resultDAO;
    }

    /**
     * Recupera la classifica globale degli utenti, ordinati per tempo medio di risposta in modo crescente.
     * 
     * @return la lista di LeaderboardEntry per la classifica
     */
    public List<LeaderboardEntry> getLeaderboard() {
        return resultDAO.getLeaderboard();
    }

    /**
     * Calcola e aggrega le statistiche individuali di un utente interrogando il DAO.
     * Questo metodo sfrutta una query consolidata per ridurre l'overhead di connessione al database.
     * 
     * @param userId l'identificativo dell'utente
     * @return un oggetto UserStatsDTO popolato con le statistiche aggregate dell'utente
     */
    public UserStatsDTO getUserStats(int userId) {
        return resultDAO.getUserStats(userId);
    }
}
