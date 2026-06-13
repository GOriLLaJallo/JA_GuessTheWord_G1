/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package guesstheword_server.game;

import java.util.ArrayList;
import java.util.List;

/**
 * Gestore delle sessioni di gioco attive (Singleton).
 *
 * @author Pc
 */
public class GameManager {

    private static GameManager instance;
    private final List<GameSession> activeSessions = new ArrayList<>();

    private GameManager() {}

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
}

