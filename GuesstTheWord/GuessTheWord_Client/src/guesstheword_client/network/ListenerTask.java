/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package guesstheword_client.network;

import javafx.concurrent.Task;

/**
 *
 * @author Pc
 */
public class ListenerTask extends Task<Void> {

    private ServerConnection connection;

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
     * Principio di funzionamento: l'app non si blocca per la classe ListenerTask estende Task che le permette di runnare in background
     * 
     * @return
     * @throws Exception 
     */
    
    @Override
    protected Void call() throws Exception {
        String messaggio;
        while ((messaggio = connection.receiveMessage()) != null) {
            updateMessage(messaggio);
        }
        return null;
    }
}