/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package guesstheword_server.analysis;

import guesstheword_server.game.Difficulty;
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
        "aveva", "hanno", "erano",
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

        // Conta le frequenze filtrando stopword e parole troppo corte tramite Stream
        return Arrays.stream(tokens)
                .filter(token -> token.length() >= MIN_WORD_LENGTH)
                .filter(token -> !STOPWORDS.contains(token))
                .collect(java.util.stream.Collectors.groupingBy(
                        token -> token,
                        java.util.stream.Collectors.summingInt(token -> 1)
                ))
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElseGet(this::randomFallback);
    }
    
    /**
     * Analizza il testo fornito ed estrae la parola più significativa a seconda della difficoltà
     * Assegna la coppia parola-frequenza, filtrando stopword e parole troppo corte
     * 
     * @param text
     * @param difficulty
     * @return 
     */
    
    public String extractKeyWord(String text, Difficulty difficulty) {
        if (text == null || text.trim().isEmpty()) {
            return randomFallback();
        }

        String[] tokens = text.toLowerCase().split("[^a-zA-Zàèìòùáéíóúâêîôûäëïöü]+");

        
        Map<String, Integer> freqMap = Arrays.stream(tokens)
                .filter(token -> token.length() >= MIN_WORD_LENGTH)
                .filter(token -> !STOPWORDS.contains(token))
                .collect(java.util.stream.Collectors.groupingBy(
                        token -> token,
                        java.util.stream.Collectors.summingInt(token -> 1)
             ));

        if (freqMap.isEmpty()) return randomFallback();

        switch (difficulty) {
            case EASY:
                //Parola più frequente e più corta (lunghezza <= 6)
                return freqMap.entrySet().stream()
                        .filter(e -> e.getKey().length() <= 6)
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey)
                        .orElseGet(() -> freqMap.entrySet().stream()
                                .max(Map.Entry.comparingByValue())
                                .map(Map.Entry::getKey)
                                .orElseGet(this::randomFallback));

            case HARD:
                //Parola più rara con lunghezza >= 9
                return freqMap.entrySet().stream()
                    .filter(e -> e.getKey().length() >= 9)
                    .min(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElseGet(() -> freqMap.entrySet().stream()
                            .min(Map.Entry.comparingByValue())
                            .map(Map.Entry::getKey)
                            .orElseGet(this::randomFallback));
                
            case MEDIUM:
            default:
                //Parola con lunghezza media (tra 6 e 8 lettere) e frequenza media (Considerato caso default)
                return freqMap.entrySet().stream()
                    .filter(e -> e.getKey().length() >= 6 && e.getKey().length() <= 8)
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElseGet(() -> freqMap.entrySet().stream()
                            .max(Map.Entry.comparingByValue())
                            .map(Map.Entry::getKey)
                            .orElseGet(this::randomFallback));
        }
    }    
    
    /**
    * Estrae uno snippet di testo attorno alla parola chiave.
    * Principio di funzionamento
    * 1) Divide in testo in un array di String (split)
    * 2) Cerca l'indice della keyword (ciclo for)
    * 3) Calcola una finestra di 30 parole prima e dopo della keyword (se ci sono) da mostrare all'utente
    * 4) Costruisce l'estratto di testo
    *
    * @param text    il testo completo
    * @param keyword la parola attorno a cui estrarre lo snippet
    * @return uno snippet di circa 20 parole attorno alla keyword
    */
    public String extractExcerpt(String text, String keyword) {
        if (text == null || text.trim().isEmpty()) return keyword;

        String[] words = text.split("\\s+");
        int keyIndex = -1;

        for (int i = 0; i < words.length; i++) {
            if (words[i].toLowerCase().replaceAll("[^a-zA-Zàèìòùáéíóú]", "").equals(keyword)) {
                keyIndex = i;
                break;
            }
        }

        if (keyIndex == -1) return keyword;

        int from = Math.max(0, keyIndex - 30);
        int to   = Math.min(words.length, keyIndex + 30);

        StringBuilder sb = new StringBuilder();
            for (int i = from; i < to; i++) {
                sb.append(words[i]);
                if (i < to - 1) sb.append(" ");
            }
        return sb.toString();
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

