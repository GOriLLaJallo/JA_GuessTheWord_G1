/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package guesstheword_server.network;

import java.io.*;
import java.net.*;
import java.util.Properties;

/**
 *
 * @author Sabrina Soriano
 */

public class GameServer {

    private ServerSocket serverSocket;
    private int port;
    
    /**
     * Costruttore del server, legge la porta dal file di configurazione e la converte da stringa e intero
     * 
     * @throws IOException 
     */

    public GameServer() throws IOException {
        Properties props = new Properties();
        //props.load(new FileInputStream("server.properties"));
        InputStream input = getClass().getResourceAsStream("/guesstheword_server/resources/properties/server.properties");
        props.load(input);
        port = Integer.parseInt(props.getProperty("server.port"));
    }
    
    /**
     * Questo metodo: 
     * 1) apre il ServerSocket (da questo momento i client possono connettersi)
     * 2) entra in un loop infinito che aspetta che un client si connette, crea un clientHandler e lo avvia in un thread separato per essere in grado di gestire il client successivo
     * 
     * @throws IOException 
     */
    public void startCon() throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server avviato sulla porta " + port);

        while (true) {
            Socket clientSocket = serverSocket.accept(); // bloccante
            System.out.println("Client connesso: " + clientSocket.getInetAddress());

            Thread t = new Thread(new ClientHandler(clientSocket));
            t.start();
        }
    }

    public static void main(String[] args) throws IOException {
        new GameServer().startCon();
    }

    
}