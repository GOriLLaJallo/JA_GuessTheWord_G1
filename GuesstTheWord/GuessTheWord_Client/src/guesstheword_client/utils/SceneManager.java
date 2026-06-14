package guesstheword_client.utils;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.event.Event;

/**
 * Classe di utilità per gestire in modo centralizzato (DRY) i cambi di schermata (Scene)
 * all'interno dell'applicazione JavaFX.
 * 
 * @author William Menza
 */
public class SceneManager {

    /**
     * Cambia la scena corrente sostituendola con quella caricata dal file FXML specificato.
     * 
     * @param <T> Il tipo del Controller associato alla vista
     * @param event L'evento scatenante (utile per recuperare la finestra corrente)
     * @param fxmlPath Il percorso assoluto o relativo del file FXML (es. "/guesstheword_client/resources/view/View.fxml")
     * @return L'istanza del Controller associato al file FXML caricato
     * @throws IOException Se il caricamento del file FXML fallisce
     */
    public static <T> T switchScene(Event event, String fxmlPath) throws IOException {
        Node sourceNode = (Node) event.getSource();
        Stage window = (Stage) sourceNode.getScene().getWindow();
        return switchScene(window, fxmlPath);
    }

    /**
     * Cambia la scena corrente utilizzando direttamente lo Stage.
     * 
     * @param <T> Il tipo del Controller associato alla vista
     * @param window Lo Stage (finestra) corrente
     * @param fxmlPath Il percorso assoluto o relativo del file FXML
     * @return L'istanza del Controller associato al file FXML caricato
     * @throws IOException Se il caricamento del file FXML fallisce
     */
    public static <T> T switchScene(Stage window, String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        
        window.setScene(scene);
        window.centerOnScreen();
        window.show();
        
        return loader.getController();
    }
}
