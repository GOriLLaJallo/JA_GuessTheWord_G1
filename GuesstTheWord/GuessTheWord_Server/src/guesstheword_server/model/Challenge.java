package guesstheword_server.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Rappresenta un'istanza di una sfida di gioco (partita) all'interno del sistema "GuessTheWord".
 * Mantiene lo stato della parola originaria in chiaro, il valore di shift applicato 
 * secondo il cifrario di Cesare, la data della sfida e il livello di difficoltà.
 * Le istanze di questa classe vengono popolate dal layer di persistenza ed elaborate 
 * dal server per la distribuzione dei testi cifrati ai client connessi.
 * @author Carmine Muollo
 */
public class Challenge {

    /** Identificatore unico della sfida nel database. */
    private int id;

    /** La parola originale in chiaro che gli utenti devono indovinare. */
    private String parolaNascosta;

    /** Il valore dello spostamento (shift) utilizzato nel cifrario di Cesare. */
    private int shiftCesare;

    /** Data e ora di inizio della sfida. */
    private LocalDateTime dataSfida;

    /** La difficoltà della sfida ("EASY", "MEDIUM", "HARD"). */
    private String difficolta;
    
    /** L'estratto di testo contenente la parola nascosta nel suo contesto. */
    private String estrattoTesto;

    /**
     * Costruttore di default. Necessario per le operazioni di mapping e per i framework 
     * di serializzazione e reflection.
     */
    public Challenge() {
    }

    /**
     * Costruttore completo per istanziare una sfida con tutti i suoi attributi.
     * Utilizzato quando si recupera una sfida esistente dal database.
     *
     * @param id             Identificatore unico della sfida
     * @param parolaNascosta La parola segreta in chiaro
     * @param shiftCesare    Il valore dello shift di Cesare
     * @param dataSfida      Data e ora della sfida
     * @param difficolta     La difficoltà della sfida
     */
    public Challenge(int id, String parolaNascosta, int shiftCesare, LocalDateTime dataSfida, String difficolta) {
        this.id = id;
        this.parolaNascosta = parolaNascosta;
        this.shiftCesare = shiftCesare;
        this.dataSfida = dataSfida;
        this.difficolta = difficolta;
    }

    /**
     * Costruttore senza l'identificatore unico.
     * Utilizzato per creare nuove sfide prima che vengano memorizzate nel database.
     *
     * @param parolaNascosta La parola segreta in chiaro
     * @param shiftCesare    Il valore dello shift di Cesare
     * @param dataSfida      Data e ora della sfida
     * @param difficolta     La difficoltà della sfida
     */
    public Challenge(String parolaNascosta, int shiftCesare, LocalDateTime dataSfida, String difficolta) {
        this.parolaNascosta = parolaNascosta;
        this.shiftCesare = shiftCesare;
        this.dataSfida = dataSfida;
        this.difficolta = difficolta;
    }

    // --- Getter e Setter ---

    /**
     * Restituisce l'ID della sfida.
     *
     * @return id della sfida
     */
    public int getId() {
        return id;
    }

    /**
     * Imposta l'ID della sfida.
     *
     * @param id nuovo ID della sfida
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Restituisce la parola nascosta in chiaro.
     *
     * @return parolaNascosta
     */
    public String getParolaNascosta() {
        return parolaNascosta;
    }

    /**
     * Imposta la parola nascosta in chiaro.
     *
     * @param parolaNascosta nuova parola nascosta
     */
    public void setParolaNascosta(String parolaNascosta) {
        this.parolaNascosta = parolaNascosta;
    }

    /**
     * Restituisce il valore dello shift di Cesare.
     *
     * @return shiftCesare
     */
    public int getShiftCesare() {
        return shiftCesare;
    }

    /**
     * Imposta il valore dello shift di Cesare.
     *
     * @param shiftCesare nuovo valore dello shift
     */
    public void setShiftCesare(int shiftCesare) {
        this.shiftCesare = shiftCesare;
    }

    /**
     * Restituisce la data della sfida.
     *
     * @return dataSfida
     */
    public LocalDateTime getDataSfida() {
        return dataSfida;
    }

    /**
     * Imposta la data della sfida.
     *
     * @param dataSfida nuova data della sfida
     */
    public void setDataSfida(LocalDateTime dataSfida) {
        this.dataSfida = dataSfida;
    }

    /**
     * Restituisce la difficoltà della sfida.
     *
     * @return difficolta ("EASY", "MEDIUM", "HARD")
     */
    public String getDifficolta() {
        return difficolta;
    }

    /**
     * Imposta la difficoltà della sfida.
     *
     * @param difficolta nuova difficoltà
     */
    public void setDifficolta(String difficolta) {
        this.difficolta = difficolta;
    }
    
    /**
    * Restituisce l'estratto di testo della sfida.
    *
    * @return estrattoTesto
    */
    public String getEstratto() {
        return estrattoTesto;
    }

    /**
    * Imposta l'estratto di testo della sfida.
    *
    * @param estrattoTesto l'estratto di testo
    */
    public void setEstratto(String estrattoTesto) {
        this.estrattoTesto = estrattoTesto;
    }

    // --- Overriding di toString, equals e hashCode ---

    /**
     * Restituisce una rappresentazione testuale dell'oggetto Challenge.
     *
     * @return stringa che rappresenta la sfida
     */
    @Override
    public String toString() {
        return "Challenge{" +
                "id=" + id +
                ", parolaNascosta='" + parolaNascosta + '\'' +
                ", shiftCesare=" + shiftCesare +
                ", dataSfida=" + dataSfida +
                ", difficolta='" + difficolta + '\'' +
                '}';
    }

   /**
     * Confronta l'uguaglianza logica tra questa sfida con un altro oggetto.
     * Due sfide sono considerate equivalenti se e solo se condividono il medesimo ID, 
     * lo stesso valore di shift e la medesima parola nascosta.
     * @param o l'oggetto da confrontare con l'istanza corrente
     * @return true se gli oggetti sono logicamente equivalenti, false altrimenti
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Challenge challenge = (Challenge) o;
        return id == challenge.id && shiftCesare == challenge.shiftCesare && Objects.equals(parolaNascosta, challenge.parolaNascosta);
    }

    /**
     * Restituisce il codice hash per la sfida.
     *
     * @return codice hash della sfida
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, parolaNascosta, shiftCesare);
    }
}
