package com.example.baldawordgame.model;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ObservableArrayList;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class GameVocabulary {

    private static final String TAG = "GAME_VOCABULARY";

    //region PATH STRINGS
    public static final String GAME_VOCABULARIES_PATH = "gameVocabularies";
    public static final String INITIAL_WORD_PATH = "initialWord";
    public static final String VOCABULARY_PATH = "vocabulary";
    //endregion

    public static final DatabaseReference GAME_VOCABULARIES = FirebaseDatabase.getInstance().getReference().child(GAME_VOCABULARIES_PATH);

    private final String gameRoomKey;
    private final ObservableArrayList<FoundWord> foundWords = new ObservableArrayList<>();
    private ChildEventListener gameVocabularyListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            FoundWord foundWord = snapshot.getValue(FoundWord.class);
            if (foundWord != null) {
                if(!foundWords.contains(foundWord)) {
                    foundWords.add(foundWord);
                    Log.d(TAG, "foundWords.add(foundWord); word is: " + foundWord);
                }
            }
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot snapshot) {

        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };
    private boolean isGameVocabularyListenerSet;

    public GameVocabulary(@NonNull String gameRoomKey) {
        this.gameRoomKey = gameRoomKey;
    }

    public void setGameVocabularyListener() {
        if (!isGameVocabularyListenerSet) {
            GAME_VOCABULARIES.child(gameRoomKey).child("vocabulary").addChildEventListener(gameVocabularyListener);
            isGameVocabularyListenerSet = true;
        }
    }

    public void removeGameVocabularyListener() {
        if (isGameVocabularyListenerSet) {
            GAME_VOCABULARIES.child(gameRoomKey).child("vocabulary").removeEventListener(gameVocabularyListener);
            isGameVocabularyListenerSet = false;
        }
    }

    public ObservableArrayList<FoundWord> getFoundWords() {
        return foundWords;
    }

    @NonNull
    public Task<Void> addWord(@NonNull String word, @NonNull String playerKey) {
        FoundWord foundWord = new FoundWord(word, playerKey);
        return GAME_VOCABULARIES.child(gameRoomKey).child(VOCABULARY_PATH).push().setValue(foundWord);
    }

    @NonNull
    public WordCheckResult checkWord(@NonNull String word) {
        if (!Dictionary.checkIfWordIsInDictionary(word)) {
            return WordCheckResult.NOT_IN_THE_DICTIONARY;
        }
        for (FoundWord foundWord : foundWords) {
            if (foundWord.getWord().equals(word)) {
                return WordCheckResult.ALREADY_FOUNDED;
            }
        }
        return WordCheckResult.NEW_WORD_FOUNDED;
    }

    public static boolean checkIfVocabularyContainsWord(String word, ObservableArrayList<FoundWord> vocabulary) {
        for (FoundWord foundWord : vocabulary) {
            if (foundWord.getWord().equals(word)) {
                return true;
            }
        }
        return false;
    }

    //region GETTERS AND SETTERS
    public ChildEventListener getGameVocabularyListener() {
        return gameVocabularyListener;
    }

    public boolean isGameVocabularyListenerSet() {
        return isGameVocabularyListenerSet;
    }
    //endregion

    public enum WordCheckResult {
        NOT_IN_THE_DICTIONARY,
        ALREADY_FOUNDED,
        NEW_WORD_FOUNDED
    }

}