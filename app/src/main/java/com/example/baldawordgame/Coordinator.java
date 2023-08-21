package com.example.baldawordgame;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.baldawordgame.model.GameProcessData;
import com.example.baldawordgame.model.GameRoom;

public class Coordinator {

    private static final String TAG = "Coordinator";

    private GameRoom gameRoom;
    private GameProcessData gameProcessData;
    private GameVocabulary gameVocabulary;
    private GameBoard gameBoard;

    @NonNull
    public String requestGameRoomKey() {
        return gameRoom.getGameRoomKey();
    }

    public void confirmCombination() {
        if (gameBoard.checkCombinationConditions()) {
            String word = gameBoard.makeUpWordFromCombination();
            if (gameVocabulary.checkWord(word).equals(GameVocabulary.WordCheckResult.NEW_FOUND_WORD)) {
                gameVocabulary.addWord(word);
                gameBoard.writeLetterCell();
            }
        }
    }

    //region GETTERS_AND_SETTERS
    public GameProcessData getGameProcessData() {
        return gameProcessData;
    }

    public void setGameProcessData(GameProcessData gameProcessData) {
        this.gameProcessData = gameProcessData;
    }

    public GameVocabulary getGameVocabulary() {
        return gameVocabulary;
    }

    public void setGameVocabulary(GameVocabulary gameVocabulary) {
        this.gameVocabulary = gameVocabulary;
    }

    public GameBoard getGameBoard() {
        return gameBoard;
    }

    public void setGameBoard(GameBoard gameBoard) {
        this.gameBoard = gameBoard;
    }

    public GameRoom getGameRoom() {
        return gameRoom;
    }

    public void setGameRoom(GameRoom gameRoom) {
        this.gameRoom = gameRoom;
    }

    //endregion
}