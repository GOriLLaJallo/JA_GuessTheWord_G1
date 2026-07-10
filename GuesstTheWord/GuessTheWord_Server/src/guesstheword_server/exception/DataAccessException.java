package guesstheword_server.exception;

/**
 * Eccezione non controllata (Runtime) utilizzata per incapsulare e propagare
 * gli errori infrastrutturali del database SQLite (come SQLException), offrendo
 * un feedback chiaro senza forzare la firma dei metodi con eccezioni
 * controllate.
 *
 * @author Carmine Muollo
 */
public class DataAccessException extends RuntimeException {

    /**
     * Costruisce una nuova DataAccessException con un messaggio descrittivo.
     *
     * @param message il messaggio descrittivo dell'errore
     */
    public DataAccessException(String message) {
        super(message);
    }

    /**
     * Costruisce una nuova DataAccessException con un messaggio descrittivo e
     * la causa sottostante.
     *
     * @param message il messaggio descrittivo dell'errore
     * @param cause la causa dell'errore (solitamente una SQLException)
     */
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
