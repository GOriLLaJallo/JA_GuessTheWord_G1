/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package guesstheword_client.network;

import java.io.*;
import java.net.*;
import java.util.Properties;

/**
 *
 * @author Sabrina Soriano
 */
public class ServerConnection {

    private static ServerConnection instance;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    
    /**
     * Costruttore del client che:
     * 1) Legge il file client.properties e ottiene l'indirizzo ip e numero di porta
     * 2) Si connette al server e apre i caneli di comunicazione (in e out)
     * 
     * @throws IOException 
     */
    
    private ServerConnection() throws IOException {
        Properties props = new Properties();
        //props.load(new FileInputStream("client.properties"));
        InputStream input = getClass().getResourceAsStream("/guesstheword_client/resources/properties/client.properties");
        props.load(input);

        String ip = props.getProperty("server.ip");
        int port = Integer.parseInt(props.getProperty("server.port"));

        this.socket = new Socket(ip, port);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
    }
    
    /**
     * metodo che serve a istanziare un nuovo oggetto ServerConnection
     * garantisce che solo un thread alla volta possa eseguire il metodo (synchronized)
     * controlla se esiste già l'istanza e nel caso la riusa
     * 
     * @return
     * @throws IOException 
     */
    
    public static synchronized ServerConnection getInstance() throws IOException {
        if (instance == null || instance.socket.isClosed()) {
            instance = new ServerConnection();
        }
        return instance;
    }
    
    
    /**
     * Manda solo un messaggi sul canale out
     * 
     * @param message 
     */
    public void sendMessage(String message) {
        out.println(message);
    }
    
    /**
     * Metodo da aggiornare per adesso legge un messaggio in arrivo dal server (bloccante)
     * 
     * @return
     * @throws IOException 
     */

    public String receiveMessage() throws IOException {
        return in.readLine();
    }
    
    /**
     * Chiude la connessione con il server
     * 
     * @throws IOException 
     */

    public void close() throws IOException {
        socket.close();
    }
}
