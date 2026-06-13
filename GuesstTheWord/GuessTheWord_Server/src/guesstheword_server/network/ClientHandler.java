/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package guesstheword_server.network;

import guesstheword_server.db.ResultDAO;
import guesstheword_server.db.UserDAO;
import guesstheword_server.game.Difficulty;
import guesstheword_server.game.GameManager;
import guesstheword_server.game.GameSession;
import guesstheword_server.model.User;
import guesstheword_server.protocol.MessageProtocol;
import guesstheword_server.model.GameResult;
import java.io.*;
import java.net.*;
import java.util.List;
import java.time.LocalDateTime;

/**
 * 
 * @author Sabrina Soriano
 */

public class ClientHandler implements Runnable {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String username;
    private User user;
    private GameSession currentSession;

    /**
     * Costruttore che prende la socket e ci apre 2 canali di comunicazione (in e out) tra client e server
     * 
     * @param socket
     * @throws IOException 
     */
    public ClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true); // autoflush
    }
    
    /**
     * Invia un messaggio al client
     *
     * @param message
     */
    public void sendMessage(String message) {
        out.println(message);
    }
    
    /**
     * Restituisce lo username del client autenticato
     *
     * @return username
     */
    public String getUsername() {
        return username;
    }
    
    /**
     * Restituisce l'oggetto User del client autenticato
     *
     * @return user
     */
    public User getUser() {
        return user;
    }

    /**
     * Loop principale del thread: legge i messaggi del client e li smista in base al comando del protocollo.
     * Idea di funzionamento:
     * 0) Un messaggio di debug per sapere dal log del server quale client si è connesso. System.out.println("[ClientHandler] Handler avviato per: " + socket.getInetAddress()); (Nel caso viene eliminato)
     * 1) Il thread si ferma e aspetta un messaggio
     * 2) Il messaggio viene diviso nelle sue parti (separate da ":")
     * 3) part[0] è sempre il comando
     * 4) Se il messaggio è null allora il client ha chiuso la connessione
     */
    @Override
    public void run() {
        try {
            System.out.println("[ClientHandler] Handler avviato per: " + socket.getInetAddress());
            String messaggio;
            while ((messaggio = in.readLine()) != null) {
                System.out.println("[ClientHandler] Ricevuto: " + messaggio);
                String[] parts = MessageProtocol.parse(messaggio);
                String command = parts[0];

                switch (command) {
                    case MessageProtocol.AUTH_LOGIN:
                        handleLogin(parts);
                        break;
                    case MessageProtocol.AUTH_REGISTER:
                        handleRegister(parts);
                        break;
                    case MessageProtocol.WAITING:
                        handleWaiting(parts);
                        break;
                    case MessageProtocol.GAME_ANSWER:
                        handleAnswer(parts);
                        break;
                    case MessageProtocol.REQ_HISTORY:
                        handleHistory();
                        break;
                    default:
                        System.out.println("[ClientHandler] Comando sconosciuto: " + command);
                        break;
                }
            }
        } 
        catch (IOException e) {
            System.out.println("[ClientHandler] Client disconnesso: " + socket.getInetAddress());
        } 
        finally {
            handleClientDisconnection();
            try { socket.close(); } catch (IOException e) { e.printStackTrace(); }
        }
        
        /**
         * Gestisce il messaggio di login
         * Principio di funzionamento:
         * 1) part[1] = username; part[2] = password
         * 2) Controlliamo le credenziali con "authenticate"
         * 3) Mandiamo al client un messaggio di OK in caso di autenticazione corretta o Errore in caso contrario
         * 
         * 
         * @param parts 
         */
        
    }
    private void handleLogin(String[] parts) {
        if (parts.length < 3) {
            sendMessage(MessageProtocol.build(MessageProtocol.AUTH_FAIL, "Formato messaggio non valido."));
            return;
        }
        String name = parts[1];
        String password = parts[2];
        try {
            UserDAO userDAO = new UserDAO();
            User authenticated = userDAO.authenticate(name, password);
            if (authenticated != null) {
                this.user = authenticated;
                this.username = authenticated.getUsername();
                sendMessage(MessageProtocol.build(MessageProtocol.AUTH_OK, username));
                System.out.println("[ClientHandler] Login OK: " + username);
            } else {
                sendMessage(MessageProtocol.build(MessageProtocol.AUTH_FAIL, "Credenziali non valide."));
            }
        } 
        catch (Exception e) {
            sendMessage(MessageProtocol.build(MessageProtocol.AUTH_FAIL, "Errore interno del server."));
            System.err.println("[ClientHandler] Errore login: " + e.getMessage());
        }
    }
    /**
     * 0) Controllare se l'username è già presente nel database e se nella password è presente il carattere speciale ":" -> messaggio Errore
     * 1) L'user può essere effettivamente registrato -> register
     * 
     * 
     * @param parts
     */
    
    private void handleRegister(String[] parts) {
        if (parts.length < 3) {
            sendMessage(MessageProtocol.build(MessageProtocol.AUTH_FAIL, "Formato messaggio non valido."));
            return;
        }
        String name = parts[1];
        String password = parts[2];
        UserDAO userDAO = new UserDAO();

        if (userDAO.findByUsername(name) != null) {
            sendMessage(MessageProtocol.build(MessageProtocol.AUTH_FAIL, "Username già in uso."));
            return;
        }
        

        User newUser = new User(name, password, "giocatore", LocalDateTime.now());
        boolean ok = userDAO.register(newUser);
        if (ok) {
            this.user = newUser;
            this.username = newUser.getUsername();
            sendMessage(MessageProtocol.build(MessageProtocol.AUTH_OK, username));
            System.out.println("[ClientHandler] Registrazione OK: " + username);
        } 
        else {
            sendMessage(MessageProtocol.build(MessageProtocol.AUTH_FAIL, "Errore durante la registrazione."));
        }
    }
    
    /**
     * Il server ha ricevuto un WAITING dal client; risponde con un WAITING per confermare al client che è in coda.
     * Poi controlla la lobby se non c'è nessuno allora mette il client in attesa, se c'è già un client li accoppia (principio gestito dal metodo addToLobby)
     * Gestendo le varie difficoltà (MEDIUM default)
     * 
     */
    
    private void handleWaiting(String[] parts) {
        Difficulty difficulty = Difficulty.MEDIUM;
        
        if (parts.length >= 2) {
            try {
                difficulty = Difficulty.valueOf(parts[1].toUpperCase());
            } catch (IllegalArgumentException e) {
            // valore non valido, resta MEDIUM
            }
        }
        
        sendMessage(MessageProtocol.build(MessageProtocol.WAITING));
        GameManager.getInstance().addToLobby(this, difficulty);
    }
    
    /**
     * Invia la guess parts[1] del giocatore "this" al metodo che controlla se è corretta
     * 
     * @param parts
     */
    
    private void handleAnswer(String[] parts) {
    if (parts.length < 2 || currentSession == null) return;
        String guess = parts[1];
        currentSession.handleAnswer(this, guess);
    }
    
    /**
     * Recupera lo storico di un utente
     * Principio di funzionamento:
     * 0) L'utente deve essere autenticato
     * 1) Recuperiamo lo storico dal Data Base e controlliamo che non sia empty
     * 2) Lo storico viene diviso in una serie di stringhe (formato -> data, esito, parola), ogni partita è separata dal carattere ";"
     * 3) Finite le sfide vengono mandate tutte con un unico messaggio all'utente
     * 
     */
    
    private void handleHistory() {
        if (user == null) {
            sendMessage(MessageProtocol.build(MessageProtocol.AUTH_FAIL, "Non autenticato."));
            return;
        }
        ResultDAO resultDAO = new ResultDAO();
        List<GameResult> history = resultDAO.getHistoryByUserId(user.getId());
    
        if (history.isEmpty()) {
            sendMessage(MessageProtocol.build(MessageProtocol.HISTORY_DATA, "Nessuna partita giocata."));
            return;
        }
    
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH.mm");
        StringBuilder sb = new StringBuilder();
        history.forEach(result -> {
            sb.append(result.getSfida().getDataSfida().format(formatter))
                    .append(",")
                    .append(result.getEsito())
                    .append(",")
                    .append(result.getSfida().getParolaNascosta())
                    .append(",")
                    .append(result.getSfida().getDifficolta() != null ? result.getSfida().getDifficolta() : "N/D")
                    .append(";");
        });
        sendMessage(MessageProtocol.build(MessageProtocol.HISTORY_DATA, sb.toString()));
    }
    
    /**
     * Controlla che la sessione corrente non sia nulla e non finita prima di delegare al metodo handleDisconnection la notifica della disconnessione dell'avversario
     */
    
    private void handleClientDisconnection() {
        if (currentSession != null && !currentSession.isFinished()) {
            currentSession.handleDisconnection(this);
        }
    }
    
    /**
     * 
     * 
     * @param session
     */
    
    public void setCurrentSession(GameSession session) {
        this.currentSession = session;
    }
}