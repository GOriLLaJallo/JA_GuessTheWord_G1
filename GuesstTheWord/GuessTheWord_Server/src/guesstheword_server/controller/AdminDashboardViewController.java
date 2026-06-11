package guesstheword_server.controller;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * Controller per la schermata di controllo dell'amministratore (AdminDashboardView.fxml).
 * Gestisce l'interazione per la selezione dei file di testo (.txt), l'avvio del calcolo
 * delle frequenze dei caratteri e delle parole, e il salvataggio/caricamento serializzato
 * della cache dei risultati.
 * 
 * @author Carmine Muollo
 */
public class AdminDashboardViewController implements Initializable {

    @FXML
    private Button selectFilesBtn;

    @FXML
    private ListView<String> filesListView;

    @FXML
    private Button startAnalysisBtn;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Label statusLabel;

    @FXML
    private Button saveResultsBtn;

    @FXML
    private Button loadResultsBtn;

    @FXML
    private Label cacheStatusLabel;

    /**
     * Inizializza il controller. Viene chiamato automaticamente dopo il caricamento del file FXML.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        statusLabel.setText("Stato: In attesa di file...");
        cacheStatusLabel.setText("Nessun file di cache caricato.");
        progressBar.setVisible(false);
    }

    /**
     * Gestisce la selezione di uno o più file di testo (.txt) tramite FileChooser.
     *
     * @param event l'evento che ha scatenato l'azione
     */
    @FXML
    private void handleSelectFiles(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleziona uno o più documenti di testo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("File di Testo (*.txt)", "*.txt")
        );

        // Recupera la finestra corrente
        Stage stage = (Stage) selectFilesBtn.getScene().getWindow();
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(stage);

        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            filesListView.getItems().clear();
            for (File file : selectedFiles) {
                filesListView.getItems().add(file.getAbsolutePath());
            }
            statusLabel.setText("Selezionati " + selectedFiles.size() + " file.");
            progressBar.setVisible(false);
            progressBar.setProgress(0.0);
        } else {
            statusLabel.setText("Selezione file annullata.");
        }
    }

    /**
     * Avvia il processo di analisi dei file selezionati.
     * Attualmente implementa una simulazione visiva in attesa dello sviluppo del servizio asincrono.
     *
     * @param event l'evento che ha scatenato l'azione
     */
    @FXML
    private void handleStartAnalysis(ActionEvent event) {
        if (filesListView.getItems().isEmpty()) {
            statusLabel.setText("Errore: Seleziona almeno un file prima di avviare!");
            return;
        }

        statusLabel.setText("Analisi del testo in corso (simulata)...");
        progressBar.setVisible(true);
        progressBar.setProgress(0.5);

        // TODO: In seguito collegheremo il servizio asincrono (JavaFX Service/Task)
        
        statusLabel.setText("Analisi completata con successo!");
        progressBar.setProgress(1.0);
    }

    /**
     * Gestisce il salvataggio dei risultati dell'analisi in un file cache serializzato (.ser).
     *
     * @param event l'evento che ha scatenato l'azione
     */
    @FXML
    private void handleSaveResults(ActionEvent event) {
        // TODO: In seguito collegheremo la logica reale di serializzazione di AnalysisResult
        cacheStatusLabel.setText("Risultati dell'analisi salvati correttamente (simulazione).");
        System.out.println("[Dashboard] Richiesto salvataggio della cache di analisi.");
    }

    /**
     * Gestisce il caricamento della cache dei risultati dell'analisi precedentemente salvata.
     *
     * @param event l'evento che ha scatenato l'azione
     */
    @FXML
    private void handleLoadResults(ActionEvent event) {
        // TODO: In seguito collegheremo la logica reale di deserializzazione di AnalysisResult
        cacheStatusLabel.setText("Cache dei risultati caricata in memoria (simulazione).");
        System.out.println("[Dashboard] Richiesto caricamento della cache di analisi.");
    }
}
