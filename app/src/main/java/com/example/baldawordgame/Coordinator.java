package com.example.baldawordgame;

import com.example.baldawordgame.model.GameProcessData;
import com.example.baldawordgame.model.GameRoom;
import com.google.android.gms.tasks.Task;

import java.util.Random;

public class Coordinator {

    private static final String TAG = "Coordinator";

    private GameRoom gameRoom;
    private GameProcessData gameProcessData;
    private GameVocabulary gameVocabulary;
    private GameBoard gameBoard;

    public void endTurn(TurnTerminationCode turnTerminationCode) {
        switch (turnTerminationCode) {
            case TIME_IS_UP:
                break;
            case TURN_SKIPPED:
                break;
            case COMBINATION_SUBMITTED:
                break;
        }

    }

    public void confirmCombination() {
        if (gameBoard.checkCombinationConditions()) {
            String word = gameBoard.makeUpWordFromCombination();
            if (gameVocabulary.checkWord(word).equals(GameVocabulary.WordCheckResult.NEW_FOUND_WORD)) {
                gameVocabulary.addWord(word);
                gameBoard.writeLetterCell();
                gameProcessData.writeActivePlayerKey(gameRoom.getOpponentKey());
            }
        }
    }

    public Task<Void> tossUp() {
        Task<Void> writeKeyTask;
        long seed = System.currentTimeMillis();
        Random random = new Random(seed);

        int randomNumber = random.nextInt(101);

        if (randomNumber % 2 == 0) {
            writeKeyTask = gameProcessData.writeActivePlayerKey(gameRoom.getHostUID());
        } else {
            writeKeyTask = gameProcessData.writeActivePlayerKey(gameRoom.getGuestUID());
        }
        return writeKeyTask;
    }

    public void setGameProcessData(GameProcessData gameProcessData) {
        this.gameProcessData = gameProcessData;
    }

    public void setGameVocabulary(GameVocabulary gameVocabulary) {
        this.gameVocabulary = gameVocabulary;
    }

    public void setGameBoard(GameBoard gameBoard) {
        this.gameBoard = gameBoard;
    }

    public void setGameRoom(GameRoom gameRoom) {
        this.gameRoom = gameRoom;
    }
}