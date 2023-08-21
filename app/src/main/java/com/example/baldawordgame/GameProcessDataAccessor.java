package com.example.baldawordgame;

import androidx.annotation.NonNull;

import com.example.baldawordgame.model.GameRoom;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class GameProcessDataAccessor {

    private static final String TAG = "GameRoomAccessor";
    public static final DatabaseReference GAME_DATA_REF = FirebaseDatabase.getInstance().getReference().child("gameProcessData");

    public static Task<Void> writeHostPlayerKey(@NonNull String gameRoomKey, @NonNull String hostPlayerKey) {
        return GAME_DATA_REF.child(gameRoomKey).child("hostPlayerKey").setValue(hostPlayerKey);
    }

    @NonNull
    public static FirebaseQueryLiveData fetchCurrentHostFirebaseQueryLiveData(@NonNull String gameRoomKey) {
        return new FirebaseQueryLiveData(GameRoom.GAME_ROOMS_REF.child(gameRoomKey).child("currentHost").getRef());
    }

    @NonNull
    public static FirebaseQueryLiveData fetchKeyOfPlayerFirebaseQueryLiveData(@NonNull String gameRoomKey) {
        return new FirebaseQueryLiveData(GameRoom.GAME_ROOMS_REF.child(gameRoomKey).child("keyOfPlayerWhoseTurnIt").getRef());
    }

    @NonNull
    public static FirebaseQueryLiveData fetchTurnTimeLeftInMillisFirebaseQueryLiveData(@NonNull String gameRoomKey) {
        return new FirebaseQueryLiveData(GameRoom.GAME_ROOMS_REF.child(gameRoomKey).child("turnTimeLeftInMillis").getRef());
    }
}