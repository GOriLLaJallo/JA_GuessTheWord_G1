package guesstheword_server.controller;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

/**
 * Test di integrazione per verificare il corretto caricamento di tutti i controller
 * e delle relative viste FXML della console di amministrazione del Server.
 * Garantisce l'assenza di eccezioni causate da fx:id errati o classi controller incongruenti.
 * 
 * @author Carmine Muollo
 */
public class TestControllersLoading extends Application {

    public static void main(String[] args) {
        // Avvia l'applicazione JavaFX per abilitare il toolkit grafico ed eseguire i test nel thread start()
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println("==================================================");
        System.out.println("AVVIO TEST CARICAMENTO CONTROLLER E VISTE FXML");
        System.out.println("==================================================");

        // Test 1: Caricamento AdminLoginView.fxml
        try {
            System.out.print("Test 1: Caricamento AdminLoginView... ");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/guesstheword_server/resources/view/AdminLoginView.fxml"));
            Parent root = loader.load();
            Object controller = loader.getController();
            
            if (!(controller instanceof AdminLoginViewController)) {
                throw new RuntimeException("Atteso controller di tipo AdminLoginViewController, trovato: " + (controller != null ? controller.getClass().getName() : "null"));
            }
            System.out.println("PASSATO");
        } catch (Exception e) {
            System.err.println("FALLITO con errore di caricamento:");
            e.printStackTrace();
            System.exit(1);
        }

        // Test 2: Caricamento AdminDashboardView.fxml
        try {
            System.out.print("Test 2: Caricamento AdminDashboardView... ");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/guesstheword_server/resources/view/AdminDashboardView.fxml"));
            Parent root = loader.load();
            Object controller = loader.getController();
            
            if (!(controller instanceof AdminDashboardViewController)) {
                throw new RuntimeException("Atteso controller di tipo AdminDashboardViewController, trovato: " + (controller != null ? controller.getClass().getName() : "null"));
            }
            System.out.println("PASSATO");
        } catch (Exception e) {
            System.err.println("FALLITO con errore di caricamento:");
            e.printStackTrace();
            System.exit(1);
        }

        // Test 3: Caricamento LeaderBoardView.fxml
        try {
            System.out.print("Test 3: Caricamento LeaderBoardView... ");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/guesstheword_server/resources/view/LeaderBoardView.fxml"));
            Parent root = loader.load();
            Object controller = loader.getController();
            
            if (!(controller instanceof LeaderBoardViewController)) {
                throw new RuntimeException("Atteso controller di tipo LeaderBoardViewController, trovato: " + (controller != null ? controller.getClass().getName() : "null"));
            }
            System.out.println("PASSATO");
        } catch (Exception e) {
            System.err.println("FALLITO con errore di caricamento:");
            e.printStackTrace();
            System.exit(1);
        }

        // Test 4: Caricamento AdminMainView.fxml (Contenitore principale)
        try {
            System.out.print("Test 4: Caricamento AdminMainView... ");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/guesstheword_server/resources/view/AdminMainView.fxml"));
            Parent root = loader.load();
            Object controller = loader.getController();
            
            if (!(controller instanceof AdminMainViewController)) {
                throw new RuntimeException("Atteso controller di tipo AdminMainViewController, trovato: " + (controller != null ? controller.getClass().getName() : "null"));
            }
            System.out.println("PASSATO");
        } catch (Exception e) {
            System.err.println("FALLITO con errore di caricamento:");
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("==================================================");
        System.out.println("TUTTE LE VISTE E I CONTROLLER SONO STATI CARICATI CORRETTAMENTE!");
        System.out.println("==================================================");
        System.exit(0);
    }
}
