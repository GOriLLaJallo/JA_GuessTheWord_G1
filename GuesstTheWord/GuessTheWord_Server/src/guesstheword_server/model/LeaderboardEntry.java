package guesstheword_server.model;

import java.util.Objects;

/**
 * Rappresenta una voce (riga) all'interno della classifica globale (leaderboard).
 * Contiene i dati aggregati delle statistiche di un utente che ha conseguito almeno una vittoria.
 * 
 * @author Carmine Muollo
 */
public class LeaderboardEntry {

    /** Nome utente del giocatore. */
    private String username;

    /** Numero totale di partite vinte dal giocatore. */
    private int vittorie;

    /** Tempo medio di risposta in millisecondi per le partite vinte. */
    private double tempoMedio;

    /**
     * Costruttore vuoto.
     */
    public LeaderboardEntry() {
    }

    /**
     * Costruttore completo.
     *
     * @param username   Nome utente
     * @param vittorie   Numero totale di vittorie
     * @param tempoMedio Tempo medio di risposta in millisecondi
     */
    public LeaderboardEntry(String username, int vittorie, double tempoMedio) {
        this.username = username;
        this.vittorie = vittorie;
        this.tempoMedio = tempoMedio;
    }

    // --- Getter e Setter ---

    /**
     * Restituisce il nome utente.
     *
     * @return username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Imposta il nome utente.
     *
     * @param username nuovo nome utente
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Restituisce il numero totale di vittorie.
     *
     * @return numero di vittorie
     */
    public int getVittorie() {
        return vittorie;
    }

    /**
     * Imposta il numero di vittorie.
     *
     * @param vittorie nuovo numero di vittorie
     */
    public void setVittorie(int vittorie) {
        this.vittorie = vittorie;
    }

    /**
     * Restituisce il tempo medio di risposta in millisecondi.
     *
     * @return tempo medio
     */
    public double getTempoMedio() {
        return tempoMedio;
    }

    /**
     * Imposta il tempo medio di risposta.
     *
     * @param tempoMedio nuovo tempo medio in millisecondi
     */
    public void setTempoMedio(double tempoMedio) {
        this.tempoMedio = tempoMedio;
    }

    @Override
    public String toString() {
        return "LeaderboardEntry{" +
                "username='" + username + '\'' +
                ", vittorie=" + vittorie +
                ", tempoMedio=" + String.format("%.2f ms", tempoMedio) +
                '}';
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + Objects.hashCode(this.username);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LeaderboardEntry other = (LeaderboardEntry) obj;
        return Objects.equals(this.username, other.username);
    }
    
    
}
