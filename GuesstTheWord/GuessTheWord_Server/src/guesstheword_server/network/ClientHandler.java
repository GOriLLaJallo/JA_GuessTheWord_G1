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
    private ClientRegistry registry;

    /**
     * Costruttore che prende la socket e il registro dei client.
     * 
     * @param socket la socket del client
     * @param registry il registro dei client connessi
     */
    public ClientHandler(Socket socket, ClientRegistry registry) {
        this.socket = socket;
        this.registry = registry;
    }

    /**
     * Restituisce la socket associata a questo handler.
     *
     * @return la socket
     */
    public Socket getSocket() {
        return socket;
    }
    
    /**
     * Invia un messaggio al client
     *
     * @param message
     */
    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        } else {
            System.err.println("[ClientHandler] Errore: PrintWriter non inizializzato. Impossibile inviare: " + message);
        }
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
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new PrintWriter(socket.getOutputStream(), true);
            System.out.println("[ClientHandler] Handler avviato per: " + socket.getInetAddress());
            
            // Imposta timeout fisso e breve per il polling
            socket.setSoTimeout(2000);
            
            long lastActivityTimestamp = System.currentTimeMillis();

            while (true) {
                String messaggio = null;
                try {
                    messaggio = in.readLine();
                } catch (java.net.SocketTimeoutException e) {
                    // Controlla se la soglia di inattività per lo stato attuale è stata superata
                    long elapsed = System.currentTimeMillis() - lastActivityTimestamp;
                    if (elapsed > getInactivityThreshold()) {
                        System.out.println("[ClientHandler] Inattività superata (" + elapsed + " ms) per " + socket.getInetAddress() + ". Disconnessione.");
                        break;
                    }
                    continue;
                }

                if (messaggio == null) {
                    System.out.println("[ClientHandler] Connessione chiusa ordinatamente dal client: " + socket.getInetAddress());
                    break;
                }

                // Ricezione di dati con successo
                lastActivityTimestamp = System.currentTimeMillis();
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
            System.out.println("[ClientHandler] Client disconnesso o errore I/O: " + socket.getInetAddress() + " - " + e.getMessage());
        } 
        finally {
            if (registry != null) {
                registry.unregister(this);
            }
            handleClientDisconnection();
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                System.err.println("[ClientHandler] Errore nella chiusura della socket: " + e.getMessage());
            }
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
        if (parts.length < 1 || parts.length > 2) {
            sendMessage(MessageProtocol.build(MessageProtocol.AUTH_FAIL, "Formato comando WAITING non valido."));
            return;
        }
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
    
        StringBuilder sb = new StringBuilder();
        history.forEach(result -> {
            sb.append(result.getSfida().getDataSfida())
                .append(",")
                .append(result.getEsito())
                .append(",")
                .append(result.getSfida().getParolaNascosta())
                .append(",")
                .append(result.getSfida().getDifficolta() != null 
                    ? result.getSfida().getDifficolta() : "N/D")
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

    public GameSession getCurrentSession() {
        return currentSession;
    }

    private boolean isSessionActive() {
        return currentSession != null && !currentSession.isFinished();
    }

    private long getInactivityThreshold() {
        if (isSessionActive()) {
            // (DEFAULT_TIMER_SECONDS + 30) * 1000 -> 90000 ms
            return (GameSession.DEFAULT_TIMER_SECONDS + 30) * 1000L;
        }
        return Long.MAX_VALUE; // Nessun limite di inattività al di fuori della partita attiva
    }
}