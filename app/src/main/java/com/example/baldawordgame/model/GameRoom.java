package com.example.baldawordgame.model;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.baldawordgame.livedata.NewValueSnapshotLiveData;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;
import java.util.Random;

public class GameRoom {

    private static final String TAG = "GameRoom";

    //region PATH STRINGS
    public static final String GAME_ROOMS = "gameRooms";
    public static final String FIRST_PLAYER_UID_PATH = "firstPlayerUID";
    public static final String SECOND_PLAYER_UID_PATH = "secondPlayerUID";
    public static final String GAME_ROOM_STATUS_PATH = "gameRoomStatus";
    public static final String DATA_STATE_PATH = "dataState";
    //endregion

    public static final DatabaseReference GAME_ROOMS_REF = FirebaseDatabase.getInstance().getReference().child(GAME_ROOMS);

    private String gameRoomKey;
    private String firstPlayerUID, secondPlayerUID;
    private String gameRoomStatus;
    private String dataState;
    private int gameBoardSize;
    private Turn turn;
    private int turnDuration;

    public GameRoom() {

    }

    public GameRoom(String gameRoomKey, int gameBoardSize, int turnDuration) {
        this.gameRoomKey = gameRoomKey;
        this.firstPlayerUID = User.getPlayerUid();
        this.gameBoardSize = gameBoardSize;
        this.turnDuration = turnDuration;
        this.dataState = DataStatus.DATA_NOT_PREPARED;
        this.gameRoomStatus = GameRoom.RoomStatus.OPEN_GAME_ROOM;
    }

//    public Task<Void> tossUpFirstTurn() {
//        long seed = System.currentTimeMillis();
//        Random random = new Random(seed);
//        int randomNumber = random.nextInt(101);
//
//        if (randomNumber % 2 == 0) {
////            turn = new Turn(firstPlayerUID);
//        } else {
////            turn = new Turn(secondPlayerUID);
//        }
//        Turn turn = new Turn();
//        return GAME_ROOMS_REF.child(gameRoomKey).child(TURN).setValue(turn);
//    }

    public String tossUpWhoTurnsFirst() {
        long seed = System.currentTimeMillis();
        Random random = new Random(seed);
        int randomNumber = random.nextInt(101);

        if (randomNumber % 2 == 0) {
            return firstPlayerUID;
        }
        return secondPlayerUID;
    }

    public static Task<GameRoom> fetchGameRoom(@NonNull String key) {
        return GAME_ROOMS_REF.child(key).get()
                .continueWith(task -> {
                    GameRoom gameRoom = task.getResult().getValue(GameRoom.class);
                    if(gameRoom != null){
                        gameRoom.gameRoomKey = key;
                    }
                    Log.d(TAG, "fetchGameRoom(); " + gameRoom);
                    return gameRoom;
                });
    }

    public Task<Void> writeDataState(@NonNull String gameState){
        return GAME_ROOMS_REF.child(gameRoomKey).child(DATA_STATE_PATH).setValue(gameState);
    }

    public NewValueSnapshotLiveData<String> getDataStateUniqueSnapshotLiveData(){
        return new NewValueSnapshotLiveData<>(GAME_ROOMS_REF.child(gameRoomKey).child(DATA_STATE_PATH), String.class);
    }
//
//    public NewValueSnapshotLiveData<Turn> getTurnUniqueSnapshotLiveData(){
//        return new NewValueSnapshotLiveData<>(GAME_ROOMS_REF.child(gameRoomKey).child(TURN), Turn.class);
//    }

    //region GETTERS
    @Exclude
    public String getGameRoomKey() {
        return gameRoomKey;
    }

    public String getFirstPlayerUID() {
        return firstPlayerUID;
    }
    public String getSecondPlayerUID() {
        return secondPlayerUID;
    }
    public int getGameBoardSize() {
        return gameBoardSize;
    }
    public int getTurnDuration() {
        return turnDuration;
    }
    public Turn getTurn() {
        return turn;
    }
    public String getGameRoomStatus() {
        return gameRoomStatus;
    }
    public String getDataState() {
        return dataState;
    }
    public String getOpponentKey(){
        if(User.getPlayerUid().equals(firstPlayerUID)){
            return secondPlayerUID;
        } else
            return firstPlayerUID;
    }
    //endregion

    public void setTurn(Turn turn) {
        this.turn = turn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameRoom gameRoom = (GameRoom) o;
        return gameBoardSize == gameRoom.gameBoardSize && turnDuration == gameRoom.turnDuration && Objects.equals(gameRoomKey, gameRoom.gameRoomKey) && Objects.equals(firstPlayerUID, gameRoom.firstPlayerUID) && Objects.equals(secondPlayerUID, gameRoom.secondPlayerUID) && Objects.equals(gameRoomStatus, gameRoom.gameRoomStatus) && Objects.equals(dataState, gameRoom.dataState);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gameRoomKey, firstPlayerUID, secondPlayerUID, gameRoomStatus, dataState, gameBoardSize, turnDuration);
    }

    @Override
    public String toString() {
        return "GameRoom{" +
                "gameRoomKey='" + gameRoomKey + '\'' +
                ", hostUID='" + firstPlayerUID + '\'' +
                ", guestUID='" + secondPlayerUID + '\'' +
                ", gameRoomStatus='" + gameRoomStatus + '\'' +
                ", gameStage='" + dataState + '\'' +
                ", gameBoardSize=" + gameBoardSize +
                ", turnDuration=" + turnDuration +
                '}';
    }

    public static final class RoomStatus{
        public static final String OPEN_GAME_ROOM = "OPEN_GAME_ROOM";
        public static final String FULL_GAME_ROOM = "FULL_GAME_ROOM";
    }
    public static final class DataStatus {
        public static final String DATA_NOT_PREPARED = "DATA_NOT_PREPARED";
        public static final String DATA_PREPARED = "DATA_PREPARED";
    }

}