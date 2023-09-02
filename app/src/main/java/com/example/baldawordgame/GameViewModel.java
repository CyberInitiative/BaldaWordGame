package com.example.baldawordgame;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.databinding.ObservableArrayList;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.baldawordgame.model.GameProcessData;
import com.example.baldawordgame.model.GameRoom;
import com.example.baldawordgame.model.Timer;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Random;

public class GameViewModel extends ViewModel {

    private static final String TAG = "GameViewModel";

    //region FirebaseQueryLiveData OBJECTS
    private FirebaseQueryLiveData gameStateFirebaseQueryLiveData;
    private FirebaseQueryLiveData activePlayerKeyFirebaseQueryLiveData;
    //endregion

    //region LiveData OBJECTS
    private LiveData<String> gameStateLiveData;
    private LiveData<String> activePlayerKeyLiveData;
    private LiveData<Long> intervalLiveData;
    //endregion

    private final MutableLiveData<Boolean> dataLoadedStateLiveData = new MutableLiveData<>();
    private ArrayList<LetterCellLiveData> arrayListOfLetterCellLiveData;

    private Coordinator coordinator;
    private GameRoom gameRoom;
    private GameProcessData gameProcessData;
    private GameBoard gameBoard;
    private GameVocabulary gameVocabulary;
    private Timer timer;

    public GameViewModel(@NonNull String gameRoomKey) {
        coordinator = new Coordinator();
        GameRoom.fetchGameRoom(gameRoomKey).addOnCompleteListener(task -> {
            gameRoom = task.getResult();

            gameBoard = new GameBoard(gameRoomKey, gameRoom.getGameBoardSize(), coordinator);
            gameVocabulary = new GameVocabulary(gameRoomKey, coordinator);
            gameProcessData = new GameProcessData(gameRoomKey);
            timer = new Timer(gameRoomKey, gameRoom.getTurnDuration());

            coordinator.setGameProcessData(gameProcessData);
            coordinator.setGameVocabulary(gameVocabulary);
            coordinator.setGameBoard(gameBoard);
            coordinator.setGameRoom(gameRoom);

            gameStateFirebaseQueryLiveData = gameProcessData.getGameStateFirebaseQueryLiveData();
            activePlayerKeyFirebaseQueryLiveData = gameProcessData.getActivePlayerKeyFirebaseQueryLiveData();

            gameStateLiveData = Transformations.map(gameStateFirebaseQueryLiveData, new StringDeserializer());
            activePlayerKeyLiveData = Transformations.map(activePlayerKeyFirebaseQueryLiveData, new StringDeserializer());
            intervalLiveData = timer.getInterval();

            if (gameRoom.getHostUID().equals(User.getPlayerUid())) {
                hostGameSetting();
            } else if (gameRoom.getGuestUID().equals(User.getPlayerUid())) {
                guestGameSetting();
            }

        });
    }

    private void hostGameSetting() {
        String randomWord = Dictionary.getRandomWordOfACertainLength(gameRoom.getGameBoardSize());
        gameVocabulary.writeInitialWord(randomWord)
                .continueWithTask(task -> gameBoard.writeGameBoard())
                .continueWithTask(task -> gameBoard.writeInitialWordInGameBoard(gameVocabulary.getInitialWord()))
                .continueWithTask(task -> {
                    arrayListOfLetterCellLiveData = gameBoard.getFirebaseQueryLetterCellLiveData();
                    return gameProcessData.writeDataState(GameProcessData.DATA_PREPARED_STATE);
                })
                .addOnCompleteListener(task -> {
                    ValueEventListener guestStateValueListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String data = snapshot.getValue(String.class);
                            if (data != null && data.equals(GameProcessData.GUEST_IS_READY)) {
                                gameProcessData.removeGuestStateValueListener();
                                dataLoadedStateLiveData.setValue(true);
                                tossUpFirstTurn().continueWithTask(task -> timer.writeTurnStartedAt());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    };

                    gameProcessData.addGuestStateValueListener(guestStateValueListener);

                });
    }

    private void guestGameSetting() {
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String data = snapshot.getValue(String.class);
                if (data != null && data.equals(GameProcessData.DATA_PREPARED_STATE)) {
                    gameVocabulary.fetchInitialWord()
                            .continueWithTask(task -> gameBoard.fetchGameBoard())
                            .continueWithTask(task -> {
                                arrayListOfLetterCellLiveData = gameBoard.getFirebaseQueryLetterCellLiveData();
                                return gameProcessData.writeGuestState(GameProcessData.GUEST_IS_READY);
                            })
                            .addOnCompleteListener(task -> dataLoadedStateLiveData.setValue(true));
                    gameProcessData.removeDataStateValueListener();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
        gameProcessData.addDataStateValueListener(valueEventListener);
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

    private void confirmCombination() {
        if (gameBoard.checkCombinationConditions()) {
            String word = gameBoard.makeUpWordFromCombination();
            if (gameVocabulary.checkWord(word).equals(GameVocabulary.WordCheckResult.NEW_FOUND_WORD)) {
                gameVocabulary.addWord(word)
                        .continueWithTask(task -> gameBoard.writeLetterCell())
                        .continueWithTask(task -> gameProcessData.writeActivePlayerKey(gameRoom.getOpponentKey()));
            }
        }
    }

    private Task<Void> tossUpFirstTurn() {
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

    public void endTurn(TurnTerminationCode turnTerminationCode) {
        switch (turnTerminationCode) {
            case TIME_IS_UP:
                gameBoard.eraseEverything();

                if (activePlayerKeyLiveData.getValue() != null && activePlayerKeyLiveData.getValue().equals(User.getPlayerUid())) {
                    gameProcessData.writeActivePlayerKey(gameRoom.getOpponentKey())
                            .continueWithTask(task -> timer.writeTurnStartedAt());
                }
                break;
            case TURN_SKIPPED:
                gameBoard.eraseEverything();
                gameProcessData.writeActivePlayerKey(gameRoom.getOpponentKey());
                break;
            case COMBINATION_SUBMITTED:
                confirmCombination();
                break;
        }

    }

    //region GETTERS AND SETTERS
    public GameRoom getGameRoom() {
        return gameRoom;
    }

    public GameBoard getGameBoard() {
        return gameBoard;
    }

    public MutableLiveData<Boolean> getDataLoadedStateLiveData() {
        return dataLoadedStateLiveData;
    }

    public ArrayList<LetterCellLiveData> getArrayListOfLetterCellLiveData() {
        return arrayListOfLetterCellLiveData;
    }

    public FirebaseQueryLiveData getGameStateFirebaseQueryLiveData() {
        return gameStateFirebaseQueryLiveData;
    }

    public FirebaseQueryLiveData getActivePlayerKeyFirebaseQueryLiveData() {
        return activePlayerKeyFirebaseQueryLiveData;
    }

    public LiveData<String> getGameStateLiveData() {
        return gameStateLiveData;
    }

    public LiveData<String> getActivePlayerKeyLiveData() {
        return activePlayerKeyLiveData;
    }

    public LiveData<Long> getIntervalLiveData() {
        return intervalLiveData;
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