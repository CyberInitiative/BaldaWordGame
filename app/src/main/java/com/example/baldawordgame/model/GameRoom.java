package com.example.baldawordgame.model;

import androidx.annotation.NonNull;

import com.example.baldawordgame.Coordinator;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class GameRoom {
    private static final DatabaseReference GAME_ROOMS_REF = FirebaseDatabase.getInstance().getReference().child("gameRooms");
    private final static String TAG = "GAME_ROOM";
    private String gameRoomKey;
    private String gameRoomState;
    private String playerOneUID; //Игрок который создал комнату;
    private String playerTwoUID;  //Игрок который подключился;
    private int gameGridSize;
    private long turnTimeInMillis; //Время на ход;
    private long turnTimeLeftInMillis;
    private String currentHost;
    private String keyOfPlayerWhoseTurnIt;

    public GameRoom() {
    }

    public GameRoom(@NonNull String gameRoomKey, @NonNull String roomCreatedUserUID, int gameGridSize, long turnTime) {
        this.gameRoomKey = gameRoomKey;
        this.playerOneUID = roomCreatedUserUID;
        this.gameGridSize = gameGridSize;
        this.turnTimeInMillis = turnTime;
        currentHost = roomCreatedUserUID;
        gameRoomState = ROOM_CREATED_STATE;
    }

    @NonNull
    public String getGameRoomKey() {
        return gameRoomKey;
    }

    public void setGameRoomKey(String gameRoomKey) {
        this.gameRoomKey = gameRoomKey;
    }

    @NonNull
    public String getGameRoomState() {
        return gameRoomState;
    }

    public void setGameRoomState(String gameRoomState) {
        this.gameRoomState = gameRoomState;
    }

    public String getPlayerOneUID() {
        return playerOneUID;
    }

    public void setPlayerOneUID(String playerOneUID) {
        this.playerOneUID = playerOneUID;
    }

    public String getPlayerTwoUID() {
        return playerTwoUID;
    }

    public void setPlayerTwoUID(String playerTwoUID) {
        this.playerTwoUID = playerTwoUID;
    }

    public long getTurnTimeInMillis() {
        return turnTimeInMillis;
    }

    public void setTurnTimeInMillis(long turnTimeInMillis) {
        this.turnTimeInMillis = turnTimeInMillis;
    }

    public long getTurnTimeLeftInMillis() {
        return turnTimeLeftInMillis;
    }

    public void setTurnTimeLeftInMillis(long turnTimeLeftInMillis) {
        this.turnTimeLeftInMillis = turnTimeLeftInMillis;
    }

    public int getGameGridSize() {
        return gameGridSize;
    }

    public void setGameGridSize(int gameGridSize) {
        this.gameGridSize = gameGridSize;
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

    @Override
    public String toString() {
        return "GameRoom{" +
                "gameRoomKey='" + gameRoomKey + '\'' +
                ", gameRoomStatus='" + gameRoomState + '\'' +
                ", playerOneUID='" + playerOneUID + '\'' +
                ", playerTwoUID='" + playerTwoUID + '\'' +
                ", turnTimeInMillis=" + turnTimeInMillis +
                ", turnTimeLeftInMillis=" + turnTimeLeftInMillis +
                ", gameGridSize=" + gameGridSize +
                ", currentHost='" + currentHost + '\'' +
                ", keyOfPlayerWhoseTurnIs='" + keyOfPlayerWhoseTurnIt + '\'' +
                '}';
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