/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package guesstheword_server.network;


import java.io.*;
import java.net.*;

/**
 * 
 * @author Pc
 */

public class ClientHandler implements Runnable {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    /**
     * Costruttore che prende la socket e ci apre 2 canali di comunicazione (in e out) tra client e server
     * 
     * @param socket
     * @throws IOException 
     */
    public ClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true); // autoflush
    }

    /**
     * Per adesso è un segnaposto non fa nient'altro che stampare i messaggi che il client riceve, poi verrà modificato con la logica del gioco
     */
    
    @Override
    public void run() {
        try {
            System.out.println("Handler avviato per: " + socket.getInetAddress());

            // per ora legge e risponde con echo
            String messaggio;
            while ((messaggio = in.readLine()) != null) {
                System.out.println("Ricevuto: " + messaggio);
                out.println("Echo: " + messaggio);
            }

        } catch (IOException e) {
            System.out.println("Client disconnesso: " + socket.getInetAddress());
        } finally {
            try { socket.close(); } catch (IOException e) { e.printStackTrace(); }
        }
    }
}