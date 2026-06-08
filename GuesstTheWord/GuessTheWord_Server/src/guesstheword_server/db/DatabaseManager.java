package guesstheword_server.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Gestore del Database in modalità Singleton per l'applicazione GuessTheWord_Server.
 * Questa classe gestisce la connessione JDBC con il database SQLite, l'apertura e la chiusura 
 * delle risorse di connessione e l'inizializzazione dello schema del database (creazione tabelle).
 * 
 * @author Carmine Muollo
 */
public class DatabaseManager {

    /** Istanza unica del DatabaseManager (Pattern Singleton). */
    private static DatabaseManager instance;

    /** Percorso relativo della directory del database. */
    private static final String DB_DIR = "SQLite db";

    /** Nome del file del database SQLite. */
    private static final String DB_FILE = "database.db";

    /** URL JDBC per la connessione al database SQLite. */
    private static final String DB_URL = "jdbc:sqlite:" + DB_DIR + "/" + DB_FILE;

    /** Connessione JDBC condivisa. */
    private Connection connection;

    /**
     * Costruttore privato per prevenire l'istanziazione diretta dall'esterno (Singleton).
     * Inizializza la directory del database e crea lo schema iniziale delle tabelle.
     */
    private DatabaseManager() {
        createDatabaseDirectory();
        initSchema();
    }

    /**
     * Restituisce l'istanza unica del DatabaseManager.
     * Implementa l'inizializzazione lazy thread-safe.
     *
     * @return l'istanza di DatabaseManager
     */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /**
     * Crea la directory per il database se non è già presente sul filesystem.
     */
    private void createDatabaseDirectory() {
        File dir = new File(DB_DIR);
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                System.out.println("[DB] Cartella '" + DB_DIR + "' creata con successo.");
            } else {
                System.err.println("[DB] Errore nella creazione della cartella '" + DB_DIR + "'.");
            }
        }
    }

    /**
     * Restituisce la connessione attiva al database SQLite.
     * Se la connessione non è presente o è stata chiusa, ne viene aperta una nuova.
     *
     * @return oggetto Connection per le operazioni JDBC
     * @throws SQLException in caso di errore di connessione al database
     */
    public synchronized Connection getConnection() throws SQLException {
        try {
            // Carica esplicitamente il driver SQLite
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("[DB] Driver JDBC SQLite non trovato: " + e.getMessage());
        }

        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
            // Abilita il supporto alle Foreign Key in SQLite
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON;");
            }
        }
        return connection;
    }

    /**
     * Chiude la connessione attiva al database se aperta.
     */
    public synchronized void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DB] Connessione al database chiusa correttamente.");
            }
        } catch (SQLException e) {
            System.err.println("[DB] Errore durante la chiusura della connessione: " + e.getMessage());
        }
    }

    /**
     * Inizializza lo schema del database eseguendo le istruzioni DDL di creazione 
     * delle tabelle utenti, sfide e risultati, se non già esistenti.
     */
    private void initSchema() {
        String createUtentiTable = "CREATE TABLE IF NOT EXISTS utenti ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "username TEXT UNIQUE NOT NULL, "
                + "password TEXT NOT NULL, "
                + "ruolo TEXT NOT NULL, "
                + "data_iscrizione TEXT NOT NULL"
                + ");";

        String createSfideTable = "CREATE TABLE IF NOT EXISTS sfide ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "parola_nascosta TEXT NOT NULL, "
                + "shift_cesare INTEGER NOT NULL, "
                + "data_sfida TEXT NOT NULL"
                + ");";

        String createRisultatiTable = "CREATE TABLE IF NOT EXISTS risultati ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "id_utente INTEGER NOT NULL, "
                + "id_sfida INTEGER NOT NULL, "
                + "esito TEXT NOT NULL, "
                + "risposta_inviata TEXT, "
                + "tempo_risposta INTEGER, "
                + "FOREIGN KEY(id_utente) REFERENCES utenti(id) ON DELETE CASCADE, "
                + "FOREIGN KEY(id_sfida) REFERENCES sfide(id) ON DELETE CASCADE"
                + ");";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createUtentiTable);
            stmt.execute(createSfideTable);
            stmt.execute(createRisultatiTable);

            System.out.println("[DB] Schema del database inizializzato con successo.");

        } catch (SQLException e) {
            System.err.println("[DB] Errore durante l'inizializzazione dello schema del DB: " + e.getMessage());
        }
    }
}
