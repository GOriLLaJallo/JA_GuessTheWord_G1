package guesstheword_server.db;

import guesstheword_server.utils.HashUtil;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Gestore del Database come Connection Factory per l'applicazione
 * GuessTheWord_Server. Questa classe gestisce la connessione JDBC con il
 * database SQLite, l'apertura e la chiusura delle risorse di connessione e
 * l'inizializzazione dello schema del database (creazione tabelle).
 *
 * @author Carmine Muollo
 */
public class DatabaseManager {

    /**
     * Istanza unica del DatabaseManager (Pattern Singleton).
     */
    private static DatabaseManager instance;

    /**
     * Percorso relativo della directory del database.
     */
    private static final String DB_DIR = "SQLite db";

    /**
     * Nome del file del database SQLite.
     */
    private static final String DB_FILE = "database.db";

    /**
     * URL JDBC per la connessione al database SQLite.
     */
    private static final String DB_URL = "jdbc:sqlite:" + DB_DIR + "/" + DB_FILE;

    static {
        try {
            // Carica esplicitamente il driver SQLite una sola volta all'avvio della classe per ottimizzare le prestazioni
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("[DB] Driver JDBC SQLite non trovato: " + e.getMessage());
        }
    }

    /**
     * Costruttore privato per prevenire l'istanziazione diretta dall'esterno
     * (Singleton). Gestisce in modo deterministico la creazione della directory
     * estraendo il percorso genitore direttamente dal contesto dell'URL JDBC,
     * inizializzando poi lo schema iniziale del database.
     */
    private DatabaseManager() {
        String dbPath = DB_URL.replace("jdbc:sqlite:", "");
        File dbFile = new File(dbPath);
        File parentDir = dbFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        initSchema();
    }

    /**
     * Restituisce l'istanza unica del DatabaseManager. Implementa
     * l'inizializzazione lazy thread-safe.
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
     * Restituisce una NUOVA connessione attiva al database SQLite (Connection
     * Factory). Ogni thread riceve la propria istanza di connessione per
     * garantire la concurrency e la stabilità durante le transazioni JDBC.
     *
     * @return oggetto Connection indipendente per le operazioni JDBC
     * @throws SQLException in caso di errore di connessione al database
     */
    public Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL);
        // Abilita il supporto alle Foreign Key in SQLite
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }

    /**
     * Metodo deprecato. Le connessioni sono ora gestite in modalità
     * indipendente (Connection Factory). Ciascuna connessione aperta deve
     * essere chiusa direttamente dal chiamante (o tramite try-with-resources).
     */
    @Deprecated
    public void closeConnection() {
        // No-op per compatibilità di firma
    }

    /**
     * Inizializza lo schema del database eseguendo le istruzioni DDL di
     * creazione delle tabelle utenti, sfide e risultati, se non già esistenti.
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
                + "data_sfida TEXT NOT NULL, "
                + "difficolta TEXT"
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

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {

            stmt.execute(createUtentiTable);
            stmt.execute(createSfideTable);
            stmt.execute(createRisultatiTable);

            // Migrazione incrementale e idempotente per la colonna 'difficolta'
            boolean columnExists = false;
            try (ResultSet columnsRs = conn.getMetaData().getColumns(null, null, "sfide", "difficolta")) {
                if (columnsRs.next()) {
                    columnExists = true;
                }
            }
            if (!columnExists) {
                try (Statement alterStmt = conn.createStatement()) {
                    alterStmt.execute("ALTER TABLE sfide ADD COLUMN difficolta TEXT;");
                    System.out.println("[DB] Migrazione: colonna 'difficolta' aggiunta con successo alla tabella 'sfide'.");
                }
            }

            // Inserimento utente admin predefinito se non è già presente
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM utenti WHERE ruolo = 'admin';")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    // Password hash per "admin" usando SHA-256
                    String adminPasswordHash = HashUtil.sha256("admin");
                    String insertAdmin = "INSERT INTO utenti (username, password, ruolo, data_iscrizione) VALUES (?, ?, ?, ?);";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertAdmin)) {
                        insertStmt.setString(1, "admin");
                        insertStmt.setString(2, adminPasswordHash);
                        insertStmt.setString(3, "admin");
                        insertStmt.setString(4, java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                        insertStmt.executeUpdate();
                    }
                    System.out.println("[DB] Account amministratore predefinito creato.");
                }
            }

            // Inserimento utenti player predefiniti per il testing
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM utenti WHERE username = 'User1';")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    String user1PasswordHash = HashUtil.sha256("User123");
                    String insertUser = "INSERT INTO utenti (username, password, ruolo, data_iscrizione) VALUES (?, ?, ?, ?);";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertUser)) {
                        insertStmt.setString(1, "User1");
                        insertStmt.setString(2, user1PasswordHash);
                        insertStmt.setString(3, "player");
                        insertStmt.setString(4, java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                        insertStmt.executeUpdate();
                    }
                    System.out.println("[DB] Account di test 'User1' creato.");
                }
            }

            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM utenti WHERE username = 'User2';")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    String user2PasswordHash = HashUtil.sha256("User456");
                    String insertUser = "INSERT INTO utenti (username, password, ruolo, data_iscrizione) VALUES (?, ?, ?, ?);";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertUser)) {
                        insertStmt.setString(1, "User2");
                        insertStmt.setString(2, user2PasswordHash);
                        insertStmt.setString(3, "player");
                        insertStmt.setString(4, java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                        insertStmt.executeUpdate();
                    }
                    System.out.println("[DB] Account di test 'User2' creato.");
                }
            }

            System.out.println("[DB] Schema del database inizializzato con successo.");

        } catch (SQLException e) {
            System.err.println("[DB] Errore durante l'inizializzazione dello schema del DB: " + e.getMessage());
        }
    }
}
