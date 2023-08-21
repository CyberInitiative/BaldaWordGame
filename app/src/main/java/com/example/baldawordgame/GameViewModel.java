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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class GameViewModel extends ViewModel {

    private static final String TAG = "GameViewModel";

    //region FirebaseQueryLiveData objects
    private FirebaseQueryLiveData gameStateFirebaseQueryLiveData;
    private FirebaseQueryLiveData keyOfPlayerWhoseTurnItFirebaseQueryLiveData;
    private FirebaseQueryLiveData turnTimeLeftInMillisFirebaseQueryLiveData;
    //endregion

    //region LiveData objects
    private LiveData<String> gameStateLiveData;
    private LiveData<String> keyOfPlayerWhoseTurnItLiveData;
    private LiveData<Long> turnTimeLeftInMillisLiveData;
    //endregion

    private MutableLiveData<Boolean> dataLoadedStateLiveData = new MutableLiveData<>();
    private ArrayList<LetterCellLiveData> arrayListOfLetterCellLiveData;

    private Coordinator coordinator;
    private GameRoom gameRoom;
    private GameProcessData gameProcessData;
    private GameBoard gameBoard;
    private GameVocabulary gameVocabulary;

    private void hostGameSetting() {
        String randomWord = Dictionary.getRandomWordOfACertainLength(gameRoom.getGameGridSize());
        gameVocabulary.writeInitialWord(randomWord)
                .continueWithTask(task -> gameBoard.writeGameBoard(gameRoom.getGameGridSize()))
                .continueWithTask(task -> gameBoard.writeInitialWordInGameBoard(gameVocabulary.getInitialWord()))
                .continueWithTask(task -> {
                    arrayListOfLetterCellLiveData = gameBoard.getFirebaseQueryLetterCellLiveData();
                    return gameProcessData.writeDataState(GameProcessData.DATA_PREPARED_STATE);
                })
                .addOnCompleteListener(task -> {
                    ValueEventListener valueEventListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String data = snapshot.getValue(String.class);
                            if (data != null && data.equals(GameProcessData.GUEST_IS_READY)) {
                                gameProcessData.removeGuestStateValueListener(this);
                                dataLoadedStateLiveData.setValue(true);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    };

                    gameProcessData.addGuestStateValueListener(valueEventListener);

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
                    gameProcessData.removeDataStateValueListener(this);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
        gameProcessData.addDataStateValueListener(valueEventListener);
    }

    public GameViewModel(@NonNull String gameRoomKey) {
        coordinator = new Coordinator();
        GameRoom.fetchGameRoom(gameRoomKey).addOnCompleteListener(task -> {
            gameRoom = task.getResult();
            gameBoard = new GameBoard(gameRoomKey, coordinator);
            gameVocabulary = new GameVocabulary(gameRoomKey, coordinator);
            gameProcessData = new GameProcessData(gameRoomKey, coordinator);

            coordinator.setGameProcessData(gameProcessData);
            coordinator.setGameVocabulary(gameVocabulary);
            coordinator.setGameBoard(gameBoard);

            gameStateFirebaseQueryLiveData = gameProcessData.getGameStateFirebaseQueryLiveData();
            turnTimeLeftInMillisFirebaseQueryLiveData = gameProcessData.getTurnTimeLeftInMillisFirebaseQueryLiveData();
            keyOfPlayerWhoseTurnItFirebaseQueryLiveData = gameProcessData.getKeyOfPlayerWhoseTurnItFirebaseQueryLiveData();

            gameStateLiveData = Transformations.map(gameStateFirebaseQueryLiveData, new StringDeserializer());
            turnTimeLeftInMillisLiveData = Transformations.map(turnTimeLeftInMillisFirebaseQueryLiveData, new LongDeserializer());
            keyOfPlayerWhoseTurnItLiveData = Transformations.map(keyOfPlayerWhoseTurnItFirebaseQueryLiveData, new StringDeserializer());

            if (gameRoom.getHostUID().equals(User.getPlayerKey())) {
                hostGameSetting();
            } else if (gameRoom.getGuestUID().equals(User.getPlayerKey())) {
                guestGameSetting();
            }

        });
    }

    public void confirmCombination() {
        coordinator.confirmCombination();
    }

    public ObservableArrayList<FoundedWord> getOpponentsVocabulary(){
        return gameVocabulary.getOpponentsVocabulary();
    }

    public ObservableArrayList<FoundedWord> getPlayersVocabulary(){
        return gameVocabulary.getPlayersVocabulary();
    }

    public ObservableArrayList<LetterCell> getLettersCombination(){
        return gameBoard.getLettersCombination();
    }

    public void turnOnVocabularyListener(DictionaryAdapter userAdapter, DictionaryAdapter opponentAdapter){
        gameVocabulary.turnOnVocabularyListener(userAdapter, opponentAdapter);
    }

    public void turnOffVocabularyListener(){
        gameVocabulary.turnOffVocabularyListener();
    }

    //region GETTERS_AND_SETTERS
    public GameProcessData getGameProcessData() {
        return gameProcessData;
    }

    public GameRoom getGameRoom() {
        return gameRoom;
    }

    public GameVocabulary getGameVocabulary() {
        return gameVocabulary;
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

    public Coordinator getCoordinator() {
        return coordinator;
    }

    public FirebaseQueryLiveData getGameStateFirebaseQueryLiveData() {
        return gameStateFirebaseQueryLiveData;
    }

    public FirebaseQueryLiveData getKeyOfPlayerWhoseTurnItFirebaseQueryLiveData() {
        return keyOfPlayerWhoseTurnItFirebaseQueryLiveData;
    }

    public FirebaseQueryLiveData getTurnTimeLeftInMillisFirebaseQueryLiveData() {
        return turnTimeLeftInMillisFirebaseQueryLiveData;
    }

    public LiveData<String> getGameStateLiveData() {
        return gameStateLiveData;
    }

    public LiveData<String> getKeyOfPlayerWhoseTurnItLiveData() {
        return keyOfPlayerWhoseTurnItLiveData;
    }

    public LiveData<Long> getTurnTimeLeftInMillisLiveData() {
        return turnTimeLeftInMillisLiveData;
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
    //endregion

}

//        GameRoom.fetchGameRoom(gameRoomKey).continueWith(task ->{
//            gameRoom = task.getResult();
//
//            gameVocabulary = new GameVocabulary(gameRoomKey, coordinator);
//            gameProcessData = new GameProcessData(gameRoomKey, coordinator);
//            gameBoard = new GameBoard(gameRoomKey, coordinator);
//
//            if(gameRoom.getHostUID().equals(User.getPlayerKey())){
//                String randomWord = Dictionary.getRandomWordOfACertainLength(gameRoom.getGameGridSize());
//                gameVocabulary.writeInitialWord(randomWord);
//                return GameVocabularyAccessor.setInitialWord(gameRoomKey, randomWord);
//
//            } else if (gameRoom.getGuestUID().equals(User.getPlayerKey())) {
//
//            }
//
//            return null;
//        });

//        GameRoom.fetchGameRoom(gameRoomKey)
//                .continueWith(task -> {
//                    gameRoom = task.getResult();
//                    coordinator.setGameRoom(gameRoom);
//                    return null;
//                })
//                .continueWithTask(task -> {
//                    gameBoard = new GameBoard(gameRoomKey, coordinator);
//                    if (gameRoom.getHostUID().equals(User.getPlayerKey())) {
//                        return gameBoard.writeGameBoard(gameRoom.getGameGridSize());
//                    } else {
//                        return gameBoard.fetchGameBoard();
//                    }
//                })
//                .continueWith(task -> {
//
//                    gameVocabulary = new GameVocabulary(gameRoomKey, coordinator);
//                    gameProcessData = new GameProcessData(gameRoomKey, coordinator);
//
//                    coordinator.setGameBoard(gameBoard);
//                    coordinator.setGameVocabulary(gameVocabulary);
//                    coordinator.setGameProcessData(gameProcessData);
//
//                    arrayListOfLetterCellLiveData = gameBoard.getFirebaseQueryLetterCellLiveData();
//                    return null;
//                })
//                .continueWithTask(task -> {
//                    if (gameRoom.getHostUID().equals(User.getPlayerKey())) {
//                        String randomWord = Dictionary.getRandomWordOfACertainLength(gameRoom.getGameGridSize());
//                        return gameVocabulary.writeInitialWord(randomWord);
//                    } else {
//                        return null;
//                    }
//                })
//                .continueWithTask(task -> gameBoard.writeInitialWordInGameBoard(gameVocabulary.getInitialWord()))
//                .addOnCompleteListener(tasks -> dataLoadedStateLiveData.setValue(true));