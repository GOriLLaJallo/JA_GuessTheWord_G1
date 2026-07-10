package guesstheword_server.db;

import guesstheword_server.model.Challenge;
import guesstheword_server.exception.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Data Access Object (DAO) per la gestione della persistenza dell'entità
 * Challenge. Fornisce metodi per memorizzare nuove sfide nel database SQLite e
 * recuperarle tramite il loro ID.
 *
 * @author Carmine Muollo
 */
public class ChallengeDAO {

    /**
     * Formattatore standard per la serializzazione delle date in SQLite.
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Costruttore di default esplicito per la classe ChallengeDAO.
     */
    public ChallengeDAO() {
        // Costruttore vuoto di default
    }

    /**
     * Salva una nuova sfida nel database SQLite utilizzando una connessione
     * gestita esternamente. Utile all'interno di transazioni JDBC.
     *
     * @param challenge la sfida da salvare
     * @param conn la connessione JDBC attiva
     * @return true se il salvataggio è riuscito
     * @throws DataAccessException in caso di errore di persistenza
     */
    public boolean save(Challenge challenge, Connection conn) {
        String query = "INSERT INTO sfide (parola_nascosta, shift_cesare, data_sfida, difficolta) VALUES (?, ?, ?, ?);";
        try (PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, challenge.getParolaNascosta());
            ps.setInt(2, challenge.getShiftCesare());
            ps.setString(3, challenge.getDataSfida().format(DATE_FORMATTER));
            ps.setString(4, challenge.getDifficolta());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new DataAccessException("Salvataggio sfida fallito: nessuna riga inserita.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    challenge.setId(generatedKeys.getInt(1));
                }
            }
            return true;
        } catch (SQLException e) {
            throw new DataAccessException("Errore durante il salvataggio della sfida nel DB", e);
        }
    }

    /**
     * Salva una nuova sfida nel database SQLite. Apre e gestisce internamente
     * la connessione.
     *
     * @param challenge la sfida da salvare
     * @return true se il salvataggio è riuscito
     * @throws DataAccessException in caso di errore di persistenza
     */
    public boolean save(Challenge challenge) {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            return save(challenge, conn);
        } catch (SQLException e) {
            throw new DataAccessException("Errore di connessione durante il salvataggio della sfida", e);
        }
    }

    /**
     * Cerca una sfida nel database tramite il suo identificativo unico (ID).
     *
     * @param id l'identificativo della sfida
     * @return l'oggetto Challenge se trovato, null altrimenti
     */
    public Challenge findById(int id) {
        String query = "SELECT id, parola_nascosta, shift_cesare, data_sfida, difficolta FROM sfide WHERE id = ?;";
        DatabaseManager dbManager = DatabaseManager.getInstance();

        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Challenge challenge = new Challenge();
                    challenge.setId(rs.getInt("id"));
                    challenge.setParolaNascosta(rs.getString("parola_nascosta"));
                    challenge.setShiftCesare(rs.getInt("shift_cesare"));

                    String dateStr = rs.getString("data_sfida");
                    challenge.setDataSfida(LocalDateTime.parse(dateStr, DATE_FORMATTER));
                    challenge.setDifficolta(rs.getString("difficolta"));

                    return challenge;
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Errore durante il recupero della sfida per ID: " + id, e);
        }
        return null;
    }
}
