package com.example.baldawordgame;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class GameBoard {
    private static final String TAG = "GameBoard";

    private boolean newLetterAddedStatus;
    private LetterCell currentLetterCell;
    private HashMap<DatabaseReference, LetterCell> letterCellToRef;
    private LinkedList<LetterCell> lettersCombination = new LinkedList<>();
    private LetterCellCareTaker letterCellCareTaker = new LetterCellCareTaker();
    private Coordinator coordinator;

    public GameBoard(HashMap<DatabaseReference, LetterCell> letterCellToRef, Coordinator coordinator) {
        this.letterCellToRef = letterCellToRef;
        this.coordinator = coordinator;
    }

    public static boolean checkIfOneLetterIsCloseToAnother(@NonNull LetterCell currentCell, LetterCell allegedNeighbor) {
        if (allegedNeighbor != null) {
            if (currentCell.getRowIndex() + 1 == allegedNeighbor.getRowIndex()
                    && currentCell.getColumnIndex() == allegedNeighbor.getColumnIndex()) {
                //allegedNeighbor is on the right;
                return true;
            } else if (currentCell.getRowIndex() - 1 == allegedNeighbor.getRowIndex()
                    && currentCell.getColumnIndex() == allegedNeighbor.getColumnIndex()) {
                //allegedNeighbor is on the left;
                return true;
            } else if (currentCell.getColumnIndex() + 1 == allegedNeighbor.getColumnIndex()
                    && currentCell.getRowIndex() == allegedNeighbor.getRowIndex()) {
                //allegedNeighbor is below;
                return true;
            } else if (currentCell.getColumnIndex() - 1 == allegedNeighbor.getColumnIndex()
                    && currentCell.getRowIndex() == allegedNeighbor.getRowIndex()) {
                //allegedNeighbor is above;
                return true;
            } else {
                //not a neighbor;
                return false;
            }
        }
        return false;
    }

    public LetterCell getLetterCellByRowAndColumn(int row, int column) {
        for (Map.Entry<DatabaseReference, LetterCell> entry : letterCellToRef.entrySet()) {
            LetterCell letterCell = entry.getValue();
            if (letterCell.getRowIndex() == row && letterCell.getColumnIndex() == column) {
                Log.d(TAG, "getLetterCellByRowAndColumn(); return letterCell: " + letterCell);
                return letterCell;
            }
        }
        Log.d(TAG, "getLetterCellByRowAndColumn(); return null");
        return null;
    }

    public DatabaseReference getLetterCellRef(@NonNull LetterCell letterCell) {
        for (Map.Entry<DatabaseReference, LetterCell> entry : letterCellToRef.entrySet()) {
            if (letterCell == entry.getValue()) {
                return entry.getKey();
            }
        }
        return null;
    }

    public void setIntendedLetterCellAsPartOfCombination(LetterCell letterCell) {
        writeMementoForLetterCell(letterCell);
        letterCell.setStateAndNotifySubscriber(LetterCell.LETTER_CELL_INTENDED_SELECTED_AS_PART_OF_COMBINATION_STATE);
        lettersCombination.addLast(letterCell);
    }

    public void setLetterCellAsPartOfCombination(LetterCell letterCell) {
        writeMementoForLetterCell(letterCell);
        letterCell.setStateAndNotifySubscriber(LetterCell.LETTER_CELL_SELECTED_AS_PART_OF_COMBINATION_STATE);
        lettersCombination.addLast(letterCell);
    }

    public void writeMementoForLetterCell(@NonNull LetterCell letterCell) {
        DatabaseReference ref = getLetterCellRef(letterCell);
        LetterCellMemento memento = letterCell.saveStateToMemento();
        letterCellCareTaker.add(ref, memento);
    }

    public void getMementoForLetterCell(@NonNull LetterCell letterCell) {
        DatabaseReference ref = getLetterCellRef(letterCell);
        LetterCellMemento memento = letterCellCareTaker.getLastMemento(ref);
        letterCell.getStateFromMemento(memento);
    }

    public void eraseFromCombination(LetterCell targetLetterCell, ShowPanelAdapter showPanelAdapter) {
        LetterCell letterCell = lettersCombination.getLast();
        showPanelAdapter.notifyItemRemoved(lettersCombination.indexOf(letterCell));
        lettersCombination.remove(letterCell);
        getMementoForLetterCell(letterCell);
        letterCell.notifySubscriberAboutStateChange();

        if (letterCell != targetLetterCell) {
            eraseFromCombination(targetLetterCell, showPanelAdapter);
        }
    }

    public void eraseAllFromCombination(ShowPanelAdapter showPanelAdapter) {
        if (!lettersCombination.isEmpty()) {
            LetterCell letterCell = lettersCombination.getLast();
            showPanelAdapter.notifyItemRemoved(lettersCombination.indexOf(letterCell));
            lettersCombination.remove(letterCell);
            getMementoForLetterCell(letterCell);
            letterCell.notifySubscriberAboutStateChange();

            eraseAllFromCombination(showPanelAdapter);
        }
    }

    public void sendCombination(){
        if(checkConditions()){
            coordinator.checkCombinedWord(makeUpWordFromCombination());
        }
    }

    public boolean checkConditions() {
        boolean intendedLetterIsHere = false;
        if (!lettersCombination.isEmpty()) {
            for (LetterCell letterCell : lettersCombination) {
                if (letterCell.getState().equals(LetterCell.LETTER_CELL_INTENDED_SELECTED_AS_PART_OF_COMBINATION_STATE)) {
                    intendedLetterIsHere = true;
                }
            }
            if (intendedLetterIsHere) {
                coordinator.checkCombinedWord(makeUpWordFromCombination());
                return true;
            }
        }
        return false;
    }

    private String makeUpWordFromCombination() {
        StringBuilder wordBuilder = new StringBuilder();
        for (LetterCell letterCell : lettersCombination) {
            wordBuilder.append(letterCell.getLetter());
        }
        return wordBuilder.toString();
    }

    public void updateAvailableLetterCellsAround(LetterCell letterCell) {
        int currColIndex = letterCell.getColumnIndex();
        int currRowIndex = letterCell.getRowIndex();

        LetterCell letterCellFromLeft = getLetterCellByRowAndColumn(currRowIndex, currColIndex - 1);
        if (letterCellFromLeft != null) {
            if (letterCellFromLeft.getState() != null) {
                if (letterCellFromLeft.getState().equals(LetterCell.LETTER_CELL_UNAVAILABLE_WITHOUT_LETTER_STATE)
                        && letterCell.getState().equals(LetterCell.LETTER_CELL_WITH_LETTER_STATE)) {
                    letterCellFromLeft.setState(LetterCell.LETTER_CELL_AVAILABLE_WITHOUT_LETTER_STATE);
                    letterCellFromLeft.notifySubscriberAboutStateChange();
                }
            }
        }

        LetterCell letterCellFromRight = getLetterCellByRowAndColumn(currRowIndex, currColIndex + 1);
        if (letterCellFromRight != null) {
            if (letterCellFromRight.getState() != null) {
                if (letterCellFromRight.getState().equals(LetterCell.LETTER_CELL_UNAVAILABLE_WITHOUT_LETTER_STATE)
                        && letterCell.getState().equals(LetterCell.LETTER_CELL_WITH_LETTER_STATE)) {
                    letterCellFromRight.setState(LetterCell.LETTER_CELL_AVAILABLE_WITHOUT_LETTER_STATE);
                    letterCellFromRight.notifySubscriberAboutStateChange();
                }
            }
        }

        LetterCell letterCellFromAbove = getLetterCellByRowAndColumn(currRowIndex - 1, currColIndex);
        if (letterCellFromAbove != null) {
            if (letterCellFromAbove.getState() != null) {
                if (letterCellFromAbove.getState().equals(LetterCell.LETTER_CELL_UNAVAILABLE_WITHOUT_LETTER_STATE)
                        && letterCell.getState().equals(LetterCell.LETTER_CELL_WITH_LETTER_STATE)) {
                    letterCellFromAbove.setState(LetterCell.LETTER_CELL_AVAILABLE_WITHOUT_LETTER_STATE);
                    letterCellFromAbove.notifySubscriberAboutStateChange();
                }
            }
        }

        LetterCell gameCellFromBelow = getLetterCellByRowAndColumn(currRowIndex + 1, currColIndex);
        if (gameCellFromBelow != null) {
            if (gameCellFromBelow.getState() != null) {
                if (gameCellFromBelow.getState().equals(LetterCell.LETTER_CELL_UNAVAILABLE_WITHOUT_LETTER_STATE)
                        && letterCell.getState().equals(LetterCell.LETTER_CELL_WITH_LETTER_STATE)) {
                    gameCellFromBelow.setState(LetterCell.LETTER_CELL_AVAILABLE_WITHOUT_LETTER_STATE);
                    gameCellFromBelow.notifySubscriberAboutStateChange();
                }
            }
        }
    }

    public void updateAvailableLetterCells() {
        for (Map.Entry<DatabaseReference, LetterCell> entry : letterCellToRef.entrySet()) {
            updateAvailableLetterCellsAround(entry.getValue());
        }
    }

    public boolean isNewLetterAddedStatus() {
        return newLetterAddedStatus;
    }

    public void setNewLetterAddedStatus(boolean newLetterAddedStatus) {
        this.newLetterAddedStatus = newLetterAddedStatus;
    }

    public LetterCell getCurrentLetterCell() {
        return currentLetterCell;
    }

    public void setCurrentLetterCell(LetterCell currentLetterCell) {
        this.currentLetterCell = currentLetterCell;
    }

    public LinkedList<LetterCell> getLettersCombination() {
        return lettersCombination;
    }

    public LetterCellCareTaker getLetterCellCareTaker() {
        return letterCellCareTaker;
    }

}