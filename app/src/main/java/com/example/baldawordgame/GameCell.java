package com.example.baldawordgame;

import android.database.Observable;

import androidx.databinding.ObservableField;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.database.Exclude;

public class GameCell {
    private int columnIndex;
    private int rowIndex;
    private String letterInCell;
    private String cellState;
    private String previousCellState;
    private Subscriber subscriber;

    public interface Subscriber {
        void processUpdatedGameCellState(String cellState);
        void processUpdatedGameCellLetter(String letterInCell);
    }

    public GameCell() {
    }

    public GameCell(int columnIndex, int rowIndex) {
        this.columnIndex = columnIndex;
        this.rowIndex = rowIndex;
        cellState = LETTER_CELL_UNAVAILABLE_WITHOUT_LETTER_STATE;
        letterInCell = null;
    }

    public void notifySubscriberAboutStateChange(){
        subscriber.processUpdatedGameCellState(cellState);
    }

    public void notifySubscriberAboutLetterChange(){
        subscriber.processUpdatedGameCellLetter(letterInCell);
    }

    public int getColumnIndex() {
        return columnIndex;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public String getLetterInCell() {
        return letterInCell;
    }

    public void setLetterInCell(String letterInCell) {
        this.letterInCell = letterInCell;
    }

    public String getCellState() {
        return cellState;
    }

    public void setCellState(String cellState) {
        if(this.cellState != null){
            setPreviousCellState(this.cellState);
        }
        this.cellState = cellState;
    }

    public void setPreviousCellState(String previousCellState) {
        this.previousCellState = previousCellState;
    }

    public void getBackToPreviousState() {
        if (!previousCellState.equals(GameCell.LETTER_CELL_UNDEFINED_STATE)) {
            cellState = previousCellState;
        }
    }

    public void addSubscriber(Subscriber subscriber) {
        this.subscriber = subscriber;
    }

    public void removeSubscriber() {
        subscriber = null;
    }

    @Exclude
    public Subscriber getSubscriber() {
        return subscriber;
    }

    @Exclude
    public String getPreviousCellState() {
        return previousCellState;
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


    @Override
    public String toString() {
        return "GameCell{" +
                "columnIndex=" + columnIndex +
                ", rowIndex=" + rowIndex +
                ", letterInCell='" + letterInCell + '\'' +
                ", cellState='" + cellState + '\'' +
                ", previousCellState='" + previousCellState + '\'' +
                ", subscriber=" + subscriber +
                '}';
    }
}
