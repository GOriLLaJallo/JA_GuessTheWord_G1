package guesstheword_server.db;

import guesstheword_server.model.Challenge;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Data Access Object (DAO) per la gestione della persistenza dell'entità Challenge.
 * Fornisce metodi per memorizzare nuove sfide nel database SQLite e recuperarle tramite il loro ID.
 * 
 * @author Carmine Muollo
 */
public class ChallengeDAO {

    /** Formattatore standard per la serializzazione delle date in SQLite. */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Salva una nuova sfida nel database SQLite.
     * Imposta l'ID autoincrementante generato sull'oggetto Challenge passato come parametro.
     *
     * @param challenge la sfida da salvare
     * @return true se il salvataggio è riuscito, false altrimenti
     */
    public boolean save(Challenge challenge) {
        String query = "INSERT INTO sfide (parola_nascosta, shift_cesare, data_sfida) VALUES (?, ?, ?);";
        DatabaseManager dbManager = DatabaseManager.getInstance();

        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, challenge.getParolaNascosta());
            ps.setInt(2, challenge.getShiftCesare());
            ps.setString(3, challenge.getDataSfida().format(DATE_FORMATTER));

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                return false;
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    challenge.setId(generatedKeys.getInt(1));
                }
            }
            return true;

        } catch (SQLException e) {
            System.err.println("[ChallengeDAO] Errore durante il salvataggio della sfida: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cerca una sfida nel database tramite il suo identificativo unico (ID).
     *
     * @param id l'identificativo della sfida
     * @return l'oggetto Challenge se trovato, null altrimenti
     */
    public Challenge findById(int id) {
        String query = "SELECT id, parola_nascosta, shift_cesare, data_sfida FROM sfide WHERE id = ?;";
        DatabaseManager dbManager = DatabaseManager.getInstance();

        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Challenge challenge = new Challenge();
                    challenge.setId(rs.getInt("id"));
                    challenge.setParolaNascosta(rs.getString("parola_nascosta"));
                    challenge.setShiftCesare(rs.getInt("shift_cesare"));
                    
                    String dateStr = rs.getString("data_sfida");
                    challenge.setDataSfida(LocalDateTime.parse(dateStr, DATE_FORMATTER));
                    
                    return challenge;
                }
            }

        } catch (SQLException e) {
            System.err.println("[ChallengeDAO] Errore durante il recupero della sfida per ID: " + e.getMessage());
        }
        return null;
    }
}
