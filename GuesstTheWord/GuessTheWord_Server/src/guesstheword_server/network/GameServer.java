package guesstheword_server.network;

import java.io.*;
import java.net.*;
import java.util.Properties;

/**
 * Gestisce l'avvio e lo spegnimento ordinato del Server Socket di GuessTheWord.
 *
 * @author Sabrina Soriano
 */
public class GameServer {

    private ServerSocket serverSocket;
    private int port;
    private volatile boolean running = true;
    private final ClientRegistry clientRegistry = ClientRegistry.getInstance();
    
    private final java.util.concurrent.ExecutorService executorService = 
        java.util.concurrent.Executors.newFixedThreadPool(50);

    /**
     * Costruttore del server, legge la porta dal file di configurazione.
     * 
     * @throws IOException in caso di problemi di lettura delle proprietà
     */
    public GameServer() throws IOException {
        Properties props = new Properties();
        InputStream input = getClass().getResourceAsStream("/guesstheword_server/resources/properties/server.properties");
        props.load(input);
        port = Integer.parseInt(props.getProperty("server.port"));
    }

    /**
     * Apre il ServerSocket ed entra nel loop bloccante accept() per ricevere i client,
     * delegando ciascuna connessione ad un ClientHandler ed inserendolo nel registro.
     * 
     * @throws IOException in caso di errori di rete imprevisti
     */
    public void startCon() throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server avviato sulla porta " + port);

        try {
            while (running) {
                Socket clientSocket = serverSocket.accept(); // bloccante
                System.out.println("Client connesso: " + clientSocket.getInetAddress());

                // Crea l'handler passando la socket e il riferimento al registro
                ClientHandler handler = new ClientHandler(clientSocket, clientRegistry);
                clientRegistry.register(handler);
                executorService.submit(handler);
            }
        } catch (SocketException e) {
            if (!running) {
                System.out.println("[GameServer] ServerSocket chiuso volontariamente e correttamente.");
            } else {
                throw e;
            }
        }
    }

    /**
     * Esegue lo spegnimento controllato e ordinato di tutto il server socket,
     * chiudendo il ServerSocket, interrompendo i ClientHandler attivi (inviando la notifica),
     * eseguendo il cleanup di GameManager ed arrestando il pool di thread.
     */
    public synchronized void stopCon() {
        if (!running) return;
        running = false;
        System.out.println("[GameServer] Arresto ordinato del server in corso...");

        // 1. Chiude il ServerSocket per interrompere il loop di accept()
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                System.err.println("[GameServer] Errore nella chiusura del ServerSocket: " + e.getMessage());
            }
        }

        // 2. Notifica ed arresta tutti i ClientHandler connessi
        clientRegistry.closeAllConnections();

        // 3. Esegue il clean-up totale di GameManager
        guesstheword_server.game.GameManager.getInstance().shutdownAll();

        // 4. Arresta il pool di thread
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(3, java.util.concurrent.TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) throws IOException {
        new GameServer().startCon();
    }
}