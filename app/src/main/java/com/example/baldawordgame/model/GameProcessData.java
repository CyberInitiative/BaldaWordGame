package com.example.baldawordgame.model;

import androidx.annotation.NonNull;

import com.example.baldawordgame.Coordinator;
import com.google.firebase.database.Exclude;

public class GameProcessData {

    private final static String TAG = "GameProcessData";
    private String gameState;
    private long turnTimeLeftInMillis;
    private String currentHost;
    private String keyOfPlayerWhoseTurnIt;
    private Coordinator coordinator;

    public GameProcessData() {
    }

    public GameProcessData(Coordinator coordinator){
        this.coordinator = coordinator;
    }

    @NonNull
    public String getGameState() {
        return gameState;
    }

    public void setGameState(String gameState) {
        this.gameState = gameState;
    }

    public long getTurnTimeLeftInMillis() {
        return turnTimeLeftInMillis;
    }

    public void setTurnTimeLeftInMillis(long turnTimeLeftInMillis) {
        this.turnTimeLeftInMillis = turnTimeLeftInMillis;
    }

    public String getCurrentHost() {
        return currentHost;
    }

    public void setCurrentHost(String currentHost) {
        this.currentHost = currentHost;
    }

    public String getKeyOfPlayerWhoseTurnIt() {
        return keyOfPlayerWhoseTurnIt;
    }

    public void setKeyOfPlayerWhoseTurnIt(String keyOfPlayerWhoseTurnIt) {
        this.keyOfPlayerWhoseTurnIt = keyOfPlayerWhoseTurnIt;
    }

    @Exclude
    public Coordinator getCoordinator() {
        return coordinator;
    }

    public void setCoordinator(Coordinator coordinator) {
        this.coordinator = coordinator;
    }

    //region CONSTANTS
    @Exclude
    public static final String ROOM_CREATED_STATE = "ROOM_CREATED_STATE";
    @Exclude
    public static final String ROOM_DATA_PREPARING_STATE = "ROOM_DATA_PREPARING_STATE";
    @Exclude
    public static final String ROOM_DATA_PREPARED_STATE = "ROOM_DATA_PREPARED_STATE";

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