package guesstheword_server.game;

import java.util.Random;

/**
 *
 *
 * @author Sabrina Soriano
 */
public class CaesarCipher {
    
    private static final int ALPHABET_SIZE = 26;
    
    /**
     * Codifica di una parola usando il cifrario di Cesare dato uno shift
     *
     * @param word  
     * @param shift (0-25)
     * @return parola codificata
     */
    public static String encrypt(String word, int shift) {
        if (word == null || word.isEmpty()) return word;
        shift = ((shift % 26) + 26) % 26; //perchè in java % restituisce anche valori negativi
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < word.length(); i++) {
            final char c = word.charAt(i);
            if (c >= 'A' && c <= 'Z') {
                sb.append((char) ('A' + (c - 'A' + shift) % ALPHABET_SIZE));
            } else if (c >= 'a' && c <= 'z') {
                sb.append((char) ('a' + (c - 'a' + shift) % ALPHABET_SIZE));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Decripta una parola codificata con il cifrario di Cesare, non fa nient'altro che chiamare il metodo encrypt invertendo lo shift
     * Es:
     * Hello con shift == 3 diventa Khoor; Khoor con shift == 26 - 3 diventa Hello 
     *
     * @param word  
     * @param shift 
     * @return la parola originale
     */
    public static String decrypt(String word, int shift) {
        return encrypt(word, 26 - shift); 
    }

    /**
     * Trova tutte le occorrenze delle parole da cifrare e la sostituisce con la loro versione cifrata
     *
     * @param text        
     * @param wordsToHide 
     * @param shift       
     * @return il testo con le parole cifrate
     */
    public static String encryptWordsInText(String text, String[] wordsToHide, int shift) {
        if (text == null || wordsToHide == null) return text;
        for (String word : wordsToHide) {
            if (word == null || word.isEmpty()) continue;
            //usiamo il metodo encrypt per cifrare una parola
            String encrypted = encrypt(word, shift);
            //usiamo il metodo replaceAll della classe String per sostiruire tutte le occorrenze all'interno di text
            //importante Pattern.quote() impedisce che l'eventuale presenza di caratteri speciali venga vista come caratteri speciali e "(?<![\\w])" ci assicura che la parola sia preceduta da un carattere speciale
            //quindi sia una parola e non una parte di un'altra parola
            //Es "xxx ore xxxxx" ore viene cifrato, mentre "xxxx dittatore xxxxx" ore all'interno di dittatore non viene cifrato
            text = text.replaceAll("(?<![\\w])" + java.util.regex.Pattern.quote(word)
                                   + "(?![\\w])", encrypted);
        }
        return text;
    }

    /**
     * Genera un random shift a seconda della difficoltà
     * Capire se effettivamente è più facile
     *
     * @param difficulty
     * @return shift del cifrario di Cesare
     */
    public static int randomShift(Difficulty difficulty) {
        Random random = new Random();

        switch (difficulty) {
            case EASY: {
            // Sceglie casualmente tra range basso [1-5] o alto [21-25]
                if (random.nextBoolean()) {
                    return 1 + random.nextInt(5);   // Restituisce 1, 2, 3, 4, o 5
                } else {
                    return 21 + random.nextInt(5);  // Restituisce 21, 22, 23, 24, o 25 (EVITA 26 CHE E' UGUALE A 0)
                }
            }
            case MEDIUM: {
                // Sceglie casualmente tra range [5-8] o [19-22]
                if (random.nextBoolean()) {
                    return 5 + random.nextInt(4);   // [5-8]
                } else {
                    return 19 + random.nextInt(4);  // [19-22]
                }
            }
            case HARD:
                default: {
                return 9 + random.nextInt(10);      // [9-18]
            }
        }
    }


}
