package guesstheword_client.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Classe di utilità per il calcolo degli hash delle password.
 * Utilizza l'algoritmo standard SHA-256 per garantire la sicurezza del sistema.
 * 
 * @author William Menza
 */
public class HashUtil {

    /**
     * Calcola l'hash SHA-256 di una stringa di testo.
     *
     * @param input la stringa da hashare
     * @return la stringa hashata espressa in formato esadecimale
     */
    public static String sha256(String input) {
        if (input == null) return null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            System.err.println("[HashUtil] Algoritmo di hashing non supportato: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
