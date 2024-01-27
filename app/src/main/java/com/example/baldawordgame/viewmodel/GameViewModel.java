package com.example.baldawordgame.viewmodel;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.databinding.ObservableArrayList;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.baldawordgame.model.Dictionary;
import com.example.baldawordgame.model.FoundWord;
import com.example.baldawordgame.model.GameVocabulary;
import com.example.baldawordgame.model.GameProcessData;
import com.example.baldawordgame.model.LetterCell;
import com.example.baldawordgame.livedata.LetterCellLiveData;
import com.example.baldawordgame.TurnTerminationCode;
import com.example.baldawordgame.livedata.NewValueSnapshotLiveData;
import com.example.baldawordgame.model.Rematch;
import com.example.baldawordgame.model.User;
import com.example.baldawordgame.model.GameBoard;
import com.example.baldawordgame.model.GameRoom;
import com.example.baldawordgame.livedata.TimerLiveData;
import com.example.baldawordgame.model.Turn;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class GameViewModel extends ViewModel {
    private static final String TAG = "GameViewModel";

    //region LIVEDATA OBJECTS
    private final MutableLiveData<Boolean> gameRoomFetchedStatus = new MutableLiveData<>();
    private final MutableLiveData<Boolean> dataConsumedStatus = new MutableLiveData<>();
    private NewValueSnapshotLiveData<Integer> firstPlayerScoreNewValueSnapshotLiveData, secondPlayerScoreNewValueSnapshotLiveData;
    private NewValueSnapshotLiveData<Integer> firstPlayerSkippedTurnsNewValueSnapshotLiveData, secondPlayerSkippedTurnsNewValueSnapshotLiveData;
    private NewValueSnapshotLiveData<Integer> firstPlayerStatusCodeNewValueSnapshotLiveData, secondPlayerStatusCodeNewValueSnapshotLiveData;
    private NewValueSnapshotLiveData<String> dataStateNewValueSnapshotLiveData;
    private NewValueSnapshotLiveData<Turn> turnNewValueSnapshotLiveData;

    private NewValueSnapshotLiveData<Long> serverOffsetNewValueSnapshotLiveData =
            new NewValueSnapshotLiveData<>(FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset"), Long.class);
    private TimerLiveData timerLiveData;
    private ArrayList<LetterCellLiveData> arrayListOfLetterCellLiveData;
    //endregion

    private GameRoom gameRoom;
    private GameBoard gameBoard;
    private GameVocabulary gameVocabulary;
    private GameProcessData gameProcessData;

    public GameViewModel(@NonNull String gameRoomKey) {
        GameRoom.fetchGameRoom(gameRoomKey).addOnCompleteListener(task -> {
            gameRoom = task.getResult();

            gameBoard = new GameBoard(gameRoomKey, gameRoom.getGameBoardSize());
            gameVocabulary = new GameVocabulary(gameRoomKey);
            gameProcessData = new GameProcessData(gameRoomKey);

            timerLiveData = new TimerLiveData(gameRoom.getTurnDuration());

            this.dataStateNewValueSnapshotLiveData = gameRoom.getDataStateUniqueSnapshotLiveData();
            this.turnNewValueSnapshotLiveData = gameProcessData.getTurnUniqueNewValueSnapshotLiveData();

            this.firstPlayerScoreNewValueSnapshotLiveData = gameProcessData.getFirstPlayerScoreNewValueSnapshotLiveData();
            this.secondPlayerScoreNewValueSnapshotLiveData = gameProcessData.getSecondPlayerScoreNewValueSnapshotLiveData();

            this.firstPlayerSkippedTurnsNewValueSnapshotLiveData = gameProcessData.getFirstPlayerSkippedTurnsNewValueSnapshotLiveData();
            this.secondPlayerSkippedTurnsNewValueSnapshotLiveData = gameProcessData.getSecondPlayerSkippedTurnsNewValueSnapshotLiveData();

//            this.firstPlayerStatusCodeNewValueSnapshotLiveData = gameProcessData.getFirstPlayerStatusCodeNewValueSnapshotLiveData();
//            this.secondPlayerStatusCodeNewValueSnapshotLiveData = gameProcessData.getSecondPlayerStatusCodeNewValueSnapshotLiveData();

            gameRoomFetchedStatus.setValue(true);
        });
    }

    public void reactToGameStageUpdated(String gameStage) {
        if (gameStage != null) {
            if (User.fetchPlayerUID().equals(gameRoom.getFirstPlayerUID())) {
                hostReaction(gameStage);
            } else if (User.fetchPlayerUID().equals(gameRoom.getSecondPlayerUID())) {
                guestReaction(gameStage);
            }
        }
    }

    private void hostReaction(@NonNull String gameStage) {
        switch (gameStage) {
            case GameRoom.DataStatus.DATA_NOT_PREPARED:
                String randomWord = Dictionary.getRandomWordOfACertainLength(gameRoom.getGameBoardSize());

                Task<Void> initialWordWritingTask = gameVocabulary.addWord(randomWord, FoundWord.INITIAL_WORD_KEY);
                Task<Void> gameBoardCreationTask = gameBoard.createGameBoard(randomWord);

                Tasks.whenAll(initialWordWritingTask, gameBoardCreationTask /*,tossingUpTask*/).addOnCompleteListener(task -> {
                    gameRoom.writeDataState(GameRoom.DataStatus.DATA_PREPARED);
                });

                break;
            case GameRoom.DataStatus.DATA_PREPARED:
                if (dataConsumedStatus.getValue() == null || !dataConsumedStatus.getValue()) {
                    gameBoard.fetchGameBoard()
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
                    gameBoard.fetchGameBoard()
                            .continueWith(task ->
                                    arrayListOfLetterCellLiveData = gameBoard.getLetterCellLiveDataArrayList())
                            .addOnCompleteListener(task -> dataConsumedStatus.setValue(true));
                }
                break;
        }
    }

    public Task<Void> writeScore(int score) {
        if (gameRoom.getFirstPlayerUID().equals(User.fetchPlayerUID())) {
            return gameProcessData.writeFirstPlayerScore(score);
        } else if (gameRoom.getSecondPlayerUID().equals(User.fetchPlayerUID())) {
            return gameProcessData.writeSecondPlayerScore(score);
        }
       return null;
    }

    public Task<Void> writeRematchData(){
        if (gameRoom.getFirstPlayerUID().equals(User.fetchPlayerUID())) {
            return gameProcessData.writeRematchOffering(Rematch.firstPlayerOfferedRematch());
        } else if (gameRoom.getSecondPlayerUID().equals(User.fetchPlayerUID())) {
            return gameProcessData.writeRematchOffering(Rematch.secondPlayerOfferedRematch());
        }
        return null;
    }

    private boolean confirmCombination() {
        if (gameBoard.checkCombinationConditions()) {
            String word = gameBoard.makeUpWordFromCombination();
            if (gameVocabulary.checkWord(word).equals(GameVocabulary.WordCheckResult.NEW_WORD_FOUNDED)) {
                gameVocabulary.addWord(word, User.fetchPlayerUID())
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
                if (gameProcessData.getTurn().getActivePlayerKey().equals(User.fetchPlayerUID())) {
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
                    gameProcessData.writeTurn(gameRoom.getOpponentKey());
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                        Thread thread = new Thread(new Runnable() {
//                            @Override
//                            public void run() {
//                                GameAnalyzer.checkIfThereAreAvailableTurns(gameBoard.getLetterCellsArrayList(), gameVocabulary.getFoundWords());
//                            }
//                        });
//                        thread.start();
//                    }
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

    public GameVocabulary getGameVocabulary() {
        return gameVocabulary;
    }

    public LiveData<Boolean> getDataConsumedStatus() {
        return dataConsumedStatus;
    }

    public LiveData<String> getGameStageUniqueSnapshotLiveData() {
        return dataStateNewValueSnapshotLiveData;
    }

    public LiveData<Boolean> getGameRoomFetchedStatus() {
        return gameRoomFetchedStatus;
    }

    public LiveData<Integer> getFirstPlayerScoreNewValueSnapshotLiveData() {
        return firstPlayerScoreNewValueSnapshotLiveData;
    }

    public LiveData<Integer> getFirstPlayerSkippedTurnsNewValueSnapshotLiveData() {
        return firstPlayerSkippedTurnsNewValueSnapshotLiveData;
    }

    public LiveData<Integer> getSecondPlayerScoreNewValueSnapshotLiveData() {
        return secondPlayerScoreNewValueSnapshotLiveData;
    }

    public LiveData<Integer> getSecondPlayerSkippedTurnsNewValueSnapshotLiveData() {
        return secondPlayerSkippedTurnsNewValueSnapshotLiveData;
    }

    public NewValueSnapshotLiveData<Turn> getTurnNewValueSnapshotLiveData() {
        return turnNewValueSnapshotLiveData;
    }

    public NewValueSnapshotLiveData<Long> getServerOffsetNewValueSnapshotLiveData() {
        return serverOffsetNewValueSnapshotLiveData;
    }

//    public NewValueSnapshotLiveData<Integer> getFirstPlayerStatusCodeNewValueSnapshotLiveData() {
//        return firstPlayerStatusCodeNewValueSnapshotLiveData;
//    }

//    public NewValueSnapshotLiveData<Integer> getSecondPlayerStatusCodeNewValueSnapshotLiveData() {
//        return secondPlayerStatusCodeNewValueSnapshotLiveData;
//    }

    public TimerLiveData getTurnTimer() {
        return timerLiveData;
    }

    public ArrayList<LetterCellLiveData> getArrayListOfLetterCellLiveData() {
        return arrayListOfLetterCellLiveData;
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