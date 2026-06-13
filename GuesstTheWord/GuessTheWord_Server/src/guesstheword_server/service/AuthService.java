package guesstheword_server.service;

import guesstheword_server.db.UserDAO;
import guesstheword_server.model.User;
import guesstheword_server.utils.HashUtil;
import java.time.LocalDateTime;

/**
 * Servizio per la gestione dell'autenticazione e della registrazione degli utenti.
 * Si occupa di applicare le politiche di sicurezza (cifratura delle password tramite SHA-256)
 * e delega la persistenza a UserDAO.
 * 
 * @author Carmine Muollo
 */
public class AuthService {

    private final UserDAO userDAO;

    /**
     * Costruisce una nuova istanza di AuthService con un UserDAO predefinito.
     */
    public AuthService() {
        this.userDAO = new UserDAO();
    }

    /**
     * Costruisce una nuova istanza di AuthService con un UserDAO personalizzato.
     * 
     * @param userDAO il DAO da utilizzare
     */
    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Esegue il login dell'utente confrontando username e password (previa cifratura).
     * 
     * @param username il nome utente inserito
     * @param password la password in chiaro inserita
     * @return l'oggetto User autenticato se le credenziali sono valide, null altrimenti
     */
    public User login(String username, String password) {
        if (password == null) {
            return null;
        }
        String passwordHash = HashUtil.sha256(password);
        return userDAO.authenticate(username, passwordHash);
    }

    /**
     * Registra un nuovo utente nel sistema cifrando la password fornita.
     * 
     * @param username il nome utente per la registrazione
     * @param password la password in chiaro per la registrazione
     * @param ruolo il ruolo dell'utente (es. "giocatore")
     * @return true se la registrazione è andata a buon fine, false altrimenti
     */
    public boolean register(String username, String password, String ruolo) {
        if (username == null || password == null || ruolo == null) {
            return false;
        }
        String passwordHash = HashUtil.sha256(password);
        User user = new User(username, passwordHash, ruolo, LocalDateTime.now());
        return userDAO.register(user);
    }
}
