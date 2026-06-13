package guesstheword_client.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Modello dati per rappresentare una singola riga nello storico delle partite.
 * Utilizza le Properties di JavaFX per permettere il binding automatico
 * con le colonne della TableView.
 * 
 * @author William Menza
 */
public class HistoryItem {

    private final StringProperty date;
    private final StringProperty result;
    private final StringProperty word;

    /**
     * Costruisce un nuovo elemento dello storico.
     * 
     * @param date la data in cui si è svolta la partita
     * @param result l'esito della partita (es. WIN, LOSE, TIMEOUT)
     * @param word la parola che era da indovinare
     */
    public HistoryItem(String date, String result, String word) {
        this.date = new SimpleStringProperty(date);
        this.result = new SimpleStringProperty(result);
        this.word = new SimpleStringProperty(word);
    }

    /**
     * @return la property della data
     */
    public StringProperty dateProperty() {
        return date;
    }

    /**
     * @return la property dell'esito
     */
    public StringProperty resultProperty() {
        return result;
    }

    /**
     * @return la property della parola
     */
    public StringProperty wordProperty() {
        return word;
    }

    /**
     * @return il valore in formato stringa della data
     */
    public String getDate() {
        return date.get();
    }

    /**
     * @return il valore in formato stringa dell'esito
     */
    public String getResult() {
        return result.get();
    }

    /**
     * @return il valore in formato stringa della parola
     */
    public String getWord() {
        return word.get();
    }
}
