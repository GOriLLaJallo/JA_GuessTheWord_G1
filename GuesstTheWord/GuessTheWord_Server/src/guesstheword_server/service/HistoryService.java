package guesstheword_server.service;

import guesstheword_server.db.ResultDAO;
import guesstheword_server.model.GameResult;
import java.util.List;

/**
 * Servizio per la gestione dello storico delle partite giocate dagli utenti.
 * Interagisce con il database delegando le operazioni a ResultDAO.
 *
 * @author Carmine Muollo
 */
public class HistoryService {

    private final ResultDAO resultDAO;

    /**
     * Costruisce una nuova istanza di HistoryService con un ResultDAO
     * predefinito.
     */
    public HistoryService() {
        this.resultDAO = new ResultDAO();
    }

    /**
     * Costruisce una nuova istanza di HistoryService con un ResultDAO
     * personalizzato.
     *
     * @param resultDAO il DAO da utilizzare
     */
    public HistoryService(ResultDAO resultDAO) {
        this.resultDAO = resultDAO;
    }

    /**
     * Recupera la cronologia completa delle partite di un utente.
     *
     * @param userId l'identificativo dell'utente
     * @return la lista di GameResult associati all'utente
     */
    public List<GameResult> getHistoryByUserId(int userId) {
        return resultDAO.getHistoryByUserId(userId);
    }
}
