package com.example.baldawordgame;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.baldawordgame.model.GameProcessData;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class GameProcessDataAccessor {

    private static final String TAG = "GameRoomAccessor";
    private static final DatabaseReference GAME_ROOMS_REF = FirebaseDatabase.getInstance().getReference().child("openRooms");

    public static Task<GameProcessData> fetchGameRoom(@NonNull String gameRoomKey) {
        return GAME_ROOMS_REF.child(gameRoomKey).get()
                .continueWith(new Continuation<DataSnapshot, GameProcessData>() {
            @Override
            public GameProcessData then(@NonNull Task<DataSnapshot> task) throws Exception {
                GameProcessData gameProcessData = task.getResult().getValue(GameProcessData.class);
                Log.d(TAG, "fetchGameRoom(); GameRoom result = " + gameProcessData);
                return gameProcessData;
            }
        });
    }

    @NonNull
    public static FirebaseQueryLiveData fetchCurrentHostFirebaseQueryLiveData(@NonNull String gameRoomKey){
        return new FirebaseQueryLiveData(GAME_ROOMS_REF.child(gameRoomKey).child("currentHost").getRef());
    }

    @NonNull
    public static FirebaseQueryLiveData fetchKeyOfPlayerFirebaseQueryLiveData(@NonNull String gameRoomKey){
        return new FirebaseQueryLiveData(GAME_ROOMS_REF.child(gameRoomKey).child("keyOfPlayerWhoseTurnIt").getRef());
    }

    @NonNull
    public static FirebaseQueryLiveData fetchTurnTimeLeftInMillisFirebaseQueryLiveData(@NonNull String gameRoomKey) {
        return new FirebaseQueryLiveData(GAME_ROOMS_REF.child(gameRoomKey).child("turnTimeLeftInMillis").getRef());
    }

}