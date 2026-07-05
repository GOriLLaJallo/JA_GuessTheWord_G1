package guesstheword_server;

import guesstheword_server.network.GameServer;
import java.io.IOException;
import guesstheword_server.db.DatabaseManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Classe principale (Entry Point) dell'applicazione GuessTheWord_Server.
 * Avvia l'interfaccia grafica JavaFX per l'amministratore e inizializza
 * la connessione e lo schema del database locale SQLite.
 * 
 * @author Carmine Muollo
 */
public class ServerApp extends Application {

    private static GameServer gameServer;

    @Override
    public void start(Stage primaryStage) {
        try {
            // Inizializza il database SQLite all'avvio (con relativo seeding dei dati se necessario)
            System.out.println("[ServerApp] Connessione ed inizializzazione del database in corso...");
            DatabaseManager.getInstance();
            
            // Shutdown hook per garantire la chiusura ordinata del pool di thread (ScheduledExecutorService)
            // in caso di terminazione forzata (es. Ctrl+C o segnali di arresto del sistema operativo).
            // Questo assicura che il server rilasci i descrittori di thread e file in modo appropriato.
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    System.out.println("[ShutdownHook] Rilevata terminazione di sistema: arresto dello scheduler e del server...");
                    guesstheword_server.game.GameSession.shutdownScheduler();
                    if (gameServer != null) {
                        gameServer.stopCon();
                    }
                }
            }));
            
            // Avvio del GameServer in un thread separato
            Thread serverThread = new Thread(() -> {
                try {
                    gameServer = new GameServer();
                    gameServer.startCon();
                } catch (IOException e) {
                    System.err.println("[ServerApp] Errore avvio GameServer: " + e.getMessage());
                }
            });
            serverThread.setDaemon(true);
            serverThread.start();

            // Caricamento del file FXML per la vista di login dell'amministratore
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/guesstheword_server/resources/view/AdminLoginView.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);

            // Configurazione dello Stage principale
            primaryStage.setTitle("GuessTheWord - Login Amministratore");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.centerOnScreen();
            primaryStage.show();
            
            System.out.println("[ServerApp] Interfaccia grafica caricata con successo.");

            } catch (Exception e) {
                System.err.println("[ServerApp] Errore irreversibile all'avvio dell'applicazione server:");
                e.printStackTrace();
            }
    }

    @Override
    public void stop() throws Exception {
        System.out.println("[ServerApp] Arresto applicazione: spegnimento dello scheduler e del server in corso...");
        guesstheword_server.game.GameSession.shutdownScheduler();
        if (gameServer != null) {
            gameServer.stopCon();
        }
        super.stop();
    }

    /**
     * Metodo main per l'avvio manuale dell'applicazione.
     *
     * @param args gli argomenti passati da riga di comando
     */
    public static void main(String[] args) {
        launch(args);
    }
}
