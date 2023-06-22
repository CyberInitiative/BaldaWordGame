package com.example.baldawordgame;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;

public class GameCellsManager {

    private GameCell[] gameCells;
    private String gameCellsKey;

    public GameCellsManager(int gameGridSize, String gameRoomKey) {
        this.gameCellsKey = gameRoomKey;
        createGameCells(gameGridSize);
//        FirebaseDatabase.getInstance().getReference().child("gameCells").child(gameCellsKey).setValue(Arrays.asList(gameCells));
    }

    private void createGameCells(int gameGridSize) {
        gameCells = new GameCell[gameGridSize * gameGridSize];
        for (int i = 0; i < gameGridSize; i++) {
            for (int j = 0; j < gameGridSize; j++) {
                GameCell gameCell = new GameCell(i, j);
                DatabaseReference gameCellRef = FirebaseDatabase.getInstance().getReference()
                        .child("gameCells").child(gameCellsKey)
                        .child(String.valueOf((i * gameGridSize) + j));

                gameCellRef.setValue(gameCell);
                gameCellRef.child("letterInCell").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String dataFromSnapshot = snapshot.getValue(String.class);
                        if (dataFromSnapshot != null) {
                            gameCell.setLetterInCell(dataFromSnapshot);
                            gameCell.notifySubscriberAboutLetterChange();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                gameCellRef.child("cellState").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String dataFromSnapshot = snapshot.getValue(String.class);
                        if (dataFromSnapshot != null) {
                            if (gameCell.getCellState() != null) {
                                gameCell.setPreviousCellState(gameCell.getCellState());
                            }
                            gameCell.setCellState(dataFromSnapshot);
                            gameCell.notifySubscriberAboutStateChange();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


                gameCells[(i * gameGridSize) + j] = gameCell;
            }
        }
    }

    public void updateCellLetterFirebase(int cellIndexInArray, String letter) {
        FirebaseDatabase.getInstance().getReference().child("gameCells").
                child(gameCellsKey).child(String.valueOf(cellIndexInArray)).child("letterInCell")
                .setValue(String.valueOf(letter));
    }

    public void updateCellStateFirebase(int cellIndexInArray, String cellState) {
        FirebaseDatabase.getInstance().getReference().child("gameCells").
                child(gameCellsKey).child(String.valueOf(cellIndexInArray)).child("cellState")
                .setValue(cellState);
    }

    public void applyInitialWord(String initialWord) {
        if (initialWord != null && ((initialWord.length() * initialWord.length()) == gameCells.length)) {
            double d = initialWord.length() / 2;
            int position = (int) Math.round(d);
            for (int i = 0; i < initialWord.length(); i++) {
                for (int j = 0, gameCellsLength = gameCells.length; j < gameCellsLength; j++) {
                    GameCell gameCell = gameCells[j];
                    if (gameCell != null) {
                        if (gameCell.getRowIndex() == i && gameCell.getColumnIndex() == position) {
                            updateCellLetterFirebase(j, String.valueOf(initialWord.charAt(i)));
                            updateCellStateFirebase(j, GameCell.LETTER_CELL_WITH_LETTER_STATE);
//                            gameCell.setLetterInCell(String.valueOf(initialWord.charAt(i)));
//                            gameCell.notifySubscriberAboutLetterChange();
//                            gameCell.setCellState(GameCell.LETTER_CELL_WITH_LETTER_STATE);
//                            gameCell.notifySubscriberAboutStateChange();
                        }
                    }
                }
            }
        }
    }

    private void updateAvailableGameCells() {
        for (int i = 0; i < gameCells.length; i++) {
            int currColIndex = gameCells[i].getColumnIndex();
            int currRowIndex = gameCells[i].getRowIndex();

            GameCell gameCellFromLeft = getLetterCellByColumnAndRowIndex(currColIndex - 1, currRowIndex);
            if (gameCellFromLeft != null) {
                if (gameCellFromLeft.getCellState().equals(GameCell.LETTER_CELL_UNAVAILABLE_WITHOUT_LETTER_STATE)
                        && gameCells[i].getCellState().equals(GameCell.LETTER_CELL_WITH_LETTER_STATE)) {
                    gameCellFromLeft.setCellState(GameCell.LETTER_CELL_AVAILABLE_WITHOUT_LETTER_STATE);
                    gameCellFromLeft.notifySubscriberAboutStateChange();
                }
            }

            GameCell gameCellFromRight = getLetterCellByColumnAndRowIndex(currColIndex + 1, currRowIndex);
            if (gameCellFromRight != null) {
                if (gameCellFromRight.getCellState().equals(GameCell.LETTER_CELL_UNAVAILABLE_WITHOUT_LETTER_STATE)
                        && gameCells[i].getCellState().equals(GameCell.LETTER_CELL_WITH_LETTER_STATE)) {
                    gameCellFromRight.setCellState(GameCell.LETTER_CELL_AVAILABLE_WITHOUT_LETTER_STATE);
                    gameCellFromRight.notifySubscriberAboutStateChange();
                }
            }

            GameCell gameCellFromAbove = getLetterCellByColumnAndRowIndex(currColIndex, currRowIndex - 1);
            if (gameCellFromAbove != null) {
                if (gameCellFromAbove.getCellState().equals(GameCell.LETTER_CELL_UNAVAILABLE_WITHOUT_LETTER_STATE)
                        && gameCells[i].getCellState().equals(GameCell.LETTER_CELL_WITH_LETTER_STATE)) {
                    gameCellFromAbove.setCellState(GameCell.LETTER_CELL_AVAILABLE_WITHOUT_LETTER_STATE);
                    gameCellFromAbove.notifySubscriberAboutStateChange();
                }
            }

            GameCell gameCellFromBelow = getLetterCellByColumnAndRowIndex(currColIndex, currRowIndex + 1);
            if (gameCellFromBelow != null) {
                if (gameCellFromBelow.getCellState().equals(GameCell.LETTER_CELL_UNAVAILABLE_WITHOUT_LETTER_STATE)
                        && gameCells[i].getCellState().equals(GameCell.LETTER_CELL_WITH_LETTER_STATE)) {
                    gameCellFromBelow.setCellState(GameCell.LETTER_CELL_AVAILABLE_WITHOUT_LETTER_STATE);
                    gameCellFromBelow.notifySubscriberAboutStateChange();
                }
            }
        }
    }

    public boolean checkIfOneLetterIsCloseToAnother(GameCell currentGameCell, GameCell checkedGameCell) {
        if (currentGameCell != null && checkedGameCell != null) {
            if (currentGameCell.getRowIndex() + 1 == checkedGameCell.getRowIndex()
                    && currentGameCell.getColumnIndex() == checkedGameCell.getColumnIndex()) {
                //Cell is on the right;
                return true;
            } else if (currentGameCell.getRowIndex() - 1 == checkedGameCell.getRowIndex()
                    && currentGameCell.getColumnIndex() == checkedGameCell.getColumnIndex()) {
                //Cell is on the left;
                return true;
            } else if (currentGameCell.getColumnIndex() + 1 == checkedGameCell.getColumnIndex()
                    && currentGameCell.getRowIndex() == checkedGameCell.getRowIndex()) {
                //Cell is below;
                return true;
            } else
                return currentGameCell.getColumnIndex() - 1 == checkedGameCell.getColumnIndex()
                        && currentGameCell.getRowIndex() == checkedGameCell.getRowIndex();
        }
        return false;
    }

    public GameCell getLetterCellByColumnAndRowIndex(int columnIndex, int rowIndex) {
        for (GameCell gameCell : gameCells) {
            if (gameCell.getColumnIndex() == columnIndex
                    && gameCell.getRowIndex() == rowIndex) {
                return gameCell;
            }
        }
        return null;
    }

    public GameCell[] getGameCells() {
        return gameCells;
    }
}