package guesstheword_server.analysis;

import guesstheword_server.analysis.AnalysisResult;
import guesstheword_server.analysis.DocumentAnalyzer;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.ArrayList;

/**
 * Servizio asincrono JavaFX (Service/Task) per l'analisi di documenti di testo.
 * Elabora una lista di file leggendoli ed estraendo la parola chiave più significativa
 * in un thread separato in background, mantenendo la GUI dell'amministratore reattiva.
 * 
 * @author Carmine Muollo
 */
public class AnalysisService extends Service<AnalysisResult> {

    private List<File> filesToAnalyze;

    /**
     * Imposta i file da analizzare.
     * 
     * @param files la lista di file di testo
     */
    public void setFilesToAnalyze(List<File> files) {
        this.filesToAnalyze = files;
    }

    @Override
    protected Task<AnalysisResult> createTask() {
        return new Task<AnalysisResult>() {
            @Override
            protected AnalysisResult call() throws Exception {
                if (filesToAnalyze == null || filesToAnalyze.isEmpty()) {
                    throw new IllegalArgumentException("Nessun file selezionato per l'analisi.");
                }

                long startTime = System.currentTimeMillis();
                StringBuilder combinedText = new StringBuilder();
                List<String> fileNames = new ArrayList<>();
                
                long totalBytes = 0;
                for (File f : filesToAnalyze) {
                    totalBytes += f.length();
                }

                long processedBytes = 0;
                for (int i = 0; i < filesToAnalyze.size(); i++) {
                    if (isCancelled()) {
                        updateMessage("Analisi annullata.");
                        return null;
                    }

                    File file = filesToAnalyze.get(i);
                    fileNames.add(file.getName());
                    updateMessage("Lettura file: " + file.getName() + "...");

                    // Leggi i byte del file convertendoli in stringa UTF-8
                    byte[] bytes = Files.readAllBytes(file.toPath());
                    combinedText.append(new String(bytes, "UTF-8")).append(" ");

                    processedBytes += file.length();
                    updateProgress(processedBytes, totalBytes);

                    // Micro-pausa per rendere visibile l'avanzamento sulla barra di progresso
                    Thread.sleep(150);
                }

                updateMessage("Elaborazione e calcolo frequenze parole chiave in corso...");
                
                DocumentAnalyzer analyzer = new DocumentAnalyzer();
                String keyWord = analyzer.extractKeyWord(combinedText.toString());

                // Stima del conteggio delle parole
                String[] words = combinedText.toString().split("\\s+");
                int totalWords = words.length;
                long elapsedTime = System.currentTimeMillis() - startTime;

                updateMessage("Analisi completata con successo.");
                return new AnalysisResult(keyWord, fileNames, totalWords, elapsedTime);
            }
        };
    }
}
