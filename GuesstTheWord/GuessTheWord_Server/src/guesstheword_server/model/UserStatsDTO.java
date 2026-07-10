package guesstheword_server.model;

/**
 * Data Transfer Object (DTO) formale impiegato per incapsulare e veicolare
 * le metriche statistiche aggregate associate a un profilo utente specifico.
 * Questa struttura dati centralizza i dati storici consolidati relativi al computo 
 * delle vittorie, al volume complessivo delle partite giocate e al tempo medio di risposta 
 * calcolato in millisecondi. Viene istanziata dal layer di persistenza (DAO) mediante 
 * query di aggregazione sul database e trasferita verso i client.
 * 
 * @author Carmine Muollo
 */
public class UserStatsDTO {
    
    /** il numero di partite vinte 
        dall'utente all'interno del sistema. 
     */
    private int victories;

    /** Il numero complessivo di partite giocate dall'utente, 
     *  indipendentemente dall'esito finale (vittoria, sconfitta o timeout). 
     */
    private int gamesPlayed;

    /** La media aritmetica del tempo di risposta calcolato sulle risposte inviate dall'utente, 
     *  espressa rigorosamente in millisecondi (ms). 
     */
    private double averageResponseTime;

    /**
     * Costruttore completo e canonico del DTO. Inizializza l'oggetto impostando contestualmente
     * l'intero set di metriche statistiche derivate dalla base di dati.
     * @param victories           il numero totale di sfide vinte dall'utente
     * @param gamesPlayed         il numero complessivo di partite giocate dall'utente
     * @param averageResponseTime il tempo medio di risposta calcolato, espresso in millisecondi (ms)
     */
    public UserStatsDTO(int victories, int gamesPlayed, double averageResponseTime) {
        this.victories = victories;
        this.gamesPlayed = gamesPlayed;
        this.averageResponseTime = averageResponseTime;
    }

    /**
     * Interroga il DTO per ottenere il numero totale di vittorie conseguite dal giocatore.
     * * @return un valore intero rappresentante il conteggio assoluto delle vittorie
     */
    public int getVictories() {
        return victories;
    }

    /**
     * Interroga il DTO per ottenere il volume complessivo delle partite a cui l'utente ha partecipato.
     * * @return il numero totale di match registrati nello storico del giocatore
     */
    public int getGamesPlayed() {
        return gamesPlayed;
    }

    /**
     * Restituisce il valore statistico medio associato al tempo di reazione dell'utente 
     * per la sottomissione delle risposte.
     * * @return il tempo medio di risposta espresso come primitiva {@code double} in millisecondi
     */
    public double getAverageResponseTime() {
        return averageResponseTime;
    }

    /**
     * Genera una proiezione testuale formattata dello stato interno del DTO.
     * Metodo sovrascritto a supporto delle attività di tracciamento e logging strutturato 
     * all'interno del layer server.
     * @return una stringa conforme contenente le etichette dei campi e i rispettivi valori correnti
     */
    @Override
    public String toString() {
        return "UserStatsDTO{" +
                "victories=" + victories +
                ", gamesPlayed=" + gamesPlayed +
                ", averageResponseTime=" + averageResponseTime +
                '}';
    }
}