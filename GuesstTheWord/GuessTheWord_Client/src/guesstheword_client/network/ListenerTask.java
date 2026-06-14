/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package guesstheword_client.network;

import javafx.concurrent.Task;

/**
 *
 * @author Sabrina Soriano
 */
public class ListenerTask extends Task<Void> {

    private final ServerConnection connection;

    /**
     * Il costruttore ha il solo compito di ricevere la ServerConnection (già aperta) e salvarla.
     * 
     * @param connection 
     */
    public ListenerTask(ServerConnection connection) {
        this.connection = connection;
    }
    
    /**
     * Metodo che serve a ricevere i messaggi senza che il metodo bloccante (receiveMessage) blocchi l'app. 
     * Utilizza updateMessage per notificare la property in modo thread-safe.
     */
    @Override
    protected Void call() throws Exception {
        String message;
        while ((message = connection.receiveMessage()) != null) {
            // Imposta a null e poi al messaggio ricevuto per forzare l'attivazione
            // dei listener anche in caso di messaggi identici consecutivi.
            updateMessage(null);
            updateMessage(message);
        }
        return null;
    }
}