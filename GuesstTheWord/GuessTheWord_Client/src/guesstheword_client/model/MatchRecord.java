package guesstheword_client.model;

import java.io.Serializable;

/**
 * Modello che rappresenta il record storico di una singola partita giocata,
 * utilizzato lato client per popolare la TableView dello storico partite (HistoryView).
 * 
 * @author William Menza
 */
public class MatchRecord implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /** Parola segreta oggetto della sfida. */
    private String secretWord;

    /** Nome dell'avversario. */
    private String opponentName;

    /** Esito della sfida per il giocatore corrente ("WIN", "LOSE", "TIMEOUT"). */
    private String outcome;

    /** Tempo di risposta impiegato per indovinare, in millisecondi (null se timeout). */
    private Integer responseTime;

    /** Data e ora in cui si è svolta la sfida (rappresentata come stringa formattata). */
    private String matchDate;

    /** Difficoltà della sfida ("EASY", "MEDIUM", "HARD"). */
    private String difficulty;

    /**
     * Costruttore vuoto.
     */
    public MatchRecord() {
    }

    /**
     * Costruttore completo.
     */
    public MatchRecord(String secretWord, String opponentName, String outcome, Integer responseTime, String matchDate, String difficulty) {
        this.secretWord = secretWord;
        this.opponentName = opponentName;
        this.outcome = outcome;
        this.responseTime = responseTime;
        this.matchDate = matchDate;
        this.difficulty = difficulty;
    }

    // --- Getter e Setter ---

    public String getSecretWord() {
        return secretWord;
    }

    public void setSecretWord(String secretWord) {
        this.secretWord = secretWord;
    }

    public String getOpponentName() {
        return opponentName;
    }

    public void setOpponentName(String opponentName) {
        this.opponentName = opponentName;
    }

    public String getOutcome() {
        return outcome;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    public Integer getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(Integer responseTime) {
        this.responseTime = responseTime;
    }

    public String getMatchDate() {
        return matchDate;
    }

    public void setMatchDate(String matchDate) {
        this.matchDate = matchDate;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    @Override
    public String toString() {
        return "MatchRecord{" +
                "secretWord='" + secretWord + '\'' +
                ", opponentName='" + opponentName + '\'' +
                ", outcome='" + outcome + '\'' +
                ", responseTime=" + responseTime +
                ", matchDate='" + matchDate + '\'' +
                ", difficulty='" + difficulty + '\'' +
                '}';
    }
}
