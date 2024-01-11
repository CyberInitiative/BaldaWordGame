package com.example.baldawordgame.model;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.baldawordgame.LetterCellMemento;
import com.google.firebase.database.Exclude;

import java.util.Objects;

public class LetterCell {
    private final static String TAG = "LETTER_CELL";
    private int columnIndex;
    private int rowIndex;
    private String letter;
    private String state;
    private transient Subscriber subscriber;

    public interface Subscriber {
        void processUpdatedGameCellState(String cellState);

        void processUpdatedGameCellLetter(String letterInCell);
    }

    public LetterCell() {}

    public LetterCell(int columnIndex, int rowIndex) {
        this.columnIndex = columnIndex;
        this.rowIndex = rowIndex;
        this.state = LetterCell.LETTER_CELL_UNAVAILABLE_WITHOUT_LETTER_STATE;
        this.letter = LetterCell.NO_LETTER_PLUG;
    }

    public LetterCell(int columnIndex, int rowIndex, char letter) {
        this.columnIndex = columnIndex;
        this.rowIndex = rowIndex;
        this.state = LetterCell.LETTER_CELL_WITH_LETTER_STATE;
        this.letter = Character.toString(letter);
    }

    public LetterCell(int columnIndex, int rowIndex, @NonNull String state) {
        this.columnIndex = columnIndex;
        this.rowIndex = rowIndex;
        this.state = state;
        this.letter = LetterCell.NO_LETTER_PLUG;
    }

    public int getColumnIndex() {
        return columnIndex;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public void setLetter(String letter) {
        this.letter = letter;
    }

    public void setLetterAndNotifySubscriber(String letter) {
        this.letter = letter;
        notifySubscriberAboutLetterChange();
    }

    public String getLetter() {
        return letter;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setStateAndNotifySubscriber(String state) {
        this.state = state;
        notifySubscriberAboutStateChange();
    }

    public String getState() {
        return state;
    }

    public void addSubscriber(Subscriber subscriber) {
        this.subscriber = subscriber;
        Log.d(TAG, "addSubscriber(); LetterCell object got subscriber: " + subscriber);
    }

    public void removeSubscriber() {
        Log.d(TAG, "removeSubscriber(); Subscriber " + subscriber + " unsubscribed from the LetterCell object;");
        subscriber = null;
    }

    @Exclude
    public Subscriber getSubscriber() {
        return subscriber;
    }

    public LetterCellMemento saveStateToMemento() {
        return new LetterCellMemento(this.getState(), this.getLetter());
    }

    public void getStateFromMemento(@NonNull LetterCellMemento memento) {
        this.state = memento.getState();
        this.letter = memento.getLetter();
    }

    public void notifySubscriberAboutStateChange() {
        if (subscriber != null) {
            subscriber.processUpdatedGameCellState(state);
            Log.d(TAG, "notifySubscriberAboutStateChange(); Subscriber notified about state change;");
        } else {
            Log.w(TAG, "notifySubscriberAboutStateChange();  Subscriber not notified -- subscriber is null;");
        }
    }

    public void notifySubscriberAboutLetterChange() {
        if (subscriber != null) {
            subscriber.processUpdatedGameCellLetter(letter);
            Log.d(TAG, "notifySubscriberAboutLetterChange(); Subscriber notified about letter change;");
        } else {
            Log.w(TAG, "notifySubscriberAboutLetterChange(); Subscriber not notified -- subscriber is null;");
        }
    }

    @Exclude
    public static final String LETTER_CELL_AVAILABLE_WITHOUT_LETTER_STATE = "LETTER_CELL_AVAILABLE_WITHOUT_LETTER_STATE";
    @Exclude
    public static final String LETTER_CELL_UNAVAILABLE_WITHOUT_LETTER_STATE = "LETTER_CELL_UNAVAILABLE_WITHOUT_LETTER_STATE";
    @Exclude
    public static final String LETTER_CELL_WITH_LETTER_STATE = "LETTER_CELL_WITH_LETTER_STATE";
    @Exclude
    public static final String LETTER_CELL_SELECTED_AS_PART_OF_COMBINATION_STATE = "LETTER_CELL_SELECTED_AS_PART_OF_COMBINATION_STATE";
    //В эту клетку игрок вставил букву;
    @Exclude
    public static final String LETTER_CELL_INTENDED_STATE = "LETTER_CELL_INTENDED_STATE";
    //Клетка со вставленной игроком буквой стала частью комбинации букв, формирующих слово;
    @Exclude
    public static final String LETTER_CELL_INTENDED_SELECTED_AS_PART_OF_COMBINATION_STATE = "LETTER_CELL_INTENDED_SELECTED_AS_PART_OF_COMBINATION_STATE";
    @Exclude
    public static final String LETTER_CELL_UNDEFINED_STATE = "LETTER_CELL_UNDEFINED_STATE";
    @Exclude
    public static final String NO_LETTER_PLUG = "NO_LETTER_PLUG";

    @NonNull
    @Override
    public String toString() {
        return "LetterCell{" +
                "columnIndex=" + columnIndex +
                ", rowIndex=" + rowIndex +
                ", letter='" + letter + '\'' +
                ", state='" + state + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LetterCell that = (LetterCell) o;
        return columnIndex == that.columnIndex && rowIndex == that.rowIndex && Objects.equals(letter, that.letter) && Objects.equals(state, that.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(columnIndex, rowIndex, letter, state);
    }
}
