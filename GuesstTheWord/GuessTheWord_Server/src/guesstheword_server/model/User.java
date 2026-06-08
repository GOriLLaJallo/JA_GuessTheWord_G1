package guesstheword_server.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Rappresenta un utente registrato nel sistema "GuessTheWord".
 * Questa classe definisce le credenziali dell'utente, il ruolo (giocatore o amministratore)
 * e la data di iscrizione al gioco.
 * 
 * @author Carmine Muollo
 */
public class User {

    /** Identificatore unico dell'utente nel database. */
    private int id;

    /** Nome utente univoco utilizzato per il login. */
    private String username;

    /** Hash della password dell'utente. */
    private String password;

    /** Ruolo dell'utente (es. "admin" per l'amministratore, "giocatore" per gli sfidanti). */
    private String ruolo;

    /** Data e ora in cui l'utente si è registrato nel sistema. */
    private LocalDateTime dataIscrizione;

    /**
     * Costruttore di default vuoto.
     */
    public User() {
    }

    /**
     * Costruttore completo per istanziare un utente con tutti i suoi attributi.
     * Utilizzato solitamente quando si recupera un utente esistente dal database.
     *
     * @param id             Identificatore unico dell'utente
     * @param username       Nome utente
     * @param password       Hash della password
     * @param ruolo          Ruolo dell'utente
     * @param dataIscrizione Data di iscrizione
     */
    public User(int id, String username, String password, String ruolo, LocalDateTime dataIscrizione) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.ruolo = ruolo;
        this.dataIscrizione = dataIscrizione;
    }

    /**
     * Costruttore senza l'identificatore unico.
     * Utilizzato solitamente per creare nuovi utenti che devono ancora essere inseriti nel database
     * (dove l'ID verrà generato automaticamente).
     *
     * @param username       Nome utente
     * @param password       Hash della password
     * @param ruolo          Ruolo dell'utente
     * @param dataIscrizione Data di iscrizione
     */
    public User(String username, String password, String ruolo, LocalDateTime dataIscrizione) {
        this.username = username;
        this.password = password;
        this.ruolo = ruolo;
        this.dataIscrizione = dataIscrizione;
    }

    // --- Getter e Setter ---

    /**
     * Restituisce l'ID dell'utente.
     *
     * @return id dell'utente
     */
    public int getId() {
        return id;
    }

    /**
     * Imposta l'ID dell'utente.
     *
     * @param id nuovo ID dell'utente
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Restituisce il nome utente.
     *
     * @return username dell'utente
     */
    public String getUsername() {
        return username;
    }

    /**
     * Imposta il nome utente.
     *
     * @param username nuovo username dell'utente
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Restituisce l'hash della password.
     *
     * @return password dell'utente
     */
    public String getPassword() {
        return password;
    }

    /**
     * Imposta l'hash della password.
     *
     * @param password nuovo hash della password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Restituisce il ruolo dell'utente.
     *
     * @return ruolo dell'utente (es. "admin", "giocatore")
     */
    public String getRuolo() {
        return ruolo;
    }

    /**
     * Imposta il ruolo dell'utente.
     *
     * @param ruolo nuovo ruolo dell'utente
     */
    public void setRuolo(String ruolo) {
        this.ruolo = ruolo;
    }

    /**
     * Restituisce la data di iscrizione dell'utente.
     *
     * @return dataIscrizione dell'utente
     */
    public LocalDateTime getDataIscrizione() {
        return dataIscrizione;
    }

    /**
     * Imposta la data di iscrizione dell'utente.
     *
     * @param dataIscrizione nuova data di iscrizione dell'utente
     */
    public void setDataIscrizione(LocalDateTime dataIscrizione) {
        this.dataIscrizione = dataIscrizione;
    }

    // --- Overriding di toString, equals e hashCode ---

    /**
     * Restituisce una rappresentazione testuale dell'oggetto User.
     *
     * @return stringa che rappresenta l'utente
     */
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", ruolo='" + ruolo + '\'' +
                ", dataIscrizione=" + dataIscrizione +
                '}';
    }

    /**
     * Confronta questo utente con un altro oggetto.
     * Due utenti sono considerati uguali se hanno lo stesso ID e lo stesso username.
     *
     * @param o l'oggetto da confrontare
     * @return true se gli oggetti sono uguali, false altrimenti
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id && Objects.equals(username, user.username);
    }

    /**
     * Restituisce il codice hash per l'utente, basato su ID e username.
     *
     * @return codice hash dell'utente
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, username);
    }
}
