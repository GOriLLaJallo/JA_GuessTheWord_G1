package guesstheword_server.service;

import guesstheword_server.db.DatabaseManager;
import guesstheword_server.db.ChallengeDAO;
import guesstheword_server.db.ResultDAO;
import guesstheword_server.model.Challenge;
import guesstheword_server.model.GameResult;
import guesstheword_server.exception.DataAccessException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Servizio transazionale per il salvataggio atomico delle partite (Caso d'uso OP3).
 * Garantisce che l'inserimento di una sfida e dei relativi due risultati (per i due giocatori)
 * avvenga all'interno di una transazione JDBC unica. Se uno dei salvataggi fallisce,
 * viene eseguito il rollback ripristinando la coerenza del database.
 * 
 * @author Carmine Muollo
 */
public class MatchPersistenceService {

    private final ChallengeDAO challengeDAO;
    private final ResultDAO resultDAO;

    /**
     * Costruisce una nuova istanza di MatchPersistenceService con DAO predefiniti.
     */
    public MatchPersistenceService() {
        this.challengeDAO = new ChallengeDAO();
        this.resultDAO = new ResultDAO();
    }

    /**
     * Costruisce una nuova istanza di MatchPersistenceService con DAO personalizzati.
     * 
     * @param challengeDAO il DAO delle sfide
     * @param resultDAO il DAO dei risultati
     */
    public MatchPersistenceService(ChallengeDAO challengeDAO, ResultDAO resultDAO) {
        this.challengeDAO = challengeDAO;
        this.resultDAO = resultDAO;
    }

    /**
     * Salva in modo atomico una partita completa: inserisce esattamente una sfida e due risultati.
     * 
     * @param challenge la sfida disputata
     * @param r1 il risultato del primo giocatore
     * @param r2 il risultato del secondo giocatore (avversario)
     * @throws DataAccessException in caso di errore di persistenza o fallimento della transazione
     */
    public void saveMatch(Challenge challenge, GameResult r1, GameResult r2) {
        if (challenge == null) {
            throw new IllegalArgumentException("La sfida non puo essere null.");
        }

        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            conn.setAutoCommit(false); // Avvia la transazione
            try {
                // 1. Salva la sfida nel DB (questo assegna l'ID autoincrementale all'oggetto challenge)
                challengeDAO.save(challenge, conn);

                // 2. Salva il risultato del primo giocatore, se presente
                if (r1 != null) {
                    r1.setSfida(challenge);
                    resultDAO.save(r1, conn);
                }

                // 3. Salva il risultato del secondo giocatore, se presente
                if (r2 != null) {
                    r2.setSfida(challenge);
                    resultDAO.save(r2, conn);
                }

                // Esegue il commit
                conn.commit();
                System.out.println("[MatchService] Partita salvata con successo. Transazione committata.");

            } catch (Exception e) {
                try {
                    conn.rollback();
                    System.err.println("[MatchService] Errore riscontrato durante il salvataggio della partita. Eseguito rollback transazione.");
                } catch (SQLException ex) {
                    System.err.println("[MatchService] Errore critico durante il rollback della transazione: " + ex.getMessage());
                }
                if (e instanceof DataAccessException) {
                    throw (DataAccessException) e;
                } else {
                    throw new DataAccessException("Errore transazionale nel salvataggio della partita completa", e);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Errore di connessione durante il salvataggio transazionale del match", e);
        }
    }
}
