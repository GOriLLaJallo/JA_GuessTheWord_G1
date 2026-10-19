package guesstheword_server.db;

import guesstheword_server.model.Challenge;
import guesstheword_server.model.GameResult;
import guesstheword_server.model.User;
import guesstheword_server.model.LeaderboardEntry;
import guesstheword_server.model.UserStatsDTO;
import guesstheword_server.exception.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) per la gestione della persistenza dell'entità
 * GameResult. Fornisce metodi per il salvataggio dei risultati delle partite
 * nel database SQLite, il recupero dello storico completo delle partite giocate
 * da un utente (con query JOIN) e il calcolo delle statistiche individuali
 * (vittorie, partite giocate, tempo medio).
 *
 * @author Carmine Muollo
 */
public class ResultDAO {

    /**
     * Formattatore standard per la serializzazione delle date in SQLite.
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Costruttore di default esplicito per la classe ResultDAO.
     */
    public ResultDAO() {
        // Costruttore vuoto di default
    }

    /**
     * Salva un nuovo risultato di gioco nel database SQLite utilizzando una
     * connessione esterna. Consente la partecipazione a transazioni JDBC.
     *
     * @param result il risultato da memorizzare
     * @param conn la connessione JDBC attiva
     * @return true se il salvataggio è andato a buon fine
     * @throws DataAccessException in caso di errore di persistenza
     */
    public boolean save(GameResult result, Connection conn) {
        String query = "INSERT INTO risultati (id_utente, id_sfida, esito, risposta_inviata, tempo_risposta) VALUES (?, ?, ?, ?, ?);";
        try (PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, result.getUtente().getId());
            ps.setInt(2, result.getSfida().getId());
            ps.setString(3, result.getEsito());

            if (result.getRispostaInviata() != null) {
                ps.setString(4, result.getRispostaInviata());
            } else {
                ps.setNull(4, Types.VARCHAR);
            }

            if (result.getTempoRisposta() != null) {
                ps.setInt(5, result.getTempoRisposta());
            } else {
                ps.setNull(5, Types.INTEGER);
            }

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new DataAccessException("Salvataggio risultato fallito: nessuna riga inserita.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    result.setId(generatedKeys.getInt(1));
                }
            }
            return true;
        } catch (SQLException e) {
            throw new DataAccessException("Errore durante il salvataggio del risultato di gioco nel DB", e);
        }
    }

    /**
     * Salva un nuovo risultato di gioco nel database SQLite. Apre e gestisce
     * internamente la connessione.
     *
     * @param result il risultato da memorizzare
     * @return true se il salvataggio è andato a buon fine
     * @throws DataAccessException in caso di errore di persistenza
     */
    public boolean save(GameResult result) {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            return save(result, conn);
        } catch (SQLException e) {
            throw new DataAccessException("Errore di connessione durante il salvataggio del risultato", e);
        }
    }

    /**
     * Recupera lo storico completo dei risultati di gioco per un determinato
     * utente. Esegue una query JOIN per popolare completamente gli oggetti
     * correlati User e Challenge.
     *
     * @param userId l'ID dell'utente di cui si vuole recuperare lo storico
     * @return una List di oggetti GameResult associati all'utente
     */
    public List<GameResult> getHistoryByUserId(int userId) {
        List<GameResult> history = new ArrayList<>();
        String query = "SELECT r.id AS r_id, r.esito, r.risposta_inviata, r.tempo_risposta, "
                + "u.id AS u_id, u.username, u.password, u.ruolo, u.data_iscrizione, "
                + "s.id AS s_id, s.parola_nascosta, s.shift_cesare, s.data_sfida, s.difficolta "
                + "FROM risultati r "
                + "JOIN utenti u ON r.id_utente = u.id "
                + "JOIN sfide s ON r.id_sfida = s.id "
                + "WHERE r.id_utente = ? "
                + "ORDER BY s.data_sfida DESC;";

        DatabaseManager dbManager = DatabaseManager.getInstance();

        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // Costruzione dell'oggetto User
                    User user = new User();
                    user.setId(rs.getInt("u_id"));
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password"));
                    user.setRuolo(rs.getString("ruolo"));
                    user.setDataIscrizione(LocalDateTime.parse(rs.getString("data_iscrizione"), DATE_FORMATTER));

                    // Costruzione dell'oggetto Challenge
                    Challenge challenge = new Challenge();
                    challenge.setId(rs.getInt("s_id"));
                    challenge.setParolaNascosta(rs.getString("parola_nascosta"));
                    challenge.setShiftCesare(rs.getInt("shift_cesare"));
                    challenge.setDataSfida(LocalDateTime.parse(rs.getString("data_sfida"), DATE_FORMATTER));
                    challenge.setDifficolta(rs.getString("difficolta"));

                    // Costruzione dell'oggetto GameResult
                    GameResult result = new GameResult();
                    result.setId(rs.getInt("r_id"));
                    result.setUtente(user);
                    result.setSfida(challenge);
                    result.setEsito(rs.getString("esito"));
                    result.setRispostaInviata(rs.getString("risposta_inviata"));

                    int tempo = rs.getInt("tempo_risposta");
                    // rs.wasNull() restituisce true se l'ultima colonna letta era NULL nel DB
                    if (rs.wasNull()) {
                        result.setTempoRisposta(null);
                    } else {
                        result.setTempoRisposta(tempo);
                    }

                    history.add(result);
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Errore durante il recupero dello storico partite per l'utente con ID: " + userId, e);
        }
        return history;
    }

    /**
     * Calcola il numero totale di vittorie per un determinato utente.
     *
     * @param userId l'identificativo dell'utente
     * @return il numero di vittorie ("WIN")
     */
    public int getVictoriesCount(int userId) {
        String query = "SELECT COUNT(*) FROM risultati WHERE id_utente = ? AND esito = 'WIN';";
        DatabaseManager dbManager = DatabaseManager.getInstance();

        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Errore durante il conteggio delle vittorie per l'utente con ID: " + userId, e);
        }
        return 0;
    }

    /**
     * Calcola il numero totale di partite (sfide) disputate da un determinato
     * utente.
     *
     * @param userId l'identificativo dell'utente
     * @return il numero totale di partite giocate
     */
    public int getGamesPlayedCount(int userId) {
        String query = "SELECT COUNT(*) FROM risultati WHERE id_utente = ?;";
        DatabaseManager dbManager = DatabaseManager.getInstance();

        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Errore durante il conteggio delle partite giocate per l'utente con ID: " + userId, e);
        }
        return 0;
    }

    /**
     * Calcola il tempo medio di risposta per le risposte andate a buon fine (o
     * comunque fornite) di un determinato utente. Esclude i timeout (dove il
     * tempo di risposta è null).
     *
     * @param userId l'identificativo dell'utente
     * @return il tempo medio di risposta in millisecondi, o 0.0 se non ci sono
     * risposte valide
     */
    public double getAverageResponseTime(int userId) {
        String query = "SELECT AVG(tempo_risposta) FROM risultati WHERE id_utente = ? AND tempo_risposta IS NOT NULL;";
        DatabaseManager dbManager = DatabaseManager.getInstance();

        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Errore durante il calcolo del tempo medio di risposta per l'utente con ID: " + userId, e);
        }
        return 0.0;
    }

    /**
     * Recupera la classifica globale degli utenti (leaderboard). Seleziona
     * tutti gli utenti che hanno almeno una partita vinta ('WIN'), ordinandoli
     * per tempo medio di risposta in modo ascendente.
     *
     * @return una lista di oggetti LeaderboardEntry ordinati
     */
    public List<LeaderboardEntry> getLeaderboard() {
        List<LeaderboardEntry> leaderboard = new ArrayList<>();
        String query = "SELECT u.username, "
                + "(SELECT COUNT(*) FROM risultati r2 WHERE r2.id_utente = u.id AND r2.esito = 'WIN') AS vittorie, "
                + "(SELECT AVG(r3.tempo_risposta) FROM risultati r3 WHERE r3.id_utente = u.id AND r3.tempo_risposta IS NOT NULL) AS tempo_medio "
                + "FROM utenti u "
                + "JOIN risultati r ON u.id = r.id_utente "
                + "WHERE r.esito = 'WIN' "
                + "GROUP BY u.id, u.username "
                + "ORDER BY tempo_medio ASC;";

        DatabaseManager dbManager = DatabaseManager.getInstance();

        try (Connection conn = dbManager.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String username = rs.getString("username");
                int vittorie = rs.getInt("vittorie");
                double tempoMedio = rs.getDouble("tempo_medio");

                leaderboard.add(new LeaderboardEntry(username, vittorie, tempoMedio));
            }

        } catch (SQLException e) {
            throw new DataAccessException("Errore durante il recupero della classifica utenti", e);
        }
        return leaderboard;
    }

    /**
     * Recupera in un'unica interrogazione aggregata le statistiche complete di
     * un utente. Questo metodo ottimizza l'accesso al database eseguendo una
     * query singola con aggregazione condizionale al posto di chiamate
     * sequenziali multiple.
     *
     * @param userId l'identificativo unico dell'utente
     * @return un oggetto UserStatsDTO popolato con vittorie, partite giocate e
     * tempo medio
     * @throws DataAccessException in caso di errore di persistenza o
     * connessione al database
     */
    public UserStatsDTO getUserStats(int userId) {
        String query = "SELECT "
                + "COUNT(CASE WHEN esito = 'WIN' THEN 1 END) AS victories, "
                + "COUNT(*) AS games_played, "
                + "AVG(tempo_risposta) AS avg_time "
                + "FROM risultati "
                + "WHERE id_utente = ?;";
        DatabaseManager dbManager = DatabaseManager.getInstance();

        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int victories = rs.getInt("victories");
                    int gamesPlayed = rs.getInt("games_played");
                    double averageResponseTime = rs.getDouble("avg_time");
                    return new UserStatsDTO(victories, gamesPlayed, averageResponseTime);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Errore durante il recupero delle statistiche per l'utente con ID: " + userId, e);
        }
        return new UserStatsDTO(0, 0, 0.0);
    }
}
