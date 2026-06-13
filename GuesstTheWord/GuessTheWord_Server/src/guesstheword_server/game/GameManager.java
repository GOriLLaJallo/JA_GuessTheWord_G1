/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package guesstheword_server.game;

import guesstheword_server.model.Challenge;
import guesstheword_server.network.ClientHandler;
import guesstheword_server.protocol.MessageProtocol;
import java.util.*;

/**
 * Gestore delle sessioni di gioco attive (Singleton).
 *
 * @author Davide Odierna, Sabrina Soriano
 */
public class GameManager {

    private static GameManager instance;
    private final List<GameSession> activeSessions = new ArrayList<>();
   private final Map<Difficulty, List<ClientHandler>> waitingPlayers = new HashMap<>(); //coda dei giocatori in attesa accoppiati alla difficoltà scelta
    private final ChallengePreparator challengePreparator = new ChallengePreparator(); //oggetto per generare la Challenge

    /**
     * Unico scopo inizializzazione della Map con una cosa vuota per ogni difficoltà
     */
    
    private GameManager() {
        for (Difficulty d : Difficulty.values()) {
            waitingPlayers.put(d, new ArrayList<>());
        }
    }

    /**
     * Restituisce l'unica istanza di GameManager.
     *
     * @return istanza singleton
     */
    public static synchronized GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }

    /**
     * Aggiunge una sessione di gioco alla lista delle sessioni attive.
     *
     * @param session la sessione da aggiungere
     */
    public synchronized void addSession(GameSession session) {
        activeSessions.add(session);
    }

    /**
     * Rimuove una sessione terminata dalla lista delle sessioni attive.
     *
     * @param session la sessione da rimuovere
     */
    public synchronized void removeSession(GameSession session) {
        activeSessions.remove(session);
    }

    /**
     * Restituisce la lista delle sessioni attive (copia difensiva).
     *
     * @return lista di sessioni attive
     */
    public synchronized List<GameSession> getActiveSessions() {
        return new ArrayList<>(activeSessions);
    }
    
    /**
     * Gestisce la parte subito precedente alla sfida
     * 0) Recupera la coda giusta a seconda della difficoltà
     * Considera 2 casi:
     * 1) nessun giocatore in attesa per quella specifica difficoltà -> il giocatore viene messo in coda in attesa che arrivi un avversario
     * 2) c'è un giocatore in coda -> estrae il giocatore dalla lista waitingPlayers e inizia a preparare la sfida
     * Generazione sfida per adesso la difficoltà è media da cambiare
     * Generazione GameSession e poi collega i 2 avversarsari alla stessa GameSession
     * Notifica avversari e inzio partita
     *  
     * @param player
     */
    
    public synchronized void addToLobby(ClientHandler player, Difficulty difficulty) {
        List<ClientHandler> queue = waitingPlayers.get(difficulty);
        
        if (queue.isEmpty()) {
            queue.add(player);
            System.out.println("[GameManager] " + player.getUsername() + " in attesa (" + difficulty + ").");
        }
        else {
            ClientHandler opponent = queue.remove(0);

            Challenge challenge = challengePreparator.prepareRandom(difficulty);
        
            GameSession session = new GameSession(opponent, player, challenge);
            addSession(session);

            opponent.setCurrentSession(session);
            player.setCurrentSession(session);

            opponent.sendMessage(MessageProtocol.build(MessageProtocol.OPPONENT_FOUND));
            player.sendMessage(MessageProtocol.build(MessageProtocol.OPPONENT_FOUND));
        
            session.start();
        }
    }
}

