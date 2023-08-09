package com.example.baldawordgame;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.baldawordgame.model.GameRoom;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

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

    private String gameRoomKey;
    private GameRoom gameRoom;
    private GameVocabulary gameVocabulary;
    private GameBoard gameBoard;

    public GameViewModel(@NonNull String gameRoomKey) {
        this.gameRoomKey = gameRoomKey;
    }

    public void test() {
        Task<GameRoom> fetchGameRoomTask = GameRoomAccessor.fetchGameRoom(gameRoomKey);

        fetchGameRoomTask.continueWithTask(task -> {
                    gameRoom = task.getResult();
                    gameVocabulary = new GameVocabulary();
                    return GameBoardAccessor.writeGameBoard(gameRoomKey, gameRoom.getGameGridSize());
                })
                .continueWith(task -> {
                    HashMap<DatabaseReference, LetterCell> refToLetterCell = task.getResult();
                    Log.d(TAG, "refToLetterCell: " + refToLetterCell);
                    arrayListOfLetterCellLiveData = createArrayListOfFirebaseQueryLetterCellLiveData(refToLetterCell);
                    gameBoard = new GameBoard(refToLetterCell);
                    return null;
                })
                .continueWithTask(task -> {
                    String randomWord = Dictionary.getRandomWordOfACertainLength(gameRoom.getGameGridSize());
                    gameVocabulary.setInitialWord(randomWord);
                    return GameVocabularyAccessor.setInitialWord(gameRoomKey, randomWord);
                })
                .continueWith(task -> {
                    List<Task<Void>> writingLettersTask = writeInitialWordInGameBoard(gameVocabulary.getInitialWord());
                    return Tasks.whenAll(writingLettersTask);
                })
                .addOnCompleteListener(tasks -> dataLoadedStateLiveData.setValue(true));

    }

    private ArrayList<LetterCellLiveData> createArrayListOfFirebaseQueryLetterCellLiveData(@NonNull HashMap<DatabaseReference, LetterCell> letterCellToRef) {
        ArrayList<LetterCellLiveData> arrayListOfaLetterCellLiveData = new ArrayList<>();
        for (Map.Entry<DatabaseReference, LetterCell> entry : letterCellToRef.entrySet()) {
            LetterCellLiveData letterCellLiveData = new LetterCellLiveData(entry.getKey());
            arrayListOfaLetterCellLiveData.add(letterCellLiveData);
        }
        return arrayListOfaLetterCellLiveData;
    }

    private List<Task<Void>> writeInitialWordInGameBoard(@NonNull String word) {
        Log.d(TAG, "writeInitialWordInGameBoard(); word is: " + word);
        Log.d(TAG, "writeInitialWordInGameBoard(); gameRoom.getGameGridSize(): " + gameRoom.getGameGridSize());
        if (word.length() == gameRoom.getGameGridSize()) {
            List<Task<Void>> tasks = new ArrayList<>();
            int rowPosition = gameRoom.getGameGridSize() / 2;
            for (int i = 0; i < gameRoom.getGameGridSize(); i++) {
                Log.d(TAG, "LOOP: " + i + " size: " + gameRoom.getGameGridSize());
                LetterCell letterCell = gameBoard.getLetterCellByRowAndColumn(rowPosition, i);
                DatabaseReference letterCellRef = gameBoard.getLetterCellRef(letterCell);

                Log.d(TAG, "REF: " + letterCellRef);
                letterCell.setLetter(String.valueOf(word.charAt(i)));
                letterCell.setState(LetterCell.LETTER_CELL_WITH_LETTER_STATE);

                Task<Void> task = GameBoardAccessor.updateLetterCell(letterCell, gameRoomKey, letterCellRef);

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
        initialWordFirebaseQueryLiveData = GameVocabularyAccessor.fetchInitialWordFirebaseLiveData(gameRoomKey);
        currentHostFirebaseQueryLiveData = GameRoomAccessor.fetchCurrentHostFirebaseQueryLiveData(gameRoomKey);
        keyOfPlayerWhoseTurnFirebaseQueryLiveData = GameRoomAccessor.fetchKeyOfPlayerFirebaseQueryLiveData(gameRoomKey);
        turnTimeLeftInMillisFirebaseQueryLiveData = GameRoomAccessor.fetchTurnTimeLeftInMillisFirebaseQueryLiveData(gameRoomKey);
    }

    private void transformFirebaseQueryLiveDataInLiveData() {
        initialWordLiveData = Transformations.map(initialWordFirebaseQueryLiveData, new StringDeserializer());
        currentHostLiveData = Transformations.map(currentHostFirebaseQueryLiveData, new StringDeserializer());
        keyOfPlayerWhoseTurnLiveData = Transformations.map(keyOfPlayerWhoseTurnFirebaseQueryLiveData, new StringDeserializer());
        turnTimeLeftInMillisLiveData = Transformations.map(turnTimeLeftInMillisFirebaseQueryLiveData, new LongDeserializer());
    }

    //region GETTERS_AND_SETTERS
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

    //    public LiveData<String> getInitialWordLiveData() {
//        return initialWordLiveData;
//    }
//
//    public LiveData<String> getCurrentHostLiveData() {
//        return currentHostLiveData;
//    }
//
//    public LiveData<String> getKeyOfPlayerWhoseTurnLiveData() {
//        return keyOfPlayerWhoseTurnLiveData;
//    }
//
//    public LiveData<Long> getTurnTimeLeftInMillisLiveData() {
//        return turnTimeLeftInMillisLiveData;
//    }
//
//    public FirebaseQueryLiveData getInitialWordFirebaseQueryLiveData() {
//        return initialWordFirebaseQueryLiveData;
//    }
//
//    public FirebaseQueryLiveData getCurrentHostFirebaseQueryLiveData() {
//        return currentHostFirebaseQueryLiveData;
//    }
//
//    public FirebaseQueryLiveData getKeyOfPlayerWhoseTurnFirebaseQueryLiveData() {
//        return keyOfPlayerWhoseTurnFirebaseQueryLiveData;
//    }
//
//    public FirebaseQueryLiveData getTurnTimeLeftInMillisFirebaseQueryLiveData() {
//        return turnTimeLeftInMillisFirebaseQueryLiveData;
//    }
//
//    public ValueEventListener getGameBoardChangeListener() {
//        return gameBoardChangeListener;
//    }

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

/*
  public void prepareDataOnCreationStage() {
        GameRoomAccessor.fetchGameRoom(gameRoomKey).addOnCompleteListener(gameRoomFetchingTask -> {
            gameRoom = gameRoomFetchingTask.getResult();
            createGameBoard();
            gameVocabulary = new GameVocabulary();

            if (gameRoom.getGameRoomState().equals(GameRoom.ROOM_CREATED_STATE)) {

                GameBoardAccessor.writeGameBoard(gameRoomKey, gameRoom.getGameGridSize()).addOnCompleteListener(gameBoardWritingTask -> {
                    String pickedWord = Dictionary.getRandomWordOfACertainLength(gameRoom.getGameGridSize());
                    if (pickedWord != null) {
                        Task<Void> initialWordWritingTask = GameVocabularyAccessor.setInitialWord(gameRoomKey, pickedWord);
                        List<Task<Void>> tasks = writeInitialWordInGameBoard(pickedWord);
                        tasks.add(initialWordWritingTask);
                        Tasks.whenAll(tasks)
                                .continueWithTask(task -> GameBoardAccessor.fetchArrayListOfFirebaseQueryLiveDataLetterCell(gameRoomKey))
                                .addOnCompleteListener(task -> {
                                    arrayListOfFirebaseQueryLiveDataLetterCell = task.getResult();
                                    Log.d(TAG, ".addOnCompleteListener(); " + arrayListOfFirebaseQueryLiveDataLetterCell);
                                    fetchFirebaseQueryLiveData();
                                    transformFirebaseQueryLiveDataInLiveData();
                                    dataLoadedStateLiveData.setValue(true);
                                });
                    }
                });

            } else {
                GameBoardAccessor.fetchArrayListOfFirebaseQueryLiveDataLetterCell(gameRoomKey).addOnCompleteListener(task -> {
                    arrayListOfFirebaseQueryLiveDataLetterCell = task.getResult();
                    fetchFirebaseQueryLiveData();
                    transformFirebaseQueryLiveDataInLiveData();
                    dataLoadedStateLiveData.setValue(true);
                });
            }

        });
    }
 */

// .continueWithTask(task -> {
//                    gameRoom = task.getResult();
//                    gameVocabulary = new GameVocabulary();
//                    return GameBoardAccessor.writeGameBoard(gameRoomKey, gameRoom.getGameGridSize());
//                }).continueWith(task -> {
//                    HashMap<DatabaseReference, LetterCell> refToLetterCell = task.getResult();
//                    Log.d(TAG, "refToLetterCell: " + refToLetterCell);
//                    arrayListOfLetterCellLiveData = createArrayListOfFirebaseQueryLetterCellLiveData(refToLetterCell);
//                    gameBoard = new GameBoard(refToLetterCell);
//                    return null;
//                }).continueWithTask(task -> {
//                    String randomWord = Dictionary.getRandomWordOfACertainLength(gameRoom.getGameGridSize());
//                    gameVocabulary.setInitialWord(randomWord);
//                    return GameVocabularyAccessor.setInitialWord(gameRoomKey, randomWord);
//                }).addOnCompleteListener(task -> {
//                    List<Task<Void>> writingLettersTask = writeInitialWordInGameBoard(gameVocabulary.getInitialWord());
//                    Tasks.whenAll(writingLettersTask).addOnCompleteListener(tasks -> dataLoadedStateLiveData.setValue(true));
//                });