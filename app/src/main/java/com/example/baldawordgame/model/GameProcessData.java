package com.example.baldawordgame.model;

import androidx.annotation.NonNull;

import com.example.baldawordgame.livedata.NewValueSnapshotLiveData;
import com.example.baldawordgame.livedata.SnapshotLiveData;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

public class GameProcessData {

    private final static String TAG = "GameProcessData";

    //region PATH STRINGS
    public static final String GAME_PROCESS_DATA_PATH = "gameProcessData";
    public static final String GAME_STATUS_PATH = "gameStatus";
    public static final String ACTIVE_SUPERVISOR_UI_PATH = "activeSupervisorUI";
    public static final String TURN_PATH = "turn";
    public static final String FIRST_PLAYER_SCORE_PATH = "firstPlayerScore";
    public static final String SECOND_PLAYER_SCORE_PATH = "secondPlayerScore";
    public static final String FIRST_PLAYER_SKIPPED_TURNS_PATH = "firstPlayerSkippedTurns";
    public static final String SECOND_PLAYER_SKIPPED_TURNS_PATH = "secondPlayerSkippedTurns";
    //endregion

    //region DATABASE REFS
    private static final DatabaseReference allGameProcessDataRef = FirebaseDatabase.getInstance().getReference().child(GAME_PROCESS_DATA_PATH);
    private DatabaseReference currGameProcessDataRef;
    private DatabaseReference gameStatusRef;
    private DatabaseReference activeSupervisorRef;
    private DatabaseReference turnRef;
    private DatabaseReference firstPlayerScoreRef;
    private DatabaseReference secondPlayerScoreRef;
    private DatabaseReference firstPlayerSkippedTurnsRef;
    private DatabaseReference secondPlayerSkippedTurnsRef;
    //endregion

    private int firstPlayerScore = 0;
    private int secondPlayerScore = 0;

    public GameProcessData() {
    }

    public GameProcessData(@NonNull String gameRoomKey) {
        //region DatabaseRefs init;
        currGameProcessDataRef = allGameProcessDataRef.child(gameRoomKey);
        gameStatusRef = currGameProcessDataRef.child(GAME_STATUS_PATH);
        activeSupervisorRef = currGameProcessDataRef.child(ACTIVE_SUPERVISOR_UI_PATH);
        turnRef = currGameProcessDataRef.child(TURN_PATH);
        firstPlayerScoreRef = currGameProcessDataRef.child(FIRST_PLAYER_SCORE_PATH);
        secondPlayerScoreRef = currGameProcessDataRef.child(SECOND_PLAYER_SCORE_PATH);
        firstPlayerSkippedTurnsRef = currGameProcessDataRef.child(FIRST_PLAYER_SKIPPED_TURNS_PATH);
        secondPlayerSkippedTurnsRef = currGameProcessDataRef.child(SECOND_PLAYER_SKIPPED_TURNS_PATH);
        //endregion

        firstPlayerScore = 0;
        secondPlayerScore = 0;
    }

    public Task<Void> writeGameStage(@NonNull String gameStage) {
        return gameStatusRef.setValue(gameStage);
    }

    public Task<Void> writeFirstPlayerScore(int score) {
        return firstPlayerScoreRef.setValue(firstPlayerScore + score);
    }

    public Task<Void> writeSecondPlayerScore(int score) {
        return firstPlayerScoreRef.setValue(secondPlayerScore + score);
    }

    public Task<Void> writeTurn(@NonNull String activePlayerKey) {
        Map<String, Object> values = new HashMap<>();
        values.put("activePlayerKey", activePlayerKey);
        values.put("turnStartedAt", ServerValue.TIMESTAMP);

        return turnRef.setValue(values);
    }

    public NewValueSnapshotLiveData<String> getGameStageUniqueSnapshotLiveData() {
        return new NewValueSnapshotLiveData<>(gameStatusRef, String.class);
    }

    public NewValueSnapshotLiveData<Turn> getTurnUniqueSnapshotLiveData() {
        return new NewValueSnapshotLiveData<>(turnRef, Turn.class);
    }

    public NewValueSnapshotLiveData<Integer> getFirstPlayerScoreSnapshotLiveData() {
        return new NewValueSnapshotLiveData<>(firstPlayerScoreRef, Integer.class);
    }

    public NewValueSnapshotLiveData<Integer> getSecondPlayerScoreSnapshotLiveData() {
        return new NewValueSnapshotLiveData<>(secondPlayerScoreRef, Integer.class);
    }

    public NewValueSnapshotLiveData<Integer> getFirstPlayerSkippedTurns(){
        return new NewValueSnapshotLiveData<>(firstPlayerSkippedTurnsRef, Integer.class);
    }

    public NewValueSnapshotLiveData<Integer> getSecondPlayerSkippedTurns(){
        return new NewValueSnapshotLiveData<>(secondPlayerSkippedTurnsRef, Integer.class);
    }

    public DatabaseReference getFirstPlayerSkippedTurnsRef() {
        return firstPlayerSkippedTurnsRef;
    }

    public void setFirstPlayerSkippedTurnsRef(DatabaseReference firstPlayerSkippedTurnsRef) {
        this.firstPlayerSkippedTurnsRef = firstPlayerSkippedTurnsRef;
    }

    public DatabaseReference getSecondPlayerSkippedTurnsRef() {
        return secondPlayerSkippedTurnsRef;
    }

    public void setSecondPlayerSkippedTurnsRef(DatabaseReference secondPlayerSkippedTurnsRef) {
        this.secondPlayerSkippedTurnsRef = secondPlayerSkippedTurnsRef;
    }

    public int getFirstPlayerScore() {
        return firstPlayerScore;
    }

    public void setFirstPlayerScore(int firstPlayerScore) {
        this.firstPlayerScore = firstPlayerScore;
    }

    public int getSecondPlayerScore() {
        return secondPlayerScore;
    }

    public void setSecondPlayerScore(int secondPlayerScore) {
        this.secondPlayerScore = secondPlayerScore;
    }

    @Override
    public String toString() {
        return "GameProcessData{" +
                "currGameProcessDataRef=" + currGameProcessDataRef +
                ", gameStatusRef=" + gameStatusRef +
                ", activeSupervisorRef=" + activeSupervisorRef +
                ", turnRef=" + turnRef +
                ", firstPlayerScoreRef=" + firstPlayerScoreRef +
                ", secondPlayerScoreRef=" + secondPlayerScoreRef +
                ", firstPlayerSkippedTurnsRef=" + firstPlayerSkippedTurnsRef +
                ", secondPlayerSkippedTurnsRef=" + secondPlayerSkippedTurnsRef +
                ", firstPlayerScore=" + firstPlayerScore +
                ", secondPlayerScore=" + secondPlayerScore +
                '}';
    }
}