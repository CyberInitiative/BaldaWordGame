package com.example.baldawordgame;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GameRoom {

    private final static String TAG = "GAME_ROOM";

    private String gameRoomKey;
    private String gameRoomStatus;
    //Игрок который создал комнату;
    private String playerOneUID;
    //Игрок который подключился;
    private String playerTwoUID;
    private String initialWord;
    private long turnTimeInMillis;
    private long turnTimeLeftInMillis;
    private int gameGridSize;
    private String currentHost;
    private String keyOfPlayerWhoseTurnIs;

    @Exclude
    private GameRoomObserver observable;
    @Exclude
    private HashMap<DatabaseReference, ValueEventListener> listenersHashMap = new HashMap<>();

    @Exclude
    private DatabaseReference currentGameRef;

    public interface GameRoomObserver {
        void update();
    }

    public interface GameRoomWaiter {
        void receiveGame(GameRoom gameRoom);
    }

    public GameRoom() {
    }

    private GameRoom(@NonNull GameRoomObserver gameRoomObservable, @NonNull String gameRoomKey, @NonNull String roomCreatedUserUID, int gameGridSize, long turnTime) {
        this.gameRoomKey = gameRoomKey;
        this.playerOneUID = roomCreatedUserUID;
        this.gameGridSize = gameGridSize;
        this.turnTimeInMillis = turnTime;
        this.observable = gameRoomObservable;
        currentHost = roomCreatedUserUID;
        currentGameRef = FirebaseDatabase.getInstance().getReference().child("openRooms").child(gameRoomKey);
    }

    public static GameRoom gameCreationStage(@NonNull GameRoomObserver gameRoomObservable, @NonNull String gameRoomKey, @NonNull String roomCreatedUserUID, int gameGridSize, long turnTime) {
        GameRoom gameRoom = new GameRoom(gameRoomObservable, gameRoomKey, roomCreatedUserUID, gameGridSize, turnTime);
        gameRoom.setPlayerTwoUIDListener();
        return gameRoom;
    }

    public static Task<GameRoom> getGameRoomFromFirebase(String gameRoomKey) {
        return FirebaseDatabase.getInstance().getReference().child("openRooms").child(gameRoomKey).get()
                .continueWith(new Continuation<DataSnapshot, GameRoom>() {
                    @Override
                    public GameRoom then(@NonNull Task<DataSnapshot> task) throws Exception {
                        if (task.isSuccessful()) {
                            return task.getResult().getValue(GameRoom.class);
                        }
                        return null;
                    }
                }).continueWith(new Continuation<GameRoom, GameRoom>() {
                    @Override
                    public GameRoom then(@NonNull Task<GameRoom> task) throws Exception {
                        if (task.isSuccessful()) {
                            GameRoom gameRoom = task.getResult();
//                            gameRoom.addInitialWordListener();
                            return gameRoom;
                        }
                        return null;
                    }
                });
    }

    public void addListeners() {
        addInitialWordListener();
        addKeyOfPlayerWhoseTurnIsListener();
        addCurrentHostListener();
    }

    private void addGameRoomStatusListener() {
        DatabaseReference ref = getGameRoomStatusRef();
        if (ref != null) {
            if (!listenersHashMap.containsKey(ref)) {
                ValueEventListener valueEventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        gameRoomStatus = snapshot.getValue(String.class);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                };
                listenersHashMap.put(ref, valueEventListener);
            }
        }
    }

    private void removeGameRoomStatusListener() {
        DatabaseReference ref = getGameRoomStatusRef();
        if (ref != null) {
            listenersHashMap.remove(ref);
        }
    }

    private void addKeyOfPlayerWhoseTurnIsListener() {
        DatabaseReference ref = getKeyOfPlayerWhoseTurnIsRef();
        if (ref != null) {
            if (!listenersHashMap.containsKey(ref)) {
                ValueEventListener valueEventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        keyOfPlayerWhoseTurnIs = snapshot.getValue(String.class);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                };
                listenersHashMap.put(ref, valueEventListener);
            }
        }
    }

    private void removeKeyOfPlayerWhoseTurnIsListener() {
        DatabaseReference ref = getKeyOfPlayerWhoseTurnIsRef();
        if (ref != null) {
            listenersHashMap.remove(ref);
        }
    }

    private void addCurrentHostListener() {
        DatabaseReference ref = getCurrentHostRef();
        if (ref != null) {
            if (!listenersHashMap.containsKey(ref)) {
                ValueEventListener valueEventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String data = snapshot.getValue(String.class);
                        if (data != null) {
                            currentHost = data;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                };
                listenersHashMap.put(ref, valueEventListener);
            }
        }
    }

    private void removeCurrentHostListener() {
        DatabaseReference ref = getCurrentHostRef();
        if (ref != null) {
            listenersHashMap.remove(ref);
        }
    }

    private void addInitialWordListener() {
        DatabaseReference ref = getInitialWordRef();
        if (ref != null) {
            if (!listenersHashMap.containsKey(ref)) {
                ValueEventListener valueEventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String snapshotData = snapshot.getValue(String.class);
                        if (snapshotData != null) {
                            initialWord = snapshot.getValue(String.class);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                };
                listenersHashMap.put(ref, valueEventListener);
            }
        }
    }

    public void addInitialWordListener(ValueEventListener valueEventListener) {
        DatabaseReference ref = getInitialWordRef();
        if (ref != null) {
            if (!listenersHashMap.containsKey(ref)) {
                listenersHashMap.put(ref, valueEventListener);
            }
        }
    }
    
    public void stopInitialWordListening() {
        DatabaseReference ref = getInitialWordRef();
        if (ref != null) {
            if (listenersHashMap.containsKey(ref)) {
                ValueEventListener valueEventListener = listenersHashMap.get(ref);
                if(valueEventListener != null){
                    ref.removeEventListener(valueEventListener);
                }
            }
        }
    }

    public void startInitialWordListening() {
        DatabaseReference ref = getInitialWordRef();
        if (ref != null) {
            if (listenersHashMap.containsKey(ref)) {
                ValueEventListener valueEventListener = listenersHashMap.get(ref);
                if(valueEventListener != null){
                    ref.addValueEventListener(valueEventListener);
                }
            }
        }
    }

    private void removeInitialWordListener() {
        DatabaseReference ref = getInitialWordRef();
        if (ref != null) {
            listenersHashMap.remove(ref);
        }
    }

    private void secondPlayerInitialWordListener(GameRoomWaiter gameRoomWaiter) {
        DatabaseReference ref = getInitialWordRef();
        if (ref != null) {
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String snapshotData = snapshot.getValue(String.class);
                    if (snapshotData != null) {
                        initialWord = snapshot.getValue(String.class);
                        gameRoomWaiter.receiveGame(GameRoom.this);
                        GameRoom.this.addInitialWordListener();
                        ref.removeEventListener(this);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
    }

    private void setPlayerTwoUIDListener() {
        getPlayerTwoUIDRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String snapshotData = snapshot.getValue(String.class);
                if (snapshotData != null) {
                    playerTwoUID = snapshotData;
                    gameRoomStatus = GAME_IS_READY;
                    getGameRoomStatusRef().setValue(GAME_IS_READY);
                    getPlayerTwoUIDRef().removeEventListener(this);
                    observable.update();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Exclude
    public DatabaseReference getGameRoomStatusRef() {
        DatabaseReference ref = getCurrentGameRef();
        if (ref != null) {
            return ref.child("gameRoomStatus");
        }
        return null;
    }

    @Exclude
    public DatabaseReference getPlayerOneUIDRef() {
        DatabaseReference ref = getCurrentGameRef();
        if (ref != null) {
            return ref.child("playerOneUID");
        }
        return null;
    }

    @Exclude
    public DatabaseReference getPlayerTwoUIDRef() {
        DatabaseReference ref = getCurrentGameRef();
        if (ref != null) {
            return ref.child("playerTwoUID");
        }
        return null;
    }

    @Exclude
    public DatabaseReference getTurnTimeLeftInMillisRef() {
        DatabaseReference ref = getCurrentGameRef();
        if (ref != null) {
            return ref.child("turnTimeLeftInMillis");
        }
        return null;
    }

    @Exclude
    public DatabaseReference getCurrentHostRef() {
        DatabaseReference ref = getCurrentGameRef();
        if (ref != null) {
            return ref.child("currentHost");
        }
        return null;
    }

    @Exclude
    public DatabaseReference getKeyOfPlayerWhoseTurnIsRef() {
        DatabaseReference ref = getCurrentGameRef();
        if (ref != null) {
            return ref.child("keyOfPlayerWhoseTurnIs");
        }
        return null;
    }

    @Exclude
    public DatabaseReference getInitialWordRef() {
        DatabaseReference ref = getCurrentGameRef();
        if (ref != null) {
            return ref.child("initialWord");
        }
        return null;
    }

    @Exclude
    public DatabaseReference getCurrentGameRef() {
        if (gameRoomKey != null) {
            return FirebaseDatabase.getInstance().getReference().child("openRooms").child(gameRoomKey);
        }
        return null;
    }

    @Exclude
    public DatabaseReference getFoundedWordsRef() {
        DatabaseReference ref = getCurrentGameRef();
        if (ref != null) {
            return ref.child("foundedWords");
        }
        return null;
    }

    public String getGameRoomKey() {
        return gameRoomKey;
    }

    public void setGameRoomStatus(@NonNull String gameRoomStatus) {
        this.gameRoomStatus = gameRoomStatus;
    }

    public void setGameRoomStatusRefValue(@NonNull String gameRoomStatus) {
        getGameRoomStatusRef().setValue(gameRoomStatus);
    }

    public String getGameRoomStatus() {
        return gameRoomStatus;
    }

    public String getPlayerOneUID() {
        return playerOneUID;
    }

    public void setPlayerTwoUID(@NonNull String playerTwoUID) {
        this.playerTwoUID = playerTwoUID;
    }

    public void setRoomJoinedUserUIDRefValue(@NonNull String roomJoinedUserUID) {
        getPlayerTwoUIDRef().setValue(roomJoinedUserUID);
    }

    public String getPlayerTwoUID() {
        return playerTwoUID;
    }

    public void setKeyOfPlayerWhoseTurnIs(@NonNull String keyOfPlayerWhoseTurnIs) {
        this.keyOfPlayerWhoseTurnIs = keyOfPlayerWhoseTurnIs;
    }

    public void setKeyOfPlayerWhoseTurnIsRefValue(@NonNull String keyOfPlayerWhoseTurnIs) {
        getKeyOfPlayerWhoseTurnIsRef().setValue(keyOfPlayerWhoseTurnIs);
    }

    public String getKeyOfPlayerWhoseTurnIs() {
        return keyOfPlayerWhoseTurnIs;
    }

    public void setInitialWord(@NonNull String initialWord) {
        this.initialWord = initialWord;
    }

    public void setInitialWordRefValue(@NonNull String initialWord) {
        getInitialWordRef().setValue(initialWord);
    }

    public String getInitialWord() {
        return initialWord;
    }

    public void setGameGridSize(int gameGridSize) {
        this.gameGridSize = gameGridSize;
    }

    public int getGameGridSize() {
        return gameGridSize;
    }

    public void setTurnTimeInMillis(long turnTimeInMillis) {
        this.turnTimeInMillis = turnTimeInMillis;
    }

    public long getTurnTimeInMillis() {
        return turnTimeInMillis;
    }

    public void setTurnTimeLeftInMillis(long turnTimeLeftInMillis) {
        this.turnTimeLeftInMillis = turnTimeLeftInMillis;
    }

    public void setTurnTimeLeftInMillisRefValue(long turnTimeLeftInMillis) {
        getTurnTimeLeftInMillisRef().setValue(turnTimeLeftInMillis);
    }

    public long getTurnTimeLeftInMillis() {
        return turnTimeLeftInMillis;
    }

    public void setCurrentHost(@NonNull String currentHost) {
        this.currentHost = currentHost;
    }

    public void setCurrentHostValue(@NonNull String currentHost) {
        getCurrentGameRef().setValue(currentHost);
    }

    public String getCurrentHost() {
        return currentHost;
    }

    @Override
    public String toString() {
        return "GameRoom{" +
                "gameRoomKey='" + gameRoomKey + '\'' +
                ", gameRoomStatus='" + gameRoomStatus + '\'' +
                ", playerOneUID='" + playerOneUID + '\'' +
                ", playerTwoUID='" + playerTwoUID + '\'' +
                ", initialWord='" + initialWord + '\'' +
                ", turnTimeInMillis=" + turnTimeInMillis +
                ", turnTimeLeftInMillis=" + turnTimeLeftInMillis +
                ", gameGridSize=" + gameGridSize +
                ", currentHost='" + currentHost + '\'' +
                ", keyOfPlayerWhoseTurnIs='" + keyOfPlayerWhoseTurnIs + '\'' +
                '}';
    }

    @Exclude
    private static final String GAME_RESTARTING = "GAME_RESTARTING";
    @Exclude
    public static final String GAME_IS_STARTED = "GAME_IS_STARTED";
    @Exclude
    public static final String GAME_IS_READY = "GAME_IS_READY";
    @Exclude
    public static final String GAME_IS_OVER = "GAME_IS_OVER";

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
}

//                            gameRoom.getInitialWordRef().addValueEventListener(new ValueEventListener() {
//                                @Override
//                                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                    String dataFromSnapshot = snapshot.getValue(String.class);
//                                    if (dataFromSnapshot != null) {
//                                        gameRoom.initialWord = dataFromSnapshot;
//                                        Log.d(TAG, "INITIAL WORD LISTENER TRIGGERED; INITIAL WORD IS: " + gameRoom.initialWord);
//                                        gameRoom.getInitialWordRef().removeEventListener(this);
//                                        gameRoomWaiter.receiveGame(gameRoom);
//                                    }
//                                }
//
//                                @Override
//                                public void onCancelled(@NonNull DatabaseError error) {
//                                }
//                            });