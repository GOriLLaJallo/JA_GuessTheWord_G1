package guesstheword_server.analysis;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import java.io.File;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.ArrayList;

/**
 * Servizio asincrono JavaFX (Service/Task) per l'analisi di documenti di testo.
 * Elabora una lista di file leggendoli ed estraendo la parola chiave più significativa
 * in un thread separato in background, mantenendo la GUI dell'amministratore reattiva.
 * Garantisce l'isolamento del thread grafico JavaFX e l'interruzione pulita delle risorse.
 * 
 * @author Carmine Muollo
 */
public class AnalysisService extends Service<AnalysisResult> {

    /** La lista dei file di testo da sottoporre all'analisi. */
    private List<File> filesToAnalyze;

    /**
     * Costruttore di default esplicito per la classe AnalysisService.
     */
    public AnalysisService() {
        // Costruttore vuoto di default
    }

    /**
     * Imposta i file da analizzare.
     * 
     * @param files la lista di file di testo
     */
    public void setFilesToAnalyze(List<File> files) {
        this.filesToAnalyze = files;
    }

    /**
     * Crea e restituisce il Task per l'elaborazione asincrona dei file.
     * Effettua una copia difensiva della lista dei file per prevenire race condition.
     *
     * @return il Task di background per l'analisi del testo
     */
    @Override
    protected Task<AnalysisResult> createTask() {
        // Copia difensiva della lista per garantire thread-safety nel caso in cui venga modificata dalla UI
        final List<File> files = new ArrayList<>(this.filesToAnalyze != null ? this.filesToAnalyze : new ArrayList<>());

        return new Task<AnalysisResult>() {
            @Override
            protected AnalysisResult call() throws Exception {
                if (files.isEmpty()) {
                    throw new IllegalArgumentException("Nessun file selezionato per l'analisi.");
                }

                long startTime = System.currentTimeMillis();
                StringBuilder combinedText = new StringBuilder();
                List<String> fileNames = new ArrayList<>();
                
                long totalBytes = 0;
                for (File f : files) {
                    totalBytes += f.length();
                }

                long processedBytes = 0;
                for (File file : files) {
                    // Controlla sia la cancellazione del Task sia l'interruzione del thread corrente
                    if (isCancelled() || Thread.currentThread().isInterrupted()) {
                        updateMessage("Analisi annullata.");
                        return null;
                    }

                    fileNames.add(file.getName());
                    updateMessage("Lettura file: " + file.getName() + "...");

                    // Ottimizzazione I/O: Lettura efficiente a blocchi orientata ai caratteri (compatibile con JDK 8)
                    try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
                        char[] buffer = new char[4096];
                        int bytesRead;
                        while ((bytesRead = reader.read(buffer)) != -1) {
                            if (isCancelled() || Thread.currentThread().isInterrupted()) {
                                updateMessage("Analisi annullata.");
                                return null;
                            }
                            combinedText.append(buffer, 0, bytesRead);
                        }
                        combinedText.append(" ");
                    } catch (IOException e) {
                        throw new IOException("Errore durante la lettura del file " + file.getName() + ": " + e.getMessage(), e);
                    }

                    processedBytes += file.length();
                    updateProgress(processedBytes, totalBytes);

                    // Micro-pausa per visualizzare l'avanzamento sulla GUI, gestendo l'interruzione
                    try {
                        Thread.sleep(150);
                    } catch (InterruptedException e) {
                        if (isCancelled() || Thread.currentThread().isInterrupted()) {
                            updateMessage("Analisi annullata.");
                            return null;
                        }
                        Thread.currentThread().interrupt(); // Ripristina lo stato di interruzione
                    }
                }

                updateMessage("Elaborazione e calcolo frequenze parole chiave in corso...");
                
                DocumentAnalyzer analyzer = new DocumentAnalyzer();
                String keyWord = analyzer.extractKeyWord(combinedText.toString());

                // Stima del conteggio delle parole
                String[] words = combinedText.toString().split("\\s+");
                int totalWords = words.length;
                long elapsedTime = System.currentTimeMillis() - startTime;

                updateMessage("Analisi completata con successo.");
                return new AnalysisResult(keyWord, fileNames, totalWords, elapsedTime, combinedText.toString());
            }
        };
    }
}
