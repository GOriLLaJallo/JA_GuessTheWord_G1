package guesstheword_server.analysis;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Rappresenta il risultato dell'analisi di uno o più documenti di testo.
 * Contiene la parola chiave estratta, l'elenco dei file analizzati, il numero totale di parole
 * elaborate ed il tempo impiegato per l'analisi.
 * Implementa Serializable per consentire il salvataggio in cache (.ser).
 * 
 * @author Davide Andrea Odierna
 */
public class AnalysisResult implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private final String keyWord;
    private final List<String> fileNames;
    private final int totalWordsProcessed;
    private final long analysisTimeMs;
    private final String sourceText;

    /**
     * Costruttore completo del risultato dell'analisi.
     * 
     * @param keyWord             la parola chiave estratta
     * @param fileNames           l'elenco dei nomi dei file analizzati
     * @param totalWordsProcessed il totale delle parole elaborate
     * @param analysisTimeMs      il tempo totale in ms impiegato
     */
    public AnalysisResult(String keyWord, List<String> fileNames, int totalWordsProcessed, long analysisTimeMs, String sourceText) {
        this.keyWord = keyWord;
        this.fileNames = fileNames != null ? new ArrayList<>(fileNames) : Collections.emptyList();
        this.totalWordsProcessed = totalWordsProcessed;
        this.analysisTimeMs = analysisTimeMs;
        this.sourceText = sourceText;
    }

    public String getKeyWord() {
        return keyWord;
    }

    public List<String> getFileNames() {
        return Collections.unmodifiableList(fileNames);
    }

    public int getTotalWordsProcessed() {
        return totalWordsProcessed;
    }

    public long getAnalysisTimeMs() {
        return analysisTimeMs;
    }
    
    public String getSourceText() {
        return sourceText;
    }

    @Override
    public String toString() {
        return "AnalysisResult{" +
                "keyWord='" + keyWord + '\'' +
                ", fileNames=" + fileNames +
                ", totalWordsProcessed=" + totalWordsProcessed +
                ", analysisTimeMs=" + analysisTimeMs +
                '}';
    }
}
