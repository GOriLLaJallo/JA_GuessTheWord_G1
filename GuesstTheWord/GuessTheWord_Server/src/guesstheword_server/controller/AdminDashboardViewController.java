package guesstheword_server.controller;

import guesstheword_server.analysis.AnalysisResult;
import guesstheword_server.service.AnalysisService;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * Controller per la schermata di controllo dell'amministratore (AdminDashboardView.fxml).
 * Gestisce l'interazione per la selezione dei file di testo (.txt), l'avvio del calcolo
 * delle parole più frequenti in modalità asincrona (tramite JavaFX Service e Task),
 * e il salvataggio/caricamento serializzato (.ser) della cache dei risultati.
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

    // Conterrà l'elenco dei file reali selezionati dall'utente
    private final List<File> selectedFiles = new ArrayList<>();

    // Conterrà il servizio JavaFX per l'esecuzione in background
    private AnalysisService analysisService;

    // Memorizza l'ultimo risultato dell'analisi per la serializzazione
    private AnalysisResult lastAnalysisResult;

    /**
     * Inizializza il controller. Configura i componenti e crea l'istanza del servizio asincrono.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        statusLabel.setText("Stato: In attesa di file...");
        cacheStatusLabel.setText("Nessun file di cache caricato.");
        progressBar.setVisible(false);

        // Inizializzazione del servizio asincrono
        analysisService = new AnalysisService();

        // Configurazione delle proprietà asincrone tramite binding
        progressBar.progressProperty().bind(analysisService.progressProperty());
        statusLabel.textProperty().bind(analysisService.messageProperty());

        // Evento di completamento con successo del task asincrono
        analysisService.setOnSucceeded(event -> {
            lastAnalysisResult = analysisService.getValue();
            progressBar.setVisible(false);
            
            if (lastAnalysisResult != null) {
                statusLabel.textProperty().unbind(); // Scollega temporaneamente il binding per mostrare il testo statico
                statusLabel.setText("Analisi completata con successo.");
                saveResultsBtn.setDisable(false);
                showAlert(Alert.AlertType.INFORMATION, "Risultato Analisi", "Analisi completata con successo!",
                        "Parola chiave estratta: '" + lastAnalysisResult.getKeyWord() + "'\n" +
                        "Parole totali elaborate: " + lastAnalysisResult.getTotalWordsProcessed() + "\n" +
                        "Tempo di esecuzione: " + lastAnalysisResult.getAnalysisTimeMs() + " ms");
            }
            setControlsDisabled(false);
        });

        // Evento di fallimento del task asincrono
        analysisService.setOnFailed(event -> {
            progressBar.setVisible(false);
            statusLabel.textProperty().unbind();
            Throwable exception = analysisService.getException();
            String errorMsg = exception != null ? exception.getMessage() : "Errore ignoto.";
            statusLabel.setText("Errore durante l'analisi.");
            setControlsDisabled(false);
            showAlert(Alert.AlertType.ERROR, "Errore Analisi", "Si è verificato un errore durante l'analisi", errorMsg);
        });

        saveResultsBtn.setDisable(true);
    }

    /**
     * Mostra un dialogo Alert modale personalizzato con lo stile dell'applicazione.
     */
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        
        try {
            DialogPane dialogPane = alert.getDialogPane();
            String cssPath = getClass().getResource("/guesstheword_server/resources/styles/stylesheet.css").toExternalForm();
            dialogPane.getStylesheets().add(cssPath);
        } catch (Exception e) {
            System.err.println("[Dashboard] Errore nell'applicazione dello stile all'Alert: " + e.getMessage());
        }
        
        alert.showAndWait();
    }

    /**
     * Disabilita/Abilita i controlli durante l'esecuzione del background task.
     */
    private void setControlsDisabled(boolean disabled) {
        selectFilesBtn.setDisable(disabled);
        startAnalysisBtn.setDisable(disabled);
        loadResultsBtn.setDisable(disabled);
        if (lastAnalysisResult != null) {
            saveResultsBtn.setDisable(disabled);
        }
    }

    /**
     * Gestisce la selezione di uno o più file di testo (.txt) tramite FileChooser.
     * Mostra in lista solo i nomi dei file (senza esporre i percorsi assoluti sul filesystem).
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

        Stage stage = (Stage) selectFilesBtn.getScene().getWindow();
        List<File> files = fileChooser.showOpenMultipleDialog(stage);

        if (files != null && !files.isEmpty()) {
            selectedFiles.clear();
            selectedFiles.addAll(files);

            filesListView.getItems().clear();
            for (File file : selectedFiles) {
                // Per pulizia dell'interfaccia, mostra solo il nome del file
                filesListView.getItems().add(file.getName());
            }
            
            // Ripristina il binding con il messaggio del servizio
            if (!statusLabel.textProperty().isBound()) {
                statusLabel.textProperty().bind(analysisService.messageProperty());
            }
            analysisService.setFilesToAnalyze(selectedFiles);
            statusLabel.setText("Selezionati " + selectedFiles.size() + " file pronti per l'analisi.");
            progressBar.setVisible(false);
        } else {
            statusLabel.setText("Selezione file annullata.");
        }
    }

    /**
     * Avvia il processo di analisi asincrono dei file selezionati.
     *
     * @param event l'evento che ha scatenato l'azione
     */
    @FXML
    private void handleStartAnalysis(ActionEvent event) {
        if (selectedFiles.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Attenzione", "Nessun file selezionato", 
                    "Seleziona almeno un file di testo (.txt) prima di avviare l'analisi.");
            return;
        }

        // Assicura il collegamento del binding del testo prima del lancio
        if (!statusLabel.textProperty().isBound()) {
            statusLabel.textProperty().bind(analysisService.messageProperty());
        }

        progressBar.setVisible(true);
        setControlsDisabled(true);

        // Avvia il servizio JavaFX (esegue il Task in background)
        analysisService.restart();
    }

    /**
     * Gestisce il salvataggio dei risultati dell'analisi in un file cache serializzato (.ser).
     * Apre un FileChooser per far scegliere il percorso del salvataggio.
     *
     * @param event l'evento che ha scatenato l'azione
     */
    @FXML
    private void handleSaveResults(ActionEvent event) {
        if (lastAnalysisResult == null) {
            showAlert(Alert.AlertType.WARNING, "Attenzione", "Nessun dato da salvare", 
                    "Nessun risultato di analisi disponibile per il salvataggio.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salva cache analisi");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("File Cache (*.ser)", "*.ser"));
        fileChooser.setInitialFileName("cache_analisi.ser");

        Stage stage = (Stage) saveResultsBtn.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                oos.writeObject(lastAnalysisResult);
                cacheStatusLabel.setText("Cache: Salvata (" + file.getName() + ")");
                showAlert(Alert.AlertType.INFORMATION, "Salvataggio Cache", "Salvataggio completato!",
                        "La cache dei risultati è stata salvata con successo in:\n" + file.getName());
                System.out.println("[Dashboard] Cache salvata con successo in " + file.getAbsolutePath());
            } catch (Exception e) {
                cacheStatusLabel.setText("Cache: Errore di salvataggio");
                showAlert(Alert.AlertType.ERROR, "Errore di Salvataggio", "Impossibile salvare la cache", e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Gestisce il caricamento della cache dei risultati dell'analisi precedentemente salvata.
     * Carica e deserializza l'oggetto AnalysisResult popolando la lista di file visualizzata.
     *
     * @param event l'evento che ha scatenato l'azione
     */
    @FXML
    private void handleLoadResults(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Apri cache analisi");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("File Cache (*.ser)", "*.ser"));

        Stage stage = (Stage) loadResultsBtn.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                Object obj = ois.readObject();
                if (obj instanceof AnalysisResult) {
                    lastAnalysisResult = (AnalysisResult) obj;

                    // Aggiorna la ListView con i nomi dei file salvati nella cache
                    filesListView.getItems().clear();
                    filesListView.getItems().addAll(lastAnalysisResult.getFileNames());
                    selectedFiles.clear(); // La cache non contiene i file reali ma solo il risultato

                    statusLabel.textProperty().unbind();
                    statusLabel.setText("Risultati caricati da cache.");
                    cacheStatusLabel.setText("Cache: Caricata (" + file.getName() + ")");
                    saveResultsBtn.setDisable(false);
                    
                    showAlert(Alert.AlertType.INFORMATION, "Caricamento Cache", "Cache caricata con successo!",
                            "File caricato: " + file.getName() + "\n" +
                            "Parola chiave: '" + lastAnalysisResult.getKeyWord() + "'\n" +
                            "Parole totali elaborate: " + lastAnalysisResult.getTotalWordsProcessed() + "\n" +
                            "Tempo di esecuzione originario: " + lastAnalysisResult.getAnalysisTimeMs() + " ms");
                    System.out.println("[Dashboard] Cache deserializzata correttamente da " + file.getAbsolutePath());
                } else {
                    cacheStatusLabel.setText("Cache: Errore formato");
                    showAlert(Alert.AlertType.ERROR, "Errore Formato Cache", "File non valido",
                            "Il file selezionato non contiene un oggetto AnalysisResult valido.");
                }
            } catch (Exception e) {
                cacheStatusLabel.setText("Cache: Errore caricamento");
                showAlert(Alert.AlertType.ERROR, "Errore di Caricamento", "Impossibile caricare la cache", e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
