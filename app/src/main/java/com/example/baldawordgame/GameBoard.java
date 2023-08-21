package com.example.baldawordgame;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableArrayList;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class GameBoard {
    private static final String TAG = "GameBoard";
    private static final DatabaseReference GAME_BOARDS = FirebaseDatabase.getInstance().getReference().child("gameBoards");

    private final String gameRoomKey;
    private LetterCell currentLetterCell;
    private LetterCell intendedLetter;
    private HashMap<DatabaseReference, LetterCell> letterCellToRef;
    private final ObservableArrayList<LetterCell> lettersCombination = new ObservableArrayList<>();
    private final LetterCellCareTaker letterCellCareTaker = new LetterCellCareTaker();
    private Coordinator coordinator;

    public GameBoard(String gameRoomKey, Coordinator coordinator) {
        this.gameRoomKey = gameRoomKey;
        this.coordinator = coordinator;
    }

    //region methods to access Firebase data;
    public Task<Void> writeGameBoard(int gameBoardSize) {
        DatabaseReference ref = GAME_BOARDS.child(gameRoomKey);

        this.letterCellToRef = new HashMap<>();
        ArrayList<Task<Void>> tasks = new ArrayList<>();

        for (int i = 0; i < gameBoardSize; i++) { //rows
            for (int j = 0; j < gameBoardSize; j++) { //columns
                LetterCell letterCell = new LetterCell(j, i);
                DatabaseReference letterCellKey = ref.push();
                letterCellToRef.put(letterCellKey, letterCell);

                Task<Void> setValueTask = letterCellKey.setValue(letterCell);
                tasks.add(setValueTask);
            }
        }

        return Tasks.whenAll(tasks).continueWith(task -> null);
    }

    public Task<Void> fetchGameBoard() {
        DatabaseReference ref = GAME_BOARDS.child(gameRoomKey);

        Task<DataSnapshot> fetchTask = ref.get();
        return fetchTask.continueWith(task -> {
            HashMap<DatabaseReference, LetterCell> letterCellToRef = new HashMap<>();

            DataSnapshot snapshot = task.getResult();
            for (DataSnapshot child : snapshot.getChildren()) {
                LetterCell letterCell = child.getValue(LetterCell.class);
                DatabaseReference letterCellRef = child.getRef();
                letterCellToRef.put(letterCellRef, letterCell);
            }
            GameBoard.this.letterCellToRef = letterCellToRef;
            return null;
        });
    }

    public Task<Void> updateLetterCell(@NonNull LetterCell cell, @NonNull DatabaseReference cellRef) {
        DatabaseReference path = FirebaseDatabase.getInstance().getReference().child("gameBoards").child(gameRoomKey);

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(cellRef.getKey() + "/letter", cell.getLetter());
        childUpdates.put(cellRef.getKey() + "/state", cell.getState());

        return path.updateChildren(childUpdates);
    }

    public Task<Void> writeInitialWordInGameBoard(@NonNull String word) {
        List<LetterCell> letterCellsToUpdate = new ArrayList<>();
        List<Task<Void>> tasks = new ArrayList<>();
        if ((word.length() * word.length()) == letterCellToRef.size()) {
            int rowPosition = word.length() / 2;
            List<LetterCell> wordLetterCells = new ArrayList<>();
            for (int i = 0; i < word.length(); i++) {
                LetterCell letterCell = getLetterCellByRowAndColumn(rowPosition, i);
                letterCell.setLetter(String.valueOf(word.charAt(i)));
                letterCell.setState(LetterCell.LETTER_CELL_WITH_LETTER_STATE);
                wordLetterCells.add(letterCell);
            }
            for (LetterCell letterCell : wordLetterCells) {
                ArrayList<LetterCell> result = updateAvailableLetterCellsAround(letterCell);
                letterCellsToUpdate.addAll(result);
                letterCellsToUpdate.add(letterCell);
            }

            for (LetterCell letterCell : letterCellsToUpdate) {
                DatabaseReference letterCellRef = getLetterCellRef(letterCell);
                Task<Void> task = updateLetterCell(letterCell, letterCellRef);

                tasks.add(task);
            }
        }
        return Tasks.whenAll(tasks).continueWith(task -> null);
    }
    //endregion

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

    public ArrayList<LetterCellLiveData> getFirebaseQueryLetterCellLiveData() {
        ArrayList<LetterCellLiveData> arrayListOfaLetterCellLiveData = new ArrayList<>();
        for (Map.Entry<DatabaseReference, LetterCell> entry : letterCellToRef.entrySet()) {
            LetterCellLiveData letterCellLiveData = new LetterCellLiveData(entry.getKey());
            arrayListOfaLetterCellLiveData.add(letterCellLiveData);
        }
        return arrayListOfaLetterCellLiveData;
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

    public void getMementoForLetterCell(@NonNull LetterCell letterCell) {
        DatabaseReference ref = getLetterCellRef(letterCell);
        LetterCellMemento memento = letterCellCareTaker.getLastMemento(ref);
        letterCell.getStateFromMemento(memento);
    }

    public void eraseFromCombination(LetterCell targetLetterCell) {
        LetterCell letterCell = lettersCombination.get(lettersCombination.size() - 1);
        lettersCombination.remove(letterCell);
        getMementoForLetterCell(letterCell);
        letterCell.notifySubscriberAboutStateChange();

        if (letterCell != targetLetterCell) {
            eraseFromCombination(targetLetterCell);
        }
    }


    public Task<Void> writeLetterCell() {
        List<Task<Void>> tasksList = new ArrayList<>();
        eraseAllFromCombination();
        intendedLetter.setState(LetterCell.LETTER_CELL_WITH_LETTER_STATE);
        tasksList.add(updateLetterCell(intendedLetter, getLetterCellRef(intendedLetter)));
        ArrayList<LetterCell> updated = updateAvailableLetterCellsAround(intendedLetter);
        for (LetterCell updatedLetterCell : updated) {
            tasksList.add(updateLetterCell(updatedLetterCell, getLetterCellRef(updatedLetterCell)));
        }
        return Tasks.whenAll(tasksList).continueWithTask(task -> {
            intendedLetter = null;
            return null;
        });
    }

    public void eraseAllFromCombination() {
        if (!lettersCombination.isEmpty()) {
            LetterCell letterCell = lettersCombination.get(0);
            eraseFromCombination(letterCell);
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

    //region getters and setters

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

    public HashMap<DatabaseReference, LetterCell> getLetterCellToRef() {
        return letterCellToRef;
    }
    //endregion
}