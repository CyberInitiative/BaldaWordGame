package com.example.baldawordgame.model;

import androidx.annotation.NonNull;

import com.example.baldawordgame.Coordinator;
import com.example.baldawordgame.FirebaseQueryLiveData;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class GameProcessData {

    private final static String TAG = "GameProcessData";

    //region PATH STRINGS
    public static final String GAME_PROCESS_DATA_PATH = "gameProcessData";
    public static final String GAME_STATE_PATH = "gameState";
    public static final String ACTIVE_PLAYER_KEY_PATH = "activePlayerKey";
    public static final String DATA_STATE_PATH = "dataState";
    public static final String GUEST_STATE_PATH = "guestState";
    //endregion

    private static final DatabaseReference GAME_DATA_REF = FirebaseDatabase.getInstance().getReference().child(GAME_PROCESS_DATA_PATH);
    private final DatabaseReference currentGameProcessDataRef;
    private final DatabaseReference currentDataStateRef;
    private final DatabaseReference currentGameStateRef;
    private final DatabaseReference currentActivePlayerKeyRef;
    private final DatabaseReference currentGuestStateRef;
    private HashMap<DatabaseReference, ValueEventListener> refToValueListener = new HashMap<>();

    public GameProcessData(@NonNull String gameRoomKey) {
        currentGameProcessDataRef = GAME_DATA_REF.child(gameRoomKey);
        currentDataStateRef = currentGameProcessDataRef.child(DATA_STATE_PATH);
        currentGameStateRef = currentGameProcessDataRef.child(GAME_STATE_PATH);
        currentActivePlayerKeyRef = currentGameProcessDataRef.child(ACTIVE_PLAYER_KEY_PATH);
        currentGuestStateRef = currentGameProcessDataRef.child(GUEST_STATE_PATH);
    }

    public FirebaseQueryLiveData getGameStateFirebaseQueryLiveData(){
        return new FirebaseQueryLiveData(currentGameStateRef);
    }

    public FirebaseQueryLiveData getActivePlayerKeyFirebaseQueryLiveData(){
        return new FirebaseQueryLiveData(currentActivePlayerKeyRef);
    }

    public Task<Void> writeGameState(@NonNull String gameState) {
        return currentGameStateRef.setValue(gameState);
    }

    public Task<Void> writeDataState(@NonNull String dataState) {
        return currentDataStateRef.setValue(dataState);
    }

    public Task<Void> writeGuestState(@NonNull String guestState){
        return currentGuestStateRef.setValue(guestState);
    }

    public Task<Void> writeActivePlayerKey(@NonNull String whoseTurnIt) {
        return currentActivePlayerKeyRef.setValue(whoseTurnIt);
    }


    public void addDataStateValueListener(@NonNull ValueEventListener valueEventListener){
        if(!refToValueListener.containsKey(currentDataStateRef)) {
            refToValueListener.put(currentDataStateRef, valueEventListener);
            currentDataStateRef.addValueEventListener(valueEventListener);
        }
    }

    public void removeDataStateValueListener(){
        if(refToValueListener.containsKey(currentDataStateRef)){
            ValueEventListener valueEventListener = refToValueListener.get(currentDataStateRef);
            currentDataStateRef.removeEventListener(valueEventListener);
        }
    }

    public void addGuestStateValueListener(@NonNull ValueEventListener valueEventListener){
        if(!refToValueListener.containsKey(currentGuestStateRef)) {
            refToValueListener.put(currentGuestStateRef, valueEventListener);
            currentGuestStateRef.addValueEventListener(valueEventListener);
        }
    }

    public void removeGuestStateValueListener(){
        if(refToValueListener.containsKey(currentGuestStateRef)){
            ValueEventListener valueEventListener = refToValueListener.get(currentGuestStateRef);
            currentGuestStateRef.removeEventListener(valueEventListener);
        }
    }

    //region CONSTANTS
    @Exclude
    public static final String ROOM_CREATED_STATE = "ROOM_CREATED_STATE";
    @Exclude
    public static final String ROOM_DATA_PREPARING_STATE = "ROOM_DATA_PREPARING_STATE";
    @Exclude
    public static final String GUEST_IS_READY = "GUEST_IS_READY";
    @Exclude
    public static final String DATA_PREPARED_STATE = "DATA_PREPARED_STATE";

    @Exclude
    public static final String GAME_GRID_SIZE_THREE_ON_THREE = "GAME_GRID_SIZE_THREE_ON_THREE";
    @Exclude
    public static final String GAME_GRID_SIZE_FIVE_ON_FIVE = "GAME_GRID_SIZE_FIVE_ON_FIVE";
    @Exclude
    public static final String GAME_GRID_SIZE_SEVEN_ON_SEVEN = "GAME_GRID_SIZE_SEVEN_ON_SEVEN";
    @Exclude
    public static final String GAME_GRID_SIZE_ANY = "GAME_GRID_SIZE_ANY";

    @Exclude
    public static final String TURN_TIME_IS_THIRTY_SECONDS = "TURN_TIME_IS_THIRTY_SECONDS";
    @Exclude
    public static final String TURN_TIME_IS_ONE_MINUTE = "TURN_TIME_IS_ONE_MINUTE";
    @Exclude
    public static final String TURN_TIME_IS_TWO_MINUTES = "TURN_TIME_IS_TWO_MINUTES";
    @Exclude
    public static final String TURN_TIME_IS_ANY = "TURN_TIME_IS_ANY";
    //endregion

}