package com.example.baldawordgame.viewmodel;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.databinding.ObservableArrayList;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.baldawordgame.model.Dictionary;
import com.example.baldawordgame.model.FoundedWord;
import com.example.baldawordgame.GameVocabulary;
import com.example.baldawordgame.model.GameProcessData;
import com.example.baldawordgame.model.LetterCell;
import com.example.baldawordgame.LetterCellLiveData;
import com.example.baldawordgame.livedata.SnapshotLiveData;
import com.example.baldawordgame.TurnTerminationCode;
import com.example.baldawordgame.livedata.NewValueSnapshotLiveData;
import com.example.baldawordgame.model.User;
import com.example.baldawordgame.model.GameBoard;
import com.example.baldawordgame.model.GameRoom;
import com.example.baldawordgame.livedata.TurnTimerLiveData;
import com.example.baldawordgame.model.Turn;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class GameViewModel extends ViewModel {
    private static final String TAG = "GameViewModel";

    private final MutableLiveData<Boolean> gameRoomFetchedStatus = new MutableLiveData<>();
    private final MutableLiveData<Boolean> dataConsumedStatus = new MutableLiveData<>();

    private NewValueSnapshotLiveData<Integer> firstPlayerScoreSnapshotLiveData, secondPlayerScoreSnapshotLiveData;
    private NewValueSnapshotLiveData<String> dataStateNewValueSnapshotLiveData;
    private NewValueSnapshotLiveData<Turn> turnNewValueSnapshotLiveData;
    private NewValueSnapshotLiveData<Long> serverOffsetNewValueSnapshotLiveData =
            new NewValueSnapshotLiveData<>(FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset"), Long.class);

    private ArrayList<LetterCellLiveData> arrayListOfLetterCellLiveData;

    private GameRoom gameRoom;
    private GameBoard gameBoard;
    private GameVocabulary gameVocabulary;
    private GameProcessData gameProcessData;
    private TurnTimerLiveData turnTimerLiveData;

    public GameViewModel(@NonNull String gameRoomKey) {
        GameRoom.fetchGameRoom(gameRoomKey).addOnCompleteListener(task -> {
            gameRoom = task.getResult();

            gameBoard = new GameBoard(gameRoomKey, gameRoom.getGameBoardSize());
            gameVocabulary = new GameVocabulary(gameRoomKey);
            gameProcessData = new GameProcessData(gameRoomKey);

            turnTimerLiveData = new TurnTimerLiveData(gameRoom.getTurnDuration());

            this.dataStateNewValueSnapshotLiveData = gameRoom.getDataStateUniqueSnapshotLiveData();
            this.turnNewValueSnapshotLiveData = gameProcessData.getTurnUniqueSnapshotLiveData();

            this.firstPlayerScoreSnapshotLiveData = gameProcessData.getFirstPlayerScoreSnapshotLiveData();
            this.secondPlayerScoreSnapshotLiveData = gameProcessData.getSecondPlayerScoreSnapshotLiveData();

            gameRoomFetchedStatus.setValue(true);
        });
    }

    public Task<Void> writeScore(int score){
        if(gameRoom.getFirstPlayerUID().equals(User.getPlayerUid())){
            return gameProcessData.writeFirstPlayerScore(score);
        }
        return gameProcessData.writeSecondPlayerScore(score);
    }

    public void reactToGameStageUpdated(String gameStage) {
        if (gameStage != null) {
            if (User.getPlayerUid().equals(gameRoom.getFirstPlayerUID())) {
                hostReaction(gameStage);
            } else if (User.getPlayerUid().equals(gameRoom.getSecondPlayerUID())) {
                guestReaction(gameStage);
            }
        }
    }

    private void hostReaction(@NonNull String gameStage) {
        switch (gameStage) {
            case GameRoom.DataStatus.DATA_NOT_PREPARED:
                String randomWord = Dictionary.getRandomWordOfACertainLength(gameRoom.getGameBoardSize());

                Task<Void> initialWordWritingTask = gameVocabulary.writeInitialWord(randomWord);
                Task<Void> gameBoardCreationTask = gameBoard.createGameBoard(randomWord);

                Tasks.whenAll(initialWordWritingTask, gameBoardCreationTask /*,tossingUpTask*/).addOnCompleteListener(task -> {
                    gameRoom.writeDataState(GameRoom.DataStatus.DATA_PREPARED);
                });

                break;
            case GameRoom.DataStatus.DATA_PREPARED:
                if (dataConsumedStatus.getValue() == null || !dataConsumedStatus.getValue()) {
                    gameBoard.getGameBoard()
                            .continueWith(task -> arrayListOfLetterCellLiveData = gameBoard.getLetterCellLiveDataArrayList())
                            .addOnCompleteListener(task -> {
                                dataConsumedStatus.setValue(true);
                                String UID = gameRoom.tossUpWhoTurnsFirst();
                                gameProcessData.writeTurn(UID);
                            });
                }
                break;
        }
    }

    private void guestReaction(@NonNull String gameStage) {
        switch (gameStage) {
            case GameRoom.DataStatus.DATA_NOT_PREPARED:
                break;
            case GameRoom.DataStatus.DATA_PREPARED:
                Log.d(TAG, "prepared");
                if (dataConsumedStatus.getValue() == null || !dataConsumedStatus.getValue()) {
                    gameBoard.getGameBoard()
                            .continueWith(task ->
                                    arrayListOfLetterCellLiveData = gameBoard.getLetterCellLiveDataArrayList())
                            .continueWithTask(task -> gameVocabulary.fetchInitialWord())
                            .addOnCompleteListener(task -> dataConsumedStatus.setValue(true));
                }
                break;
        }
    }

    public ObservableArrayList<FoundedWord> getOpponentsVocabulary() {
        return gameVocabulary.getOpponentsVocabulary();
    }

    public ObservableArrayList<FoundedWord> getPlayersVocabulary() {
        return gameVocabulary.getPlayersVocabulary();
    }

    public ObservableArrayList<LetterCell> getLettersCombination() {
        return gameBoard.getLettersCombination();
    }

    public void turnOnVocabularyListener() {
        gameVocabulary.turnOnVocabularyListener();
    }

    public void turnOffVocabularyListener() {
        gameVocabulary.turnOffVocabularyListener();
    }

    private boolean confirmCombination() {
        if (gameBoard.checkCombinationConditions()) {
            String word = gameBoard.makeUpWordFromCombination();
            if (gameVocabulary.checkWord(word).equals(GameVocabulary.WordCheckResult.NEW_WORD_FOUNDED)) {
                gameVocabulary.addWord(word)
                        .continueWithTask(task -> gameBoard.writeLetterCell())
                        .continueWithTask(task -> writeScore(word.length()));
//                        .continueWithTask(task -> gameProcessData.writeActivePlayerKey(gameRoom.getOpponentKey()));
                return true;
            }
        }
        return false;
    }

    public boolean endTurn(TurnTerminationCode turnTerminationCode) {
        switch (turnTerminationCode) {
            case TIME_IS_UP:
                if (gameRoom.getTurn().getActivePlayerKey().equals(User.getPlayerUid())) {
                    gameBoard.eraseEverything();
                    gameProcessData.writeTurn(gameRoom.getOpponentKey());
                }
                break;
            case TURN_SKIPPED:
                gameBoard.eraseEverything();
                gameProcessData.writeTurn(gameRoom.getOpponentKey());
                break;
            case COMBINATION_SUBMITTED:
                if (confirmCombination()) {
                    return true;
                }
                break;
        }
        return false;
    }

    //region GETTERS AND SETTERS
    public GameRoom getGameRoom() {
        return gameRoom;
    }

    public GameBoard getGameBoard() {
        return gameBoard;
    }

    public GameProcessData getGameProcessData() {
        return gameProcessData;
    }

    public MutableLiveData<Boolean> getDataConsumedStatus() {
        return dataConsumedStatus;
    }

    public ArrayList<LetterCellLiveData> getArrayListOfLetterCellLiveData() {
        return arrayListOfLetterCellLiveData;
    }

    public NewValueSnapshotLiveData<String> getGameStageUniqueSnapshotLiveData() {
        return dataStateNewValueSnapshotLiveData;
    }

    public NewValueSnapshotLiveData<Turn> getTurnNewValueSnapshotLiveData() {
        return turnNewValueSnapshotLiveData;
    }

    public NewValueSnapshotLiveData<Long> getServerOffsetNewValueSnapshotLiveData() {
        return serverOffsetNewValueSnapshotLiveData;
    }

    public MutableLiveData<Boolean> getGameRoomFetchedStatus() {
        return gameRoomFetchedStatus;
    }

    public NewValueSnapshotLiveData<Integer> getFirstPlayerScoreSnapshotLiveData() {
        return firstPlayerScoreSnapshotLiveData;
    }

    public NewValueSnapshotLiveData<Integer> getSecondPlayerScoreSnapshotLiveData() {
        return secondPlayerScoreSnapshotLiveData;
    }

    public void setTurnInGameRoom(Turn turn) {
        gameRoom.setTurn(turn);
    }

    public TurnTimerLiveData getTurnTimer() {
        return turnTimerLiveData;
    }

    //endregion

    //region INNER CLASSES
    private class StringDeserializer implements Function<DataSnapshot, String> {
        @Override
        public String apply(DataSnapshot dataSnapshot) {
            return dataSnapshot.getValue(String.class);
        }
    }

    private class LongDeserializer implements Function<DataSnapshot, Long> {
        @Override
        public Long apply(DataSnapshot dataSnapshot) {
            return dataSnapshot.getValue(Long.class);
        }
    }

    private class IntegerDeserializer implements Function<DataSnapshot, Integer> {
        @Override
        public Integer apply(DataSnapshot dataSnapshot) {
            return dataSnapshot.getValue(Integer.class);
        }
    }
    //endregion
}