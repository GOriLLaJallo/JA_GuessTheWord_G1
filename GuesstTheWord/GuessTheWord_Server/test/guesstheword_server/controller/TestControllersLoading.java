package guesstheword_server.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test unitari JUnit per verificare il corretto caricamento di tutti i controller
 * e delle relative viste FXML della console di amministrazione del Server.
 * 
 * @author Carmine Muollo
 */
public class TestControllersLoading {

    @BeforeClass
    public static void initJavaFX() {
        // Inizializza implicitamente il JavaFX Toolkit in modo che FXMLLoader possa essere eseguito
        new javafx.embed.swing.JFXPanel();
    }

    @Test
    public void testAdminLoginViewLoading() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/guesstheword_server/resources/view/AdminLoginView.fxml"));
        Parent root = loader.load();
        assertNotNull("Il caricamento di AdminLoginView FXML non deve restituire null", root);
        Object controller = loader.getController();
        assertTrue("Il controller deve essere istanza di AdminLoginViewController", controller instanceof AdminLoginViewController);
    }

    @Test
    public void testAdminDashboardViewLoading() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/guesstheword_server/resources/view/AdminDashboardView.fxml"));
        Parent root = loader.load();
        assertNotNull("Il caricamento di AdminDashboardView FXML non deve restituire null", root);
        Object controller = loader.getController();
        assertTrue("Il controller deve essere istanza di AdminDashboardViewController", controller instanceof AdminDashboardViewController);
    }

    @Test
    public void testLeaderBoardViewLoading() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/guesstheword_server/resources/view/LeaderBoardView.fxml"));
        Parent root = loader.load();
        assertNotNull("Il caricamento di LeaderBoardView FXML non deve restituire null", root);
        Object controller = loader.getController();
        assertTrue("Il controller deve essere istanza di LeaderBoardViewController", controller instanceof LeaderBoardViewController);
    }

    @Test
    public void testAdminMainViewLoading() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/guesstheword_server/resources/view/AdminMainView.fxml"));
        Parent root = loader.load();
        assertNotNull("Il caricamento di AdminMainView FXML non deve restituire null", root);
        Object controller = loader.getController();
        assertTrue("Il controller deve essere istanza di AdminMainViewController", controller instanceof AdminMainViewController);
    }
}
