/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package guesstheword_server.analysis;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Analizza un documento di testo libero ed estrae la parola più significativa
 * da utilizzare come parola nascosta nella sfida di gioco.
 *
 * La selezione avviene in tre fasi:
 * 1. Tokenizzazione del testo (split su punteggiatura e spazi).
 * 2. Filtraggio delle stopword italiane comuni e delle parole troppo corte.
 * 3. Selezione della parola con la frequenza più alta nel testo.
 *
 * Se il testo è vuoto o non contiene parole valide, viene restituita
 * una parola estratta casualmente dal vocabolario di fallback interno.
 *
 * @author Pc
 */
public class DocumentAnalyzer {

    /** Lunghezza minima che una parola deve avere per essere considerata. */
    private static final int MIN_WORD_LENGTH = 4;

    /** Stopword italiane comuni da ignorare durante l'analisi. */
    private static final Set<String> STOPWORDS = new HashSet<>(Arrays.asList(
        "che", "con", "del", "della", "delle", "degli", "dei", "dello",
        "dal", "dalla", "dalle", "dagli", "dai", "dallo",
        "nel", "nella", "nelle", "negli", "nei", "nello",
        "sul", "sulla", "sulle", "sugli", "sui", "sullo",
        "per", "tra", "fra", "come", "quando", "dove", "anche",
        "sono", "essere", "avere", "fare", "dire", "andare",
        "una", "uno", "gli", "alla", "alle", "agli", "allo",
        "questo", "questa", "questi", "queste", "quello", "quella",
        "quelli", "quelle", "molto", "poco", "tanto", "troppo",
        "ogni", "tutti", "tutte", "tutto", "tutta", "altri", "altre",
        "dopo", "prima", "ancora", "sempre", "mai", "però", "quindi",
        "perché", "perche", "mentre", "invece", "oppure", "nonché",
        "dalla", "nella", "nella", "dalla", "aveva", "hanno", "erano",
        "stata", "stato", "stati", "state", "verrà", "sarà"
    ));

    /** Vocabolario di fallback usato quando il testo non produce parole valide. */
    private static final List<String> FALLBACK_WORDS = Arrays.asList(
        "computer", "tastiera", "schermo", "programma", "rete",
        "finestra", "cartella", "stampante", "internet", "telefono",
        "musica", "cinema", "viaggio", "cucina", "giardino",
        "montagna", "spiaggia", "biblioteca", "ospedale", "stazione",
        "castello", "villaggio", "mercato", "ristorante", "teatro"
    );

    /**
     * Analizza il testo fornito ed estrae la parola più significativa.
     *
     * @param text il documento di testo da analizzare; può essere null o vuoto
     * @return la parola estratta (sempre in minuscolo), mai null
     */
    public String extractKeyWord(String text) {
        if (text == null || text.trim().isEmpty()) {
            return randomFallback();
        }

        // Tokenizzazione: split su tutto ciò che non è una lettera
        String[] tokens = text.toLowerCase().split("[^a-zA-Zàèìòùáéíóúâêîôûäëïöü]+");

        // Conta le frequenze filtrando stopword e parole troppo corte
        Map<String, Integer> freq = new HashMap<>();
        for (String token : tokens) {
            if (token.length() < MIN_WORD_LENGTH) continue;
            if (STOPWORDS.contains(token)) continue;
            freq.put(token, freq.getOrDefault(token, 0) + 1);
        }

        if (freq.isEmpty()) {
            return randomFallback();
        }

        // Restituisce la parola con frequenza massima
        return freq.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .get()
                .getKey();
    }

    /**
     * Restituisce una parola casuale dal vocabolario di fallback.
     *
     * @return parola di fallback
     */
    private String randomFallback() {
        return FALLBACK_WORDS.get(new Random().nextInt(FALLBACK_WORDS.size()));
    }
}

