package guesstheword_server.analysis;

import guesstheword_server.game.Difficulty;
import java.util.ArrayList;
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
 * @author Davide Andrea Odierna
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
     * Analizza il testo fornito e salva un insieme di parole a seconda dei criteri stabiliti per ogni singola difficoltà, tra cui randomicamente sceglie la parola da cifrare
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
                List<String> easyPool = freqMap.entrySet().stream()
                    .filter(e -> e.getKey().length() < 6)
                    .map(Map.Entry::getKey)
                    .collect(java.util.stream.Collectors.toList());
                if (easyPool.isEmpty()) easyPool = new ArrayList<>(freqMap.keySet());
                return easyPool.get(new Random().nextInt(easyPool.size()));

            case HARD:
                List<String> hardPool = freqMap.entrySet().stream()
                    .filter(e -> e.getKey().length() >= 9)
                    .map(Map.Entry::getKey)
                    .collect(java.util.stream.Collectors.toList());
                if (hardPool.isEmpty()) hardPool = new ArrayList<>(freqMap.keySet());
                return hardPool.get(new Random().nextInt(hardPool.size()));

            case MEDIUM:
            default:
                List<String> mediumPool = freqMap.entrySet().stream()
                    .filter(e -> e.getKey().length() >= 6 && e.getKey().length() <= 8)
                    .map(Map.Entry::getKey)
                    .collect(java.util.stream.Collectors.toList());
                if (mediumPool.isEmpty()) mediumPool = new ArrayList<>(freqMap.keySet());
                return mediumPool.get(new Random().nextInt(mediumPool.size()));
        }    
    }
    /**
    * Estrae uno snippet di testo attorno alla parola chiave.
    * Principio di funzionamento
    * 1) Divide in testo in un array di String (split)
    * 2) Cerca tutti gli indici della keyword (ciclo for) e poi ne sceglie uno casuale da cui calcolare l'estratto di testo
    * 3) Calcola una finestra di 30 parole prima e dopo della keyword (se ci sono) da mostrare all'utente
    * 4) Costruisce l'estratto di testo
    *
    * @param text    il testo completo
    * @param keyword la parola attorno a cui estrarre lo snippet
    * @return uno snippet di circa 20 parole attorno alla keyword
    */
    public String extractExcerpt(String text, String keyword) {
        if (text == null || text.trim().isEmpty() || keyword == null || keyword.trim().isEmpty()) {
            return null;
        }

        String[] words = text.split("\\s+");
        List<Integer> matchingIndices = new ArrayList<>();
        String lowerKeyword = keyword.toLowerCase();

        for (int i = 0; i < words.length; i++) {
            String[] subTokens = words[i].toLowerCase().split("[^a-zA-Zàèìòùáéíóúâêîôûäëïöü]+");
            for (int j = 0; j < subTokens.length; j++) {
                if (subTokens[j].equals(lowerKeyword)) {
                    matchingIndices.add(i);
                    break;
                }
            }
        }

        if (matchingIndices.isEmpty()) {
            return null;
        }

        int keyIndex = matchingIndices.get(new Random().nextInt(matchingIndices.size()));

        int from = Math.max(0, keyIndex - 15);
        int to   = Math.min(words.length, keyIndex + 15);

        StringBuilder sb = new StringBuilder();
        for (int i = from; i < to; i++) {
            sb.append(words[i]);
            if (i < to - 1) {
                sb.append(" ");
            }
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

    /**
     * Verifica se un estratto contiene la parola segreta in modo esatto (parola intera, case-insensitive).
     *
     * @param estratto l'estratto di testo
     * @param parola la parola da cercare
     * @return true se l'estratto contiene la parola, false altrimenti
     */
    public static boolean contieneParola(String estratto, String parola) {
        if (estratto == null || parola == null) return false;
        String lowerParola = parola.toLowerCase();
        String[] tokens = estratto.toLowerCase().split("[^a-zA-Zàèìòùáéíóúâêîôûäëïöü]+");
        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].equals(lowerParola)) {
                return true;
            }
        }
        return false;
    }
}

