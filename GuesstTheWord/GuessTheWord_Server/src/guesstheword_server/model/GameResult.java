package guesstheword_server.model;

import java.util.Objects;

/**
 * Rappresenta l'esito di una sfida per un singolo partecipante.
 * Contiene i riferimenti all'utente (giocatore) e alla sfida affrontata,
 * l'esito (vittoria, sconfitta, timeout), l'eventuale risposta inviata e il tempo di risposta.
 * 
 * @author Carmine Muollo
 */
public class GameResult {

    /** Identificatore unico del risultato nel database. */
    private int id;

    /** L'utente (giocatore) a cui fa riferimento questo risultato. */
    private User utente;

    /** La sfida a cui fa riferimento questo risultato. */
    private Challenge sfida;

    /** L'esito della sfida per questo giocatore (valori discreti: "WIN", "LOSE", "TIMEOUT"). */
    private String esito;

    /** La risposta proposta e inviata dal giocatore (può essere null in caso di TIMEOUT). */
    private String rispostaInviata;

    /** Tempo impiegato per rispondere espresso in millisecondi (può essere null in caso di TIMEOUT). */
    private Integer tempoRisposta;

    /**
     * Costruttore di default. Necessario per le operazioni di mapping e per i framework 
     * di serializzazione e reflection.
     */
    public GameResult() {
    }

    /**
     * Costruttore completo per istanziare un risultato di gioco con tutti i suoi attributi.
     * Utilizzato quando si recupera un risultato esistente dal database.
     *
     * @param id              Identificatore unico del risultato
     * @param utente          L'utente che ha partecipato
     * @param sfida           La sfida giocata
     * @param esito           L'esito conseguito ("WIN", "LOSE", "TIMEOUT")
     * @param rispostaInviata La risposta inserita dal giocatore (può essere null)
     * @param tempoRisposta   Il tempo di risposta in millisecondi (può essere null)
     */
    public GameResult(int id, User utente, Challenge sfida, String esito, String rispostaInviata, Integer tempoRisposta) {
        this.id = id;
        this.utente = utente;
        this.sfida = sfida;
        this.esito = esito;
        this.rispostaInviata = rispostaInviata;
        this.tempoRisposta = tempoRisposta;
    }

    /**
     * Costruttore senza l'identificatore unico.
     * Utilizzato per creare nuovi record di risultato prima di salvarli nel database.
     *
     * @param utente          L'utente che ha partecipato
     * @param sfida           La sfida giocata
     * @param esito           L'esito conseguito ("WIN", "LOSE", "TIMEOUT")
     * @param rispostaInviata La risposta inserita dal giocatore (può essere null)
     * @param tempoRisposta   Il tempo di risposta in millisecondi (può essere null)
     */
    public GameResult(User utente, Challenge sfida, String esito, String rispostaInviata, Integer tempoRisposta) {
        this.utente = utente;
        this.sfida = sfida;
        this.esito = esito;
        this.rispostaInviata = rispostaInviata;
        this.tempoRisposta = tempoRisposta;
    }

    // --- Getter e Setter ---

    /**
     * Restituisce l'ID del risultato.
     *
     * @return id del risultato
     */
    public int getId() {
        return id;
    }

    /**
     * Imposta l'ID del risultato.
     *
     * @param id nuovo ID del risultato
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Restituisce il giocatore associato.
     *
     * @return utente (User)
     */
    public User getUtente() {
        return utente;
    }

    /**
     * Imposta il giocatore associato.
     *
     * @param utente nuovo utente
     */
    public void setUtente(User utente) {
        this.utente = utente;
    }

    /**
     * Restituisce la sfida associata.
     *
     * @return sfida (Challenge)
     */
    public Challenge getSfida() {
        return sfida;
    }

    /**
     * Imposta la sfida associata.
     *
     * @param sfida nuova sfida
     */
    public void setSfida(Challenge sfida) {
        this.sfida = sfida;
    }

    /**
     * Restituisce l'esito del gioco.
     *
     * @return esito ("WIN", "LOSE", "TIMEOUT")
     */
    public String getEsito() {
        return esito;
    }

    /**
     * Imposta l'esito del gioco.
     *
     * @param esito nuovo esito
     */
    public void setEsito(String esito) {
        this.esito = esito;
    }

    /**
     * Restituisce la risposta inviata dal giocatore.
     *
     * @return rispostaInviata (può essere null)
     */
    public String getRispostaInviata() {
        return rispostaInviata;
    }

    /**
     * Imposta la risposta inviata dal giocatore.
     *
     * @param rispostaInviata nuova risposta (può essere null)
     */
    public void setRispostaInviata(String rispostaInviata) {
        this.rispostaInviata = rispostaInviata;
    }

    /**
     * Restituisce il tempo impiegato per rispondere in millisecondi.
     *
     * @return tempoRisposta (può essere null)
     */
    public Integer getTempoRisposta() {
        return tempoRisposta;
    }

    /**
     * Imposta il tempo impiegato per rispondere in millisecondi.
     *
     * @param tempoRisposta nuovo tempo di risposta (può essere null)
     */
    public void setTempoRisposta(Integer tempoRisposta) {
        this.tempoRisposta = tempoRisposta;
    }

    // --- Overriding di toString, equals e hashCode ---

    /**
     * Restituisce una rappresentazione testuale del GameResult.
     *
     * @return stringa che descrive il risultato del gioco
     */
    @Override
    public String toString() {
        return "GameResult{" +
                "id=" + id +
                ", utente=" + (utente != null ? utente.getUsername() : "null") +
                ", sfida=" + (sfida != null ? sfida.getId() : "null") +
                ", esito='" + esito + '\'' +
                ", rispostaInviata='" + rispostaInviata + '\'' +
                ", tempoRisposta=" + tempoRisposta +
                '}';
    }

    /**
     * Confronta questo risultato con un altro oggetto.
     * Due risultati sono considerati uguali se hanno lo stesso ID, lo stesso utente e la stessa sfida.
     *
     * @param o l'oggetto da confrontare
     * @return true se i risultati sono uguali, false altrimenti
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameResult that = (GameResult) o;
        return id == that.id &&
                Objects.equals(utente, that.utente) &&
                Objects.equals(sfida, that.sfida);
    }

    /**
     * Restituisce il codice hash per il risultato di gioco.
     *
     * @return codice hash
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, utente, sfida);
    }
}
