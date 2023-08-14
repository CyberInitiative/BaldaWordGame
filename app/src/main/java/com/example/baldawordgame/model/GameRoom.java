package com.example.baldawordgame.model;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class GameRoom {

    public static final DatabaseReference GAME_ROOMS_REF = FirebaseDatabase.getInstance().getReference().child("gameRooms");
    private static final String TAG = "GameRoom";

    private String gameRoomKey;
    private String playerOneUID; //The player who created game room;
    private String playerTwoUID; //The player who connected;
    private String gameRoomStatus;
    private int gameGridSize;
    private long turnTimeInMillis;

    public GameRoom() {

    }

    public GameRoom(@NonNull String gameRoomKey, @NonNull String roomCreatedUserUID, int gameGridSize, long turnTimeInMillis) {
        this.gameRoomKey = gameRoomKey;
        this.playerOneUID = roomCreatedUserUID;
        this.gameGridSize = gameGridSize;
        this.turnTimeInMillis = turnTimeInMillis;
        this.gameRoomStatus = GameRoom.OPEN_GAME_ROOM;
    }

    public static Task<GameRoom> fetchGameRoom(@NonNull String gameRoomKey) {
        return GAME_ROOMS_REF.child(gameRoomKey).get()
                .continueWith(task -> {
                    GameRoom gameProcessData = task.getResult().getValue(GameRoom.class);
                    Log.d(TAG, "fetchGameRoom(); " + gameProcessData);
                    return gameProcessData;
                });
    }

    public String getGameRoomKey() {
        return gameRoomKey;
    }

    public String getPlayerOneUID() {
        return playerOneUID;
    }

    public String getPlayerTwoUID() {
        return playerTwoUID;
    }

    public void setPlayerTwoUID(@NonNull String playerTwoUID) {
        this.playerTwoUID = playerTwoUID;
    }

    public int getGameGridSize() {
        return gameGridSize;
    }

    public long getTurnTimeInMillis() {
        return turnTimeInMillis;
    }

    public String getGameRoomStatus() {
        return gameRoomStatus;
    }

    public void setGameRoomStatus(String gameRoomStatus) {
        this.gameRoomStatus = gameRoomStatus;
    }

    public static final String OPEN_GAME_ROOM = "OPEN_GAME_ROOM";
    public static final String FULL_GAME_ROOM = "FULL_GAME_ROOM";

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameRoom gameRoom = (GameRoom) o;
        return gameGridSize == gameRoom.gameGridSize && turnTimeInMillis == gameRoom.turnTimeInMillis && Objects.equals(gameRoomKey, gameRoom.gameRoomKey) && Objects.equals(playerOneUID, gameRoom.playerOneUID) && Objects.equals(playerTwoUID, gameRoom.playerTwoUID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gameRoomKey, playerOneUID, playerTwoUID, gameGridSize, turnTimeInMillis);
    }

    @Override
    public String toString() {
        return "GameRoom{" +
                "gameRoomKey='" + gameRoomKey + '\'' +
                ", playerOneUID='" + playerOneUID + '\'' +
                ", playerTwoUID='" + playerTwoUID + '\'' +
                ", gameRoomStatus='" + gameRoomStatus + '\'' +
                ", gameGridSize=" + gameGridSize +
                ", turnTimeInMillis=" + turnTimeInMillis +
                '}';
    }
}
