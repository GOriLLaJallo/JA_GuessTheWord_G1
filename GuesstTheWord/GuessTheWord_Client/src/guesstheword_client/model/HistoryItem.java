package guesstheword_client.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class HistoryItem {

    private final StringProperty date;
    private final StringProperty result;
    private final StringProperty word;

    public HistoryItem(String date, String result, String word) {
        this.date = new SimpleStringProperty(date);
        this.result = new SimpleStringProperty(result);
        this.word = new SimpleStringProperty(word);
    }

    public StringProperty dateProperty() {
        return date;
    }

    public StringProperty resultProperty() {
        return result;
    }

    public StringProperty wordProperty() {
        return word;
    }

    public String getDate() {
        return date.get();
    }

    public String getResult() {
        return result.get();
    }

    public String getWord() {
        return word.get();
    }
}
