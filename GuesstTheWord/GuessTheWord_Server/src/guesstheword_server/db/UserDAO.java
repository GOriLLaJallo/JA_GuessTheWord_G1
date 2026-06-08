package guesstheword_server.db;

import guesstheword_server.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Data Access Object (DAO) per la gestione della persistenza dell'entità User.
 * Fornisce i metodi per la registrazione dei nuovi utenti, la ricerca per ID o username
 * e l'autenticazione tramite corrispondenza delle credenziali nel database SQLite.
 * 
 * @author Carmine Muollo
 */
public class UserDAO {

    /** Formattatore standard per la serializzazione delle date in SQLite. */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Registra un nuovo utente nel database.
     * Al termine dell'operazione, imposta l'ID autoincrementante generato dal DB
     * sull'oggetto User passato come parametro.
     *
     * @param user l'utente da registrare nel sistema
     * @return true se la registrazione è andata a buon fine, false altrimenti
     */
    public boolean register(User user) {
        String query = "INSERT INTO utenti (username, password, ruolo, data_iscrizione) VALUES (?, ?, ?, ?);";
        DatabaseManager dbManager = DatabaseManager.getInstance();

        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getRuolo());
            ps.setString(4, user.getDataIscrizione().format(DATE_FORMATTER));

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                return false;
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getInt(1));
                }
            }
            return true;

        } catch (SQLException e) {
            System.err.println("[UserDAO] Errore durante la registrazione dell'utente: " + e.getMessage());
            return false;
        }
    }

    /**
     * Verifica le credenziali inserite dall'utente per effettuare il login.
     *
     * @param username     il nome utente fornito
     * @param passwordHash l'hash della password inserita
     * @return l'oggetto User corrispondente se le credenziali sono corrette, null altrimenti
     */
    public User authenticate(String username, String passwordHash) {
        String query = "SELECT id, username, password, ruolo, data_iscrizione FROM utenti WHERE username = ? AND password = ?;";
        DatabaseManager dbManager = DatabaseManager.getInstance();

        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, username);
            ps.setString(2, passwordHash);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password"));
                    user.setRuolo(rs.getString("ruolo"));
                    
                    String dateStr = rs.getString("data_iscrizione");
                    user.setDataIscrizione(LocalDateTime.parse(dateStr, DATE_FORMATTER));
                    
                    return user;
                }
            }

        } catch (SQLException e) {
            System.err.println("[UserDAO] Errore durante l'autenticazione: " + e.getMessage());
        }
        return null;
    }

    /**
     * Cerca un utente nel database tramite il suo identificativo unico (ID).
     *
     * @param id l'identificativo dell'utente
     * @return l'oggetto User corrispondente, o null se non trovato
     */
    public User findById(int id) {
        String query = "SELECT id, username, password, ruolo, data_iscrizione FROM utenti WHERE id = ?;";
        DatabaseManager dbManager = DatabaseManager.getInstance();

        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password"));
                    user.setRuolo(rs.getString("ruolo"));
                    
                    String dateStr = rs.getString("data_iscrizione");
                    user.setDataIscrizione(LocalDateTime.parse(dateStr, DATE_FORMATTER));
                    
                    return user;
                }
            }

        } catch (SQLException e) {
            System.err.println("[UserDAO] Errore durante la ricerca per ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Cerca un utente nel database tramite il suo nome utente (username).
     *
     * @param username il nome utente da cercare
     * @return l'oggetto User corrispondente, o null se non trovato
     */
    public User findByUsername(String username) {
        String query = "SELECT id, username, password, ruolo, data_iscrizione FROM utenti WHERE username = ?;";
        DatabaseManager dbManager = DatabaseManager.getInstance();

        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password"));
                    user.setRuolo(rs.getString("ruolo"));
                    
                    String dateStr = rs.getString("data_iscrizione");
                    user.setDataIscrizione(LocalDateTime.parse(dateStr, DATE_FORMATTER));
                    
                    return user;
                }
            }

        } catch (SQLException e) {
            System.err.println("[UserDAO] Errore durante la ricerca per username: " + e.getMessage());
        }
        return null;
    }
}
