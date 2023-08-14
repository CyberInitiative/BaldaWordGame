package com.example.baldawordgame;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.baldawordgame.model.GameProcessData;
import com.example.baldawordgame.model.GameRoom;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameViewModel extends ViewModel {

    private static final String TAG = "GameViewModel";

    //region LiveData variables
    private MutableLiveData<Boolean> dataLoadedStateLiveData = new MutableLiveData<>();
    private LiveData<String> initialWordLiveData;
    private LiveData<String> currentHostLiveData;
    private LiveData<String> keyOfPlayerWhoseTurnLiveData;
    private LiveData<Long> turnTimeLeftInMillisLiveData;
    //endregion

    //region FirebaseQueryLiveData VARIABLES
    private FirebaseQueryLiveData initialWordFirebaseQueryLiveData;
    private FirebaseQueryLiveData currentHostFirebaseQueryLiveData;
    private FirebaseQueryLiveData keyOfPlayerWhoseTurnFirebaseQueryLiveData;
    private FirebaseQueryLiveData turnTimeLeftInMillisFirebaseQueryLiveData;
    private ArrayList<LetterCellLiveData> arrayListOfLetterCellLiveData;
    //endregion

    private Coordinator coordinator;

    public GameViewModel(@NonNull String gameRoomKey) {
        coordinator = new Coordinator();
        Task<GameRoom> fetchGameRoomTask = GameRoom.fetchGameRoom(gameRoomKey);
        fetchGameRoomTask.continueWithTask(task -> {
                    GameRoom gameRoom = task.getResult();
                    coordinator.setGameRoom(gameRoom);
                    return GameBoardAccessor.writeGameBoard(gameRoomKey, gameRoom.getGameGridSize());
                }).continueWith(task -> {
                    HashMap<DatabaseReference, LetterCell> refToLetterCell = task.getResult();

                    GameProcessData gameProcessData = new GameProcessData(coordinator);
                    GameBoard gameBoard = new GameBoard(refToLetterCell, coordinator);
                    GameVocabulary gameVocabulary = new GameVocabulary(coordinator);

                    coordinator.setGameProcessData(gameProcessData);
                    coordinator.setGameBoard(gameBoard);
                    coordinator.setGameVocabulary(gameVocabulary);

                    arrayListOfLetterCellLiveData = createArrayListOfFirebaseQueryLetterCellLiveData(refToLetterCell);
                    return null;
                }).continueWithTask(task -> {
                    String randomWord = Dictionary.getRandomWordOfACertainLength(getGameRoom().getGameGridSize());
                    coordinator.getGameVocabulary().setInitialWord(randomWord);
                    return GameVocabularyAccessor.setInitialWord(gameRoomKey, randomWord);
                })
                .continueWith(task -> {
                    List<Task<Void>> writingLettersTask = writeInitialWordInGameBoard(coordinator.getGameVocabulary().getInitialWord());
                    return Tasks.whenAll(writingLettersTask);
                })
                .addOnCompleteListener(tasks -> dataLoadedStateLiveData.setValue(true));
    }

    private ArrayList<LetterCellLiveData> createArrayListOfFirebaseQueryLetterCellLiveData
            (@NonNull HashMap<DatabaseReference, LetterCell> letterCellToRef) {
        ArrayList<LetterCellLiveData> arrayListOfaLetterCellLiveData = new ArrayList<>();
        for (Map.Entry<DatabaseReference, LetterCell> entry : letterCellToRef.entrySet()) {
            LetterCellLiveData letterCellLiveData = new LetterCellLiveData(entry.getKey());
            arrayListOfaLetterCellLiveData.add(letterCellLiveData);
        }
        return arrayListOfaLetterCellLiveData;
    }

    private List<Task<Void>> writeInitialWordInGameBoard(@NonNull String word) {
        Log.d(TAG, "writeInitialWordInGameBoard(); word is: " + word);
        Log.d(TAG, "writeInitialWordInGameBoard(); gameRoom.getGameGridSize(): " + getGameRoom().getGameGridSize());
        if (word.length() == getGameRoom().getGameGridSize()) {
            List<Task<Void>> tasks = new ArrayList<>();
            int rowPosition = getGameRoom().getGameGridSize() / 2;
            for (int i = 0; i < getGameRoom().getGameGridSize(); i++) {
                Log.d(TAG, "LOOP: " + i + " size: " + getGameRoom().getGameGridSize());
                LetterCell letterCell = getGameBoard().getLetterCellByRowAndColumn(rowPosition, i);
                DatabaseReference letterCellRef = getGameBoard().getLetterCellRef(letterCell);

                Log.d(TAG, "REF: " + letterCellRef);
                letterCell.setLetter(String.valueOf(word.charAt(i)));
                letterCell.setState(LetterCell.LETTER_CELL_WITH_LETTER_STATE);

                Task<Void> task = GameBoardAccessor.updateLetterCell(letterCell, getGameRoom().getGameRoomKey(), letterCellRef);

                tasks.add(task);
            }
            Log.d(TAG, "writeInitialWordInGameBoard(); returning tasks; size: " + tasks.size());
            return tasks;
        }
        return null;
    }

    public void prepareDataOnCreationStage() {
//        GameRoomAccessor.fetchGameRoom(gameRoomKey)
//                .continueWithTask(task -> {
//                    gameRoom = task.getResult();
//                    gameVocabulary = new GameVocabulary();
//                    createGameBoard();
//                    return GameBoardAccessor.writeGameBoard(gameRoomKey, gameRoom.getGameGridSize());
//                })
//                .continueWith(task -> {
//                    gameBoardChangeListener = new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//                            ArrayList<LetterCell> letterCellsSnapshot = new ArrayList<>();
//
//                            for (DataSnapshot snapshotChild : snapshot.getChildren()) {
//                                LetterCell letterCell = snapshotChild.getValue(LetterCell.class);
//                                letterCellsSnapshot.add(letterCell);
//                            }
//
//                            gameBoard.compareSnapshots(letterCellsSnapshot);
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError error) {
//
//                        }
//                    };
//
//                    FirebaseDatabase.getInstance().getReference().child("gameBoards").child(gameRoomKey).addValueEventListener(gameBoardChangeListener);
//                    return null;
//                }).continueWithTask(task -> {
//                    String pickedWord = Dictionary.getRandomWordOfACertainLength(gameRoom.getGameGridSize());
//                    gameVocabulary.setInitialWord(pickedWord);
//                    return GameVocabularyAccessor.setInitialWord(gameRoomKey, pickedWord);
//                }).addOnCompleteListener(task -> {
//                    Tasks.whenAll(writeInitialWordInGameBoard(gameVocabulary.getInitialWord())).addOnCompleteListener(task1 -> {
//                        dataLoadedStateLiveData.setValue(true);
//                    });
//                });
    }

    private void fetchFirebaseQueryLiveData() {
        initialWordFirebaseQueryLiveData = GameVocabularyAccessor.fetchInitialWordFirebaseLiveData(getGameRoom().getGameRoomKey());
        currentHostFirebaseQueryLiveData = GameProcessDataAccessor.fetchCurrentHostFirebaseQueryLiveData(getGameRoom().getGameRoomKey());
        keyOfPlayerWhoseTurnFirebaseQueryLiveData = GameProcessDataAccessor.fetchKeyOfPlayerFirebaseQueryLiveData(getGameRoom().getGameRoomKey());
        turnTimeLeftInMillisFirebaseQueryLiveData = GameProcessDataAccessor.fetchTurnTimeLeftInMillisFirebaseQueryLiveData(getGameRoom().getGameRoomKey());
    }

    private void transformFirebaseQueryLiveDataInLiveData() {
        initialWordLiveData = Transformations.map(initialWordFirebaseQueryLiveData, new StringDeserializer());
        currentHostLiveData = Transformations.map(currentHostFirebaseQueryLiveData, new StringDeserializer());
        keyOfPlayerWhoseTurnLiveData = Transformations.map(keyOfPlayerWhoseTurnFirebaseQueryLiveData, new StringDeserializer());
        turnTimeLeftInMillisLiveData = Transformations.map(turnTimeLeftInMillisFirebaseQueryLiveData, new LongDeserializer());
    }

    //region GETTERS_AND_SETTERS
    public GameProcessData getGameProcessData() {
        return coordinator.getGameProcessData();
    }

    public GameRoom getGameRoom() {
        return coordinator.getGameRoom();
    }

    public GameVocabulary getGameVocabulary() {
        return coordinator.getGameVocabulary();
    }

    public GameBoard getGameBoard() {
        return coordinator.getGameBoard();
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

    public LiveData<String> getInitialWordLiveData() {
        return initialWordLiveData;
    }

    public LiveData<String> getCurrentHostLiveData() {
        return currentHostLiveData;
    }

    public LiveData<String> getKeyOfPlayerWhoseTurnLiveData() {
        return keyOfPlayerWhoseTurnLiveData;
    }

    public LiveData<Long> getTurnTimeLeftInMillisLiveData() {
        return turnTimeLeftInMillisLiveData;
    }

    public FirebaseQueryLiveData getInitialWordFirebaseQueryLiveData() {
        return initialWordFirebaseQueryLiveData;
    }

    public FirebaseQueryLiveData getCurrentHostFirebaseQueryLiveData() {
        return currentHostFirebaseQueryLiveData;
    }

    public FirebaseQueryLiveData getKeyOfPlayerWhoseTurnFirebaseQueryLiveData() {
        return keyOfPlayerWhoseTurnFirebaseQueryLiveData;
    }

    public FirebaseQueryLiveData getTurnTimeLeftInMillisFirebaseQueryLiveData() {
        return turnTimeLeftInMillisFirebaseQueryLiveData;
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