package com.example.baldawordgame.model;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.baldawordgame.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class GameRoom {
    private static final String TAG = "GameRoom";

    //region PATH STRINGS
    public static final String GAME_ROOMS_PATH = "gameRooms";
    public static final String HOST_UID_PATH = "hostUID";
    public static final String GUEST_UID_PATH = "guestUID";
    public static final String GAME_ROOM_STATUS_PATH = "gameRoomStatus";
    //endregion

    public static final DatabaseReference GAME_ROOMS_REF = FirebaseDatabase.getInstance().getReference().child(GAME_ROOMS_PATH);

    private String gameRoomKey;
    private String hostUID; //The player who created game room;
    private String guestUID; //The player who connected;
    private String gameRoomStatus;
    private int gameBoardSize;
    private int turnDuration;

    public GameRoom() {

    }

    public GameRoom(int gameBoardSize, int turnDuration) {
        this.hostUID = User.getPlayerUid();
        this.gameBoardSize = gameBoardSize;
        this.turnDuration = turnDuration;
        this.gameRoomStatus = GameRoom.OPEN_GAME_ROOM;
    }

    public static Task<GameRoom> fetchGameRoom(@NonNull String key) {
        return GAME_ROOMS_REF.child(key).get()
                .continueWith(task -> {
                    GameRoom gameRoom = task.getResult().getValue(GameRoom.class);
                    if(gameRoom != null) {
                        gameRoom.gameRoomKey = key;
                    }
                    Log.d(TAG, "fetchGameRoom(); " + gameRoom);
                    return gameRoom;
                });
    }

    @Exclude
    public String getGameRoomKey() {
        return gameRoomKey;
    }

    public String getHostUID() {
        return hostUID;
    }

    public String getGuestUID() {
        return guestUID;
    }

    public void setGuestUID(@NonNull String guestUID) {
        this.guestUID = guestUID;
    }

    public int getGameBoardSize() {
        return gameBoardSize;
    }

    public int getTurnDuration() {
        return turnDuration;
    }

    public String getGameRoomStatus() {
        return gameRoomStatus;
    }

    public void setGameRoomStatus(String gameRoomStatus) {
        this.gameRoomStatus = gameRoomStatus;
    }

    public String getOpponentKey(){
        if(User.getPlayerUid().equals(hostUID)){
            return guestUID;
        } else
            return hostUID;
    }

    public static final String OPEN_GAME_ROOM = "OPEN_GAME_ROOM";
    public static final String FULL_GAME_ROOM = "FULL_GAME_ROOM";

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameRoom gameRoom = (GameRoom) o;
        return gameBoardSize == gameRoom.gameBoardSize && turnDuration == gameRoom.turnDuration && Objects.equals(gameRoomKey, gameRoom.gameRoomKey) && Objects.equals(hostUID, gameRoom.hostUID) && Objects.equals(guestUID, gameRoom.guestUID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gameRoomKey, hostUID, guestUID, gameBoardSize, turnDuration);
    }

    @Override
    public String toString() {
        return "GameRoom{" +
                "gameRoomKey='" + gameRoomKey + '\'' +
                ", playerOneUID='" + hostUID + '\'' +
                ", playerTwoUID='" + guestUID + '\'' +
                ", gameRoomStatus='" + gameRoomStatus + '\'' +
                ", gameGridSize=" + gameBoardSize +
                ", turnTimeInMillis=" + turnDuration +
                '}';
    }
}
