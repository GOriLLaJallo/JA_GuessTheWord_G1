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
        if (challenge == null || r1 == null || r2 == null) {
            throw new IllegalArgumentException("I parametri di input (sfida, risultato1, risultato2) non possono essere null.");
        }

        Connection conn = null;
        try {
            conn = DatabaseManager.getInstance().getConnection();
            conn.setAutoCommit(false); // Avvia la transazione disattivando l'autoCommit

            // 1. Salva la sfida nel DB (questo assegna l'ID autoincrementale all'oggetto challenge)
            challengeDAO.save(challenge, conn);

            // Associa la sfida appena salvata a entrambi i risultati di gioco
            r1.setSfida(challenge);
            r2.setSfida(challenge);

            // 2. Salva il risultato del primo giocatore
            resultDAO.save(r1, conn);

            // 3. Salva il risultato del secondo giocatore
            resultDAO.save(r2, conn);

            // Esegue il commit della transazione se tutti gli inserimenti sono andati a buon fine
            conn.commit();
            System.out.println("[MatchService] Partita salvata con successo (1 sfida, 2 risultati). Transazione committata.");

        } catch (Exception e) {
            // In caso di errore esegue il rollback della transazione
            if (conn != null) {
                try {
                    conn.rollback();
                    System.err.println("[MatchService] Errore riscontrato durante il salvataggio della partita. Eseguito rollback transazione.");
                } catch (SQLException ex) {
                    System.err.println("[MatchService] Errore critico durante il rollback della transazione: " + ex.getMessage());
                }
            }
            if (e instanceof DataAccessException) {
                throw (DataAccessException) e;
            } else {
                throw new DataAccessException("Errore transazionale nel salvataggio della partita completa", e);
            }
        } finally {
            // Rilascia la connessione chiudendola correttamente
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("[MatchService] Errore nella chiusura della connessione: " + e.getMessage());
                }
            }
        }
    }
}
