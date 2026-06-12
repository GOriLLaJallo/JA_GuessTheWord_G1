package guesstheword_server.model;

/**
 * Data Transfer Object (DTO) per rappresentare le statistiche aggregate di un utente.
 * Raggruppa il conteggio delle vittorie, delle partite totali giocate e il tempo medio di risposta.
 * 
 * @author Carmine Muollo
 */
public class UserStatsDTO {
    
    private int victories;
    private int gamesPlayed;
    private double averageResponseTime;

    /**
     * Costruttore completo del DTO.
     * 
     * @param victories           numero di vittorie
     * @param gamesPlayed         numero di partite giocate
     * @param averageResponseTime tempo medio di risposta (in ms)
     */
    public UserStatsDTO(int victories, int gamesPlayed, double averageResponseTime) {
        this.victories = victories;
        this.gamesPlayed = gamesPlayed;
        this.averageResponseTime = averageResponseTime;
    }

    public int getVictories() {
        return victories;
    }

    public void setVictories(int victories) {
        this.victories = victories;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public double getAverageResponseTime() {
        return averageResponseTime;
    }

    public void setAverageResponseTime(double averageResponseTime) {
        this.averageResponseTime = averageResponseTime;
    }

    @Override
    public String toString() {
        return "UserStatsDTO{" +
                "victories=" + victories +
                ", gamesPlayed=" + gamesPlayed +
                ", averageResponseTime=" + averageResponseTime +
                '}';
    }
}
