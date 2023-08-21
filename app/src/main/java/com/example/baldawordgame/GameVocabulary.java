package com.example.baldawordgame;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ObservableArrayList;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class GameVocabulary {

    private static final String TAG = "GAME_VOCABULARY";
    public static final DatabaseReference GAME_VOCABULARIES
            = FirebaseDatabase.getInstance().getReference().child("gameVocabularies");

    private final String gameRoomKey;
    private String initialWord;
    private final ObservableArrayList<FoundedWord> playersVocabulary = new  ObservableArrayList<>();
    private final ObservableArrayList<FoundedWord> opponentsVocabulary = new  ObservableArrayList<>();
    private Coordinator coordinator;

    private ChildEventListener vocabularyListener;
    public enum WordCheckResult {
        NOT_IN_THE_DICTIONARY,
        ALREADY_FOUNDED,
        NEW_FOUND_WORD
    }

    public GameVocabulary(@NonNull String gameRoomKey, Coordinator coordinator) {
        this.gameRoomKey = gameRoomKey;
        this.coordinator = coordinator;
    }

    public void turnOnVocabularyListener(DictionaryAdapter userAdapter, DictionaryAdapter opponentAdapter){
        vocabularyListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                FoundedWord foundedWord = snapshot.getValue(FoundedWord.class);
                if(foundedWord != null && foundedWord.getWord() != null && !foundedWord.getWord().equals(initialWord)){
                    if(foundedWord.getPlayerKey().equals(User.getPlayerKey())){
                        playersVocabulary.add(foundedWord);
//                        userAdapter.notifyItemInserted(playersVocabulary.indexOf(foundedWord));
                    }else {
                        opponentsVocabulary.add(foundedWord);
//                        opponentAdapter.notifyItemInserted(opponentsVocabulary.indexOf(foundedWord));
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

        GAME_VOCABULARIES.child(gameRoomKey).child("vocabulary").addChildEventListener(vocabularyListener);
    }

    public void turnOffVocabularyListener(){
        GAME_VOCABULARIES.removeEventListener(vocabularyListener);
    }

    @NonNull
    public Task<Void> writeInitialWord(@NonNull String initialWord) {
        this.initialWord = initialWord;
        return GAME_VOCABULARIES.child(gameRoomKey).child("initialWord").setValue(initialWord);
    }

    @NonNull
    public Task<Void> fetchInitialWord() {
        return GAME_VOCABULARIES.child(gameRoomKey).child("initialWord").get()
                .continueWith(task -> {
                    String initialWord = task.getResult().getValue(String.class);
                    if(initialWord != null){
                        this.initialWord = initialWord;
                    }
                    return null;
                });
    }

    @NonNull
    public Task<Void> addWord(@NonNull String word) {
        FoundedWord foundedWord = new FoundedWord(word, User.getPlayerKey());
        return GAME_VOCABULARIES.child(gameRoomKey).child("vocabulary").push().setValue(foundedWord);
    }

    @NonNull
    public WordCheckResult checkWord(String word) {
        if (!Dictionary.checkIfWordIsInDictionary(word)) {
            return WordCheckResult.NOT_IN_THE_DICTIONARY;
        }
        for (FoundedWord foundedWord : opponentsVocabulary) {
            if (foundedWord.getWord().equals(word)) {
                return WordCheckResult.ALREADY_FOUNDED;
            }
        }
        for (FoundedWord foundedWord : playersVocabulary) {
            if (foundedWord.getWord().equals(word)) {
                return WordCheckResult.ALREADY_FOUNDED;
            }
        }
        return WordCheckResult.NEW_FOUND_WORD;
    }

    //region GETTERS_AND_SETTERS
    public String getInitialWord() {
        return initialWord;
    }

    public ObservableArrayList<FoundedWord> getPlayersVocabulary() {
        return playersVocabulary;
    }

    public ObservableArrayList<FoundedWord> getOpponentsVocabulary() {
        return opponentsVocabulary;
    }

    public Coordinator getCoordinator() {
        return coordinator;
    }

    public void setCoordinator(Coordinator coordinator) {
        this.coordinator = coordinator;
    }
    //endregion
}