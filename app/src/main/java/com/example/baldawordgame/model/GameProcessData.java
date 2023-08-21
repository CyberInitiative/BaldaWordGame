package com.example.baldawordgame.model;

import androidx.annotation.NonNull;

import com.example.baldawordgame.Coordinator;
import com.example.baldawordgame.FirebaseQueryLiveData;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class GameProcessData {

    private final static String TAG = "GameProcessData";
    private static final DatabaseReference GAME_DATA_REF = FirebaseDatabase.getInstance().getReference().child("gameProcessData");

    private String gameRoomKey;

    private String dataState;
    private String gameState;
    private String keyOfPlayerWhoseTurnIt;
    private String guestState;
    private long turnTimeLeftInMillis;
    private Coordinator coordinator;

    public GameProcessData() {
    }

    public GameProcessData(@NonNull String gameRoomKey, @NonNull Coordinator coordinator) {
        this.coordinator = coordinator;
        this.gameRoomKey = gameRoomKey;
    }

    public FirebaseQueryLiveData getGameStateFirebaseQueryLiveData(){
        return new FirebaseQueryLiveData(GAME_DATA_REF.child(gameRoomKey).child("gameState"));
    }

    public FirebaseQueryLiveData getTurnTimeLeftInMillisFirebaseQueryLiveData(){
        return new FirebaseQueryLiveData(GAME_DATA_REF.child(gameRoomKey).child("turnTimeLeftInMillis"));
    }

    public FirebaseQueryLiveData getKeyOfPlayerWhoseTurnItFirebaseQueryLiveData(){
        return new FirebaseQueryLiveData(GAME_DATA_REF.child(gameRoomKey).child("keyOfPlayerWhoseTurnIt"));
    }

//    public FirebaseQueryLiveData getGuestStateFirebaseQueryLiveData(){
//        return new FirebaseQueryLiveData(GAME_DATA_REF.child(gameRoomKey).child("guestState"));
//    }

    public Task<Void> writeGameState(@NonNull String gameState) {
        return GAME_DATA_REF.child(gameRoomKey).child("gameState").setValue(gameState);
    }

    public Task<Void> writeKeyOfPlayerWhoseTurnIt(@NonNull String hostPlayerKey) {
        return GAME_DATA_REF.child(gameRoomKey).child("hostPlayerKey").setValue(hostPlayerKey);
    }

    public Task<Void> writeTurnTimeLeftInMillis(long turnTimeLeftInMillis) {
        return GAME_DATA_REF.child(gameRoomKey).child("turnTimeLeftInMillis").setValue(turnTimeLeftInMillis);
    }

    public Task<Void> writeDataState(@NonNull String dataState) {
        return GAME_DATA_REF.child(gameRoomKey).child("dataState").setValue(dataState);
    }

    public Task<Void> writeGuestState(@NonNull String guestState){
        return GAME_DATA_REF.child(gameRoomKey).child("guestState").setValue(guestState);
    }

    public void addDataStateValueListener(@NonNull ValueEventListener valueEventListener){
        GAME_DATA_REF.child(gameRoomKey).child("dataState").addValueEventListener(valueEventListener);
    }

    public void removeDataStateValueListener(@NonNull ValueEventListener valueEventListener){
        GAME_DATA_REF.child(gameRoomKey).child("gameState").removeEventListener(valueEventListener);
    }

    public void addGuestStateValueListener(@NonNull ValueEventListener valueEventListener){
        GAME_DATA_REF.child(gameRoomKey).child("guestState").addValueEventListener(valueEventListener);
    }

    public void removeGuestStateValueListener(@NonNull ValueEventListener valueEventListener){
        GAME_DATA_REF.child(gameRoomKey).child("gameState").removeEventListener(valueEventListener);
    }

    @Exclude
    public Coordinator getCoordinator() {
        return coordinator;
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