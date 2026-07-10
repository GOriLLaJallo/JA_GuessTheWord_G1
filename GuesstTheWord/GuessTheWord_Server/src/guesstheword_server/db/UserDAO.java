package guesstheword_server.db;

import guesstheword_server.model.User;
import guesstheword_server.exception.DataAccessException;
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
     * Costruttore di default esplicito per la classe UserDAO.
     */
    public UserDAO() {
        // Costruttore vuoto di default
    }

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
            throw new DataAccessException("Errore durante la registrazione dell'utente: " + user.getUsername(), e);
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
        if (passwordHash == null) return null;
        String query = "SELECT id, username, password, ruolo, data_iscrizione FROM utenti WHERE username = ? AND password = ?;";
        DatabaseManager dbManager = DatabaseManager.getInstance();

        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, username);
            ps.setString(2, passwordHash);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Errore durante l'autenticazione del nome utente: " + username, e);
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
                    return mapResultSetToUser(rs);
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Errore durante la ricerca dell'utente per ID: " + id, e);
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
                    return mapResultSetToUser(rs);
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Errore durante la ricerca dell'utente per username: " + username, e);
        }
        return null;
    }

    /**
     * Mappa una riga del ResultSet in un oggetto User.
     * Questo metodo helper privato elimina le ridondanze di parsing del ResultSet presenti 
     * nei vari metodi di interrogazione dell'utente.
     *
     * @param rs il ResultSet posizionato sulla riga corrente da mappare
     * @return un oggetto User popolato con i valori letti
     * @throws SQLException in caso di errore di lettura delle colonne del database
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setRuolo(rs.getString("ruolo"));
        
        String dateStr = rs.getString("data_iscrizione");
        if (dateStr != null) {
            user.setDataIscrizione(LocalDateTime.parse(dateStr, DATE_FORMATTER));
        }
        return user;
    }
}
