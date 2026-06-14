package guesstheword_client.model;

import java.io.Serializable;

/**
 * Modello che rappresenta lo stato corrente di una partita in corso lato client.
 * Traccia lo stato della parola nascosta, i tentativi rimasti, il turno di gioco,
 * gli indizi cifrati e le statistiche temporanee.
 * 
 * @author Carmine Muollo
 */
public class GameState implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /** Pattern della parola nascosta dell'utente (es. "c _ m _ u _ _ _"). */
    private String wordPattern;

    /** Pattern della parola nascosta dell'avversario. */
    private String opponentPattern;

    /** Numero di tentativi residui prima del fallimento/timeout. */
    private int attemptsLeft;

    /** Nome dell'avversario. */
    private String opponentName;

    /** Indizio cifrato correntemente attivo per la sfida. */
    private String cipheredHint;

    /** Chiave (shift) del cifrario di Cesare per decrittografare l'indizio. */
    private int caesarShift;

    /** Stato del gioco (es. "WAITING", "PLAYING", "WON", "LOST", "TIMEOUT"). */
    private String status;

    /**
     * Costruttore predefinito.
     */
    public GameState() {
        this.status = "WAITING";
        this.attemptsLeft = 3;
    }

    /**
     * Costruttore completo.
     */
    public GameState(String wordPattern, String opponentPattern, int attemptsLeft,
                     String opponentName, String cipheredHint, int caesarShift, String status) {
        this.wordPattern = wordPattern;
        this.opponentPattern = opponentPattern;
        this.attemptsLeft = attemptsLeft;
        this.opponentName = opponentName;
        this.cipheredHint = cipheredHint;
        this.caesarShift = caesarShift;
        this.status = status;
    }

    // --- Getter e Setter ---

    public String getWordPattern() {
        return wordPattern;
    }

    public void setWordPattern(String wordPattern) {
        this.wordPattern = wordPattern;
    }

    public String getOpponentPattern() {
        return opponentPattern;
    }

    public void setOpponentPattern(String opponentPattern) {
        this.opponentPattern = opponentPattern;
    }

    public int getAttemptsLeft() {
        return attemptsLeft;
    }

    public void setAttemptsLeft(int attemptsLeft) {
        this.attemptsLeft = attemptsLeft;
    }

    public String getOpponentName() {
        return opponentName;
    }

    public void setOpponentName(String opponentName) {
        this.opponentName = opponentName;
    }

    public String getCipheredHint() {
        return cipheredHint;
    }

    public void setCipheredHint(String cipheredHint) {
        this.cipheredHint = cipheredHint;
    }

    public int getCaesarShift() {
        return caesarShift;
    }

    public void setCaesarShift(int caesarShift) {
        this.caesarShift = caesarShift;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "GameState{" +
                "wordPattern='" + wordPattern + '\'' +
                ", opponentPattern='" + opponentPattern + '\'' +
                ", attemptsLeft=" + attemptsLeft +
                ", opponentName='" + opponentName + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
