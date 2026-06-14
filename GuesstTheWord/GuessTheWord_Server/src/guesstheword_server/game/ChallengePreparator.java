/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package guesstheword_server.game;

import guesstheword_server.analysis.DocumentAnalyzer;
import guesstheword_server.model.Challenge;
import java.time.LocalDateTime;

/**
 * Prepara una {@link Challenge} pronta per essere usata in una {@link GameSession}.
 *
 * Il flusso è:
 * 1. Analizza il testo del documento con {@link DocumentAnalyzer} ed estrae
 *    la parola chiave più significativa.
 * 2. Genera uno shift di Cesare casuale in base alla {@link Difficulty} richiesta.
 * 3. Costruisce e restituisce un oggetto {@link Challenge} con la parola estratta,
 *    lo shift scelto e il timestamp corrente.
 *
 * Se non viene fornito alcun testo, la parola viene scelta dal vocabolario
 * di fallback interno a {@link DocumentAnalyzer}.
 *
 * @author Pc
 */
public class ChallengePreparator {

    private final DocumentAnalyzer analyzer;

    /**
     * Costruisce un nuovo ChallengePreparator con il suo DocumentAnalyzer.
     */
    public ChallengePreparator() {
        this.analyzer = new DocumentAnalyzer();
    }

    /**
     * Prepara una sfida a partire da un testo libero e da un livello di difficoltà.
     * Principio di funzionamento
     * 1) Considerando il testo e la difficoltà extractKeyWord (metodo classe DocumentAnalizer) estrapola la parola nascosta
     * 2) Viene stabilito lo shift del cifrario di Cesare e viene creato un oggetto Challenge con tutti i parametri necessari
     * 3) Viene infine salvato un estratto del text
     * 
     *
     * @param text       il documento da cui estrarre la parola nascosta;
     *                   può essere null o vuoto (verrà usato il fallback)
     * @param difficulty il livello di difficoltà che determina lo shift di Cesare
     * @return un oggetto {@link Challenge} pronto per la sessione di gioco
     */
    public Challenge prepare(String text, Difficulty difficulty) {
        // 1. Estrai la parola chiave dal testo a seconda della difficoltà
        String parolaNascosta = analyzer.extractKeyWord(text, difficulty);

        int shift = CaesarCipher.randomShift(difficulty);
        
        Challenge c = new Challenge(parolaNascosta, shift, LocalDateTime.now(), difficulty.name());
        String estratto = analyzer.extractExcerpt(text, parolaNascosta);
        c.setEstratto(estratto);
        return c;
        
    }

    /**
     * Prepara una sfida con difficoltà MEDIUM (metodo di comodo).
     *
     * @param text il documento da analizzare
     * @return un oggetto {@link Challenge} con difficoltà media
     */
    public Challenge prepare(String text) {
        return prepare(text, Difficulty.MEDIUM);
    }

    /**
     * Prepara una sfida senza testo: la parola viene scelta casualmente
     * dal vocabolario di fallback con la difficoltà specificata.
     *
     * @param difficulty il livello di difficoltà
     * @return un oggetto {@link Challenge} con parola casuale
     */
    public Challenge prepareRandom(Difficulty difficulty) {
        return prepare(null, difficulty);
    }
}

