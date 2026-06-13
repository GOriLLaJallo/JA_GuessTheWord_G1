/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMain.java to edit this template
 */
package guesstheword_client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Classe principale che avvia il Client di GuessTheWord.
 *
 * @author Pc
 */
public class ClientApp extends Application {
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Carica il file FXML della schermata di Login
        Parent root = FXMLLoader.load(getClass().getResource("resources/view/LoginView.fxml"));
        
        Scene scene = new Scene(root);
        
        primaryStage.setTitle("GuessTheWord - Accesso");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false); // Impedisce di ridimensionare la finestra per non rovinare il layout
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
