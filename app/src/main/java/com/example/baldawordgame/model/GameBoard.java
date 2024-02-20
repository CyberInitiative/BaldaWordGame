package com.example.baldawordgame.model;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableArrayList;

import com.example.baldawordgame.LetterCellCareTaker;
import com.example.baldawordgame.livedata.LetterCellLiveData;
import com.example.baldawordgame.LetterCellMemento;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameBoard {
    private static final String TAG = "GameBoard";

    public static final String GAME_BOARDS_PATH = "gameBoards";

    private static final DatabaseReference GAME_BOARDS = FirebaseDatabase.getInstance().getReference().child(GAME_BOARDS_PATH);
    private final DatabaseReference currentGameBoardRef;

    private final int gameBoardSize;
    private LetterCell currentLetterCell;
    private LetterCell intendedLetter;
    private HashMap<DatabaseReference, LetterCell> refToLetterCell;
    private final ObservableArrayList<LetterCell> lettersCombination = new ObservableArrayList<>();
    private final LetterCellCareTaker letterCellCareTaker = new LetterCellCareTaker();

    public GameBoard(@NonNull String gameRoomKey, int gameBoardSize) {
        this.gameBoardSize = gameBoardSize;
        currentGameBoardRef = GAME_BOARDS.child(gameRoomKey);
    }

    public Task<Void> createGameBoard(@NonNull String initialWord) {
        int initialWordRowPosition = initialWord.length() / 2;
        ArrayList<LetterCell> letterCells = new ArrayList<>();
        for (int row = 0; row < gameBoardSize; row++) {
            for (int column = 0; column < gameBoardSize; column++) {
                LetterCell letterCell;
                if (row == initialWordRowPosition) {
                    letterCell = new LetterCell(column, row, initialWord.charAt(column));
                } else if (row == initialWordRowPosition - 1 || row == initialWordRowPosition + 1) {
                    letterCell = new LetterCell(column, row, LetterCell.LETTER_CELL_AVAILABLE_WITHOUT_LETTER_STATE);
                } else {
                    letterCell = new LetterCell(column, row);
                }
                letterCells.add(letterCell);
            }
        }

        return currentGameBoardRef.setValue(letterCells);
    }

    public Task<Void> fetchGameBoard() {
        return currentGameBoardRef.get().continueWith(task -> {
            HashMap<DatabaseReference, LetterCell> hashMap = new HashMap<>();
            for (DataSnapshot snapshot : task.getResult().getChildren()) {
                DatabaseReference letterCellRef = snapshot.getRef();
                LetterCell letterCell = snapshot.getValue(LetterCell.class);
                hashMap.put(letterCellRef, letterCell);
            }

            refToLetterCell = hashMap;
            return null;
        });
    }

    public Task<Void> eraseGameBoard(){
        return currentGameBoardRef.removeValue();
    }

    public boolean checkIfThereIsAvailableLetterCell(){
        for(Map.Entry<DatabaseReference, LetterCell> letterCellEntry : refToLetterCell.entrySet()){
            LetterCell letterCell = letterCellEntry.getValue();
            if(letterCell.getState().equals(LetterCell.LETTER_CELL_AVAILABLE_WITHOUT_LETTER_STATE)){
                return true;
            }
        }
        return false;
    }

    public ArrayList<LetterCell> getLetterCellsArrayList(){
        ArrayList<LetterCell> letterCells = new ArrayList<>();
        for(Map.Entry<DatabaseReference, LetterCell> letterCellEntry : refToLetterCell.entrySet()){
            LetterCell letterCell = letterCellEntry.getValue();
            letterCells.add(letterCell);
        }
        return letterCells;
    }

    public ArrayList<LetterCellLiveData> getLetterCellLiveDataArrayList() {
        ArrayList<LetterCellLiveData> arrayListOfaLetterCellLiveData = new ArrayList<>();
        for (Map.Entry<DatabaseReference, LetterCell> entry : refToLetterCell.entrySet()) {
            LetterCellLiveData letterCellLiveData = new LetterCellLiveData(entry.getKey());
            arrayListOfaLetterCellLiveData.add(letterCellLiveData);
        }
        return arrayListOfaLetterCellLiveData;
    }

    public Task<Void> updateLetterCell(@NonNull LetterCell cell, @NonNull DatabaseReference cellRef) {
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(cellRef.getKey() + "/letter", cell.getLetter());
        childUpdates.put(cellRef.getKey() + "/state", cell.getState());

        return currentGameBoardRef.updateChildren(childUpdates);
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
        for (Map.Entry<DatabaseReference, LetterCell> entry : refToLetterCell.entrySet()) {
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
        for (Map.Entry<DatabaseReference, LetterCell> entry : refToLetterCell.entrySet()) {
            if (letterCell == entry.getValue()) {
                return entry.getKey();
            }
        }
        return null;
    }

    public void setIntendedLetterCellAsPartOfCombination(LetterCell letterCell) {
        writeMementoForLetterCell(letterCell);
        letterCell.setStateAndNotifySubscriber(LetterCell.LETTER_CELL_INTENDED_SELECTED_AS_PART_OF_COMBINATION_STATE);
        lettersCombination.add(letterCell);
    }

    public void setLetterCellAsPartOfCombination(LetterCell letterCell) {
        writeMementoForLetterCell(letterCell);
        letterCell.setStateAndNotifySubscriber(LetterCell.LETTER_CELL_SELECTED_AS_PART_OF_COMBINATION_STATE);
        lettersCombination.add(letterCell);
    }

    public boolean checkIfLetterIsPartOfCombination(int rowIndex, int columnIndex) {
        for (LetterCell letterCell : lettersCombination) {
            if (letterCell.getRowIndex() == rowIndex && letterCell.getColumnIndex() == columnIndex) {
                return true;
            }
        }
        return false;
    }

    public void writeMementoForLetterCell(@NonNull LetterCell letterCell) {
        DatabaseReference ref = getLetterCellRef(letterCell);
        LetterCellMemento memento = letterCell.saveStateToMemento();
        letterCellCareTaker.add(ref, memento);
    }

    public Task<Void> writeLetterCell() {
        List<Task<Void>> tasksList = new ArrayList<>();
        intendedLetter.setState(LetterCell.LETTER_CELL_WITH_LETTER_STATE);
        tasksList.add(updateLetterCell(intendedLetter, getLetterCellRef(intendedLetter)));
        ArrayList<LetterCell> updated = updateAvailableLetterCellsAround(intendedLetter);
        for (LetterCell updatedLetterCell : updated) {
            tasksList.add(updateLetterCell(updatedLetterCell, getLetterCellRef(updatedLetterCell)));
        }
        eraseEverything();
        return Tasks.whenAll(tasksList).continueWithTask(task -> {
            intendedLetter = null;
            return null;
        });

    }

    public void getMementoForLetterCell(@NonNull LetterCell letterCell) {
        DatabaseReference ref = getLetterCellRef(letterCell);
        LetterCellMemento memento = letterCellCareTaker.getLastMemento(ref);
        if (memento != null) {
            letterCell.getStateFromMemento(memento);
        }
    }

    public void eraseFromCombination(LetterCell targetLetterCell) {
        LetterCell letterCell = lettersCombination.get(lettersCombination.size() - 1);
        lettersCombination.remove(letterCell);
        getMementoForLetterCell(letterCell);
        letterCell.notifySubscriberAboutStateChange();
        letterCell.notifySubscriberAboutLetterChange();

        if (letterCell != targetLetterCell) {
            eraseFromCombination(targetLetterCell);
        }
        
    }

    public void eraseEverything() {
        if (!lettersCombination.isEmpty()) {
            LetterCell letterCell = lettersCombination.get(0);
            eraseFromCombination(letterCell);
        }
        getMementoForLetterCell(intendedLetter);
        if (intendedLetter != null) {
            intendedLetter.notifySubscriberAboutLetterChange();
            intendedLetter.notifySubscriberAboutStateChange();
            intendedLetter = null;
        }
    }

    public boolean checkCombinationConditions() {
        boolean intendedLetterIsHere = false;
        if (!lettersCombination.isEmpty()) {
            for (LetterCell letterCell : lettersCombination) {
                if (letterCell.getState().equals(LetterCell.LETTER_CELL_INTENDED_SELECTED_AS_PART_OF_COMBINATION_STATE)) {
                    intendedLetterIsHere = true;
                }
            }
            if (intendedLetterIsHere) {
                return true;
            }
        }
        return false;
    }

    public String makeUpWordFromCombination() {
        StringBuilder wordBuilder = new StringBuilder();
        for (LetterCell letterCell : lettersCombination) {
            wordBuilder.append(letterCell.getLetter());
        }
        return wordBuilder.toString().trim();
    }

    public ArrayList<LetterCell> updateAvailableLetterCellsAround(LetterCell letterCell) {
        int currColIndex = letterCell.getColumnIndex();
        int currRowIndex = letterCell.getRowIndex();
        ArrayList<LetterCell> updatedLetterCells = new ArrayList<>();

        LetterCell letterCellFromLeft = getLetterCellByRowAndColumn(currRowIndex, currColIndex - 1);
        if (letterCellFromLeft != null) {
            if (letterCellFromLeft.getState() != null) {
                if (letterCellFromLeft.getState().equals(LetterCell.LETTER_CELL_UNAVAILABLE_WITHOUT_LETTER_STATE)
                        && letterCell.getState().equals(LetterCell.LETTER_CELL_WITH_LETTER_STATE)) {
                    letterCellFromLeft.setState(LetterCell.LETTER_CELL_AVAILABLE_WITHOUT_LETTER_STATE);
                    letterCellFromLeft.notifySubscriberAboutStateChange();
                    updatedLetterCells.add(letterCellFromLeft);
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
                    updatedLetterCells.add(letterCellFromRight);
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
                    updatedLetterCells.add(letterCellFromAbove);
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
                    updatedLetterCells.add(gameCellFromBelow);
                }
            }
        }
        return updatedLetterCells;
    }

    //region GETTERS AND SETTERS

    public LetterCell getIntendedLetter() {
        return intendedLetter;
    }

    public void setIntendedLetter(LetterCell intendedLetter) {
        this.intendedLetter = intendedLetter;
    }

    public LetterCell getCurrentLetterCell() {
        return currentLetterCell;
    }

    public void setCurrentLetterCell(LetterCell currentLetterCell) {
        this.currentLetterCell = currentLetterCell;
    }

    public ObservableArrayList<LetterCell> getLettersCombination() {
        return lettersCombination;
    }

    public LetterCellCareTaker getLetterCellCareTaker() {
        return letterCellCareTaker;
    }

    public HashMap<DatabaseReference, LetterCell> getRefToLetterCell() {
        return refToLetterCell;
    }
    //endregion

}