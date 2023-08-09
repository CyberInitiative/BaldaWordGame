package com.example.baldawordgame;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class GameVocabularyAccessor {

    private static final DatabaseReference GAME_VOCABULARIES
            = FirebaseDatabase.getInstance().getReference().child("gameVocabularies");

    private GameVocabularyAccessor() {
        throw new AssertionError();
    }

    public static FirebaseQueryLiveData fetchInitialWordFirebaseLiveData(@NonNull String gameRoomKey){
        DatabaseReference ref = GAME_VOCABULARIES.child(gameRoomKey).child("initialWord");
        return new FirebaseQueryLiveData(ref);
    }

    @NonNull
    public static Task<Void> setInitialWord(@NonNull String gameRoomKey, @NonNull String initialWord){
        return GAME_VOCABULARIES.child(gameRoomKey).child("initialWord").setValue(initialWord);
    }
    @NonNull
    public static Task<String> fetchInitialWord(@NonNull String gameRoomKey){
        return GAME_VOCABULARIES.child(gameRoomKey).child("initialWord").get().continueWith(task -> {
            return task.getResult().getValue(String.class);
        });
    }

    @NonNull
    public static Task<Void> addWord(@NonNull String gameRoomKey, @NonNull String word){
        FoundedWord foundedWord = new FoundedWord(word, User.getPlayerKey());
        return GAME_VOCABULARIES.child(gameRoomKey).child("vocabulary").setValue(foundedWord);
    }
}
