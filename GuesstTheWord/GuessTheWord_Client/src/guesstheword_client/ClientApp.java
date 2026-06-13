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
 * Si occupa di caricare l'interfaccia grafica iniziale (LoginView)
 * e di configurare la finestra principale dell'applicazione (Stage).
 *
 * @author William Menza
 */
public class ClientApp extends Application {
    
    /**
     * Metodo di avvio principale per l'applicazione JavaFX.
     * Carica il layout FXML della schermata di login e mostra la finestra.
     *
     * @param primaryStage lo stage principale fornito dal framework JavaFX
     * @throws Exception se si verifica un errore durante il caricamento del file FXML
     */
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
     * Entry point dell'applicazione Java.
     * Invoca il metodo launch di JavaFX per far partire il ciclo di vita dell'interfaccia grafica.
     *
     * @param args argomenti passati da riga di comando
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
