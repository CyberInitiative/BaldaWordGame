package com.example.baldawordgame;

import androidx.annotation.NonNull;

import com.example.baldawordgame.model.GameProcessData;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

public class GameProcessAccessor {

    //region PATH STRINGS
    public static final String GAME_PROCESS_DATA_PATH = "gameProcessData";
    public static final String FIRST_PLAYER_SCORE_PATH = "firstPlayerScore";
    public static final String SECOND_PLAYER_SCORE_PATH = "secondPlayerScore";
    public static final String FIRST_PLAYER_SKIPPED_TURNS_PATH = "firstPlayerSkippedTurns";
    public static final String SECOND_PLAYER_SKIPPED_TURNS_PATH = "secondPlayerSkippedTurns";
    public static final String TURN_PATH = "turn";
    //endregion

    private static final DatabaseReference ALL_GAME_PROCESS_DATA_REF = FirebaseDatabase.getInstance().getReference().child(GAME_PROCESS_DATA_PATH);

    private String gameRoomKey;

    public GameProcessAccessor(@NonNull String gameRoomKey) {
        this.gameRoomKey = gameRoomKey;
    }

    public Task<GameProcessData> fetchGameProcessData() {
        return ALL_GAME_PROCESS_DATA_REF.child(gameRoomKey).get()
                .continueWith(task -> {
                    if(task.getResult() != null){
                        return task.getResult().getValue(GameProcessData.class);
                    }
                   return null;
                });
    }

    public Task<Void> writeGameProcessData(@NonNull GameProcessData gameProcessData){
        return ALL_GAME_PROCESS_DATA_REF.child(gameRoomKey).setValue(gameProcessData);
    }

    public Task<Void> writeFirstPlayerScore(int score) {
        return ALL_GAME_PROCESS_DATA_REF.child(gameRoomKey).child(FIRST_PLAYER_SCORE_PATH).setValue(score);
    }

    public Task<Void> writeSecondPlayerScore(int score) {
        return ALL_GAME_PROCESS_DATA_REF.child(gameRoomKey).child(SECOND_PLAYER_SCORE_PATH).setValue(score);
    }

    public Task<Void> writeFirstPlayerSkippedTurn(int skippedTurnsTotal) {
        return ALL_GAME_PROCESS_DATA_REF.child(gameRoomKey).child(FIRST_PLAYER_SKIPPED_TURNS_PATH).setValue(skippedTurnsTotal);
    }

    public Task<Void> writeSecondPlayerSkippedTurn(int skippedTurnsTotal) {
        return ALL_GAME_PROCESS_DATA_REF.child(gameRoomKey).child(SECOND_PLAYER_SKIPPED_TURNS_PATH).setValue(skippedTurnsTotal);
    }

    public Task<Void> writeTurnData(@NonNull String activePlayerKey) {
        Map<String, Object> values = new HashMap<>();
        values.put("activePlayerKey", activePlayerKey);
        values.put("turnStartedAt", ServerValue.TIMESTAMP);
        return ALL_GAME_PROCESS_DATA_REF.child(gameRoomKey).child(TURN_PATH).setValue(values);
    }
}
