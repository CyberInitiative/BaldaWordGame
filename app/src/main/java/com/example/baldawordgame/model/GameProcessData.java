package com.example.baldawordgame.model;

import androidx.annotation.NonNull;

import com.example.baldawordgame.livedata.NewValueSnapshotLiveData;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

public class GameProcessData {

    private final static String TAG = "GameProcessData";

    //region PATH STRINGS
    public static final String GAME_PROCESS_DATA_PATH = "gameProcessData";
    public static final String GAME_STATUS_PATH = "gameStatus";
    public static final String HOST_PLAYER_UID_PATH = "hostPlayerUID";
    public static final String TURN_PATH = "turn";
    public static final String FIRST_PLAYER_SCORE_PATH = "firstPlayerScore";
    public static final String SECOND_PLAYER_SCORE_PATH = "secondPlayerScore";
    public static final String FIRST_PLAYER_SKIPPED_TURNS_PATH = "firstPlayerSkippedTurns";
    public static final String SECOND_PLAYER_SKIPPED_TURNS_PATH = "secondPlayerSkippedTurns";
    public static final String FIRST_PLAYER_STATUS_CODE_PATH = "firstPlayerStatusCode";
    public static final String SECOND_PLAYER_STATUS_CODE_PATH = "secondPlayerStatusCode";
    public static final String REMATCH_PATH = "rematch";
    public static final String GAME_OVER_CODE_PATH = "gameOverCode";
    //endregion

    //region DATABASE REFS
    private static final DatabaseReference ALL_GAME_PROCESS_DATA_REF
            = FirebaseDatabase.getInstance().getReference().child(GAME_PROCESS_DATA_PATH);
    private DatabaseReference currGameProcessDataRef;
    private DatabaseReference gameStatusRef;
    private DatabaseReference turnRef;
    private DatabaseReference firstPlayerScoreRef;
    private DatabaseReference secondPlayerScoreRef;
    private DatabaseReference firstPlayerSkippedTurnsRef;
    private DatabaseReference secondPlayerSkippedTurnsRef;
    private DatabaseReference firstPlayerStatusCodeRef;
    private DatabaseReference secondPlayerStatusCodeRef;
    private DatabaseReference rematchRef;
    private DatabaseReference gameOverCodeRef;
    private DatabaseReference hostPlayerUIDRef;
    //endregion

    private int firstPlayerScore, secondPlayerScore;
    private int firstPlayerSkippedTurns, secondPlayerSkippedTurns;
    private int firstPlayerStatusCode, secondPlayerStatusCode;
    private String hostPlayerUID;
    private Turn turn;
    private Rematch rematch;
    private int gameOverCode;

    public GameProcessData() {
    }

    public GameProcessData(@NonNull String gameRoomKey) {
        //region DatabaseRefs init;
        currGameProcessDataRef = ALL_GAME_PROCESS_DATA_REF.child(gameRoomKey);
        gameStatusRef = currGameProcessDataRef.child(GAME_STATUS_PATH);
        hostPlayerUIDRef = currGameProcessDataRef.child(HOST_PLAYER_UID_PATH);
        turnRef = currGameProcessDataRef.child(TURN_PATH);
        firstPlayerScoreRef = currGameProcessDataRef.child(FIRST_PLAYER_SCORE_PATH);
        secondPlayerScoreRef = currGameProcessDataRef.child(SECOND_PLAYER_SCORE_PATH);
        firstPlayerSkippedTurnsRef = currGameProcessDataRef.child(FIRST_PLAYER_SKIPPED_TURNS_PATH);
        secondPlayerSkippedTurnsRef = currGameProcessDataRef.child(SECOND_PLAYER_SKIPPED_TURNS_PATH);
        firstPlayerStatusCodeRef = currGameProcessDataRef.child(FIRST_PLAYER_STATUS_CODE_PATH);
        secondPlayerStatusCodeRef = currGameProcessDataRef.child(SECOND_PLAYER_STATUS_CODE_PATH);
        rematchRef = currGameProcessDataRef.child(REMATCH_PATH);
        gameOverCodeRef = currGameProcessDataRef.child(GAME_OVER_CODE_PATH);
        //endregion

        firstPlayerScore = 0;
        secondPlayerScore = 0;

        firstPlayerSkippedTurns = 0;
        secondPlayerSkippedTurns = 0;
    }

    public Task<Void> eraseGameProcessData(){
        return currGameProcessDataRef.removeValue();
    }

    public Task<Void> writeFirstPlayerScore(int score) {
        return firstPlayerScoreRef.setValue(firstPlayerScore + score);
    }

    public Task<Void> writeSecondPlayerScore(int score) {
        return secondPlayerScoreRef.setValue(secondPlayerScore + score);
    }

    public Task<Void> writeFirstPlayerSkippedTurn() {
        return firstPlayerSkippedTurnsRef.setValue(firstPlayerSkippedTurns + 1);
    }

    public Task<Void> writeSecondPlayerSkippedTurn() {
        return secondPlayerSkippedTurnsRef.setValue(secondPlayerSkippedTurns + 1);
    }

    public Task<Void> writeTurn(@NonNull String activePlayerKey) {
        Map<String, Object> values = new HashMap<>();
        values.put("activePlayerKey", activePlayerKey);
        values.put("turnStartedAt", ServerValue.TIMESTAMP);
        return turnRef.setValue(values);
    }

    public Task<Void> writeRematchOffering(@NonNull Rematch rematch) {
        return rematchRef.setValue(rematch);
    }

    public Task<Void> writeGameOverCode(@NonNull GameOverCode gameOverCode){
        return gameOverCodeRef.setValue(gameOverCode.getValue());
    }

    //region GETTERS AND SETTERS
    public NewValueSnapshotLiveData<String> getGameStageUniqueNewValueSnapshotLiveData() {
        return new NewValueSnapshotLiveData<>(gameStatusRef, String.class);
    }

    public NewValueSnapshotLiveData<Turn> getTurnUniqueNewValueSnapshotLiveData() {
        return new NewValueSnapshotLiveData<>(turnRef, Turn.class);
    }

    public NewValueSnapshotLiveData<Integer> getFirstPlayerScoreNewValueSnapshotLiveData() {
        return new NewValueSnapshotLiveData<>(firstPlayerScoreRef, Integer.class);
    }

    public NewValueSnapshotLiveData<Integer> getSecondPlayerScoreNewValueSnapshotLiveData() {
        return new NewValueSnapshotLiveData<>(secondPlayerScoreRef, Integer.class);
    }

    public NewValueSnapshotLiveData<Integer> getFirstPlayerSkippedTurnsNewValueSnapshotLiveData() {
        return new NewValueSnapshotLiveData<>(firstPlayerSkippedTurnsRef, Integer.class);
    }

    public NewValueSnapshotLiveData<Integer> getSecondPlayerSkippedTurnsNewValueSnapshotLiveData() {
        return new NewValueSnapshotLiveData<>(secondPlayerSkippedTurnsRef, Integer.class);
    }

    public NewValueSnapshotLiveData<Integer> getFirstPlayerStatusCodeNewValueSnapshotLiveData(){
        return new NewValueSnapshotLiveData<>(firstPlayerStatusCodeRef, Integer.class);
    }

    public NewValueSnapshotLiveData<Integer> getSecondPlayerStatusCodeNewValueSnapshotLiveData(){
        return new NewValueSnapshotLiveData<>(secondPlayerStatusCodeRef, Integer.class);
    }

    public NewValueSnapshotLiveData<Integer> getGameOverCodeNewValueSnapshotLiveData(){
        return new NewValueSnapshotLiveData<>(gameOverCodeRef, Integer.class);
    }

    public NewValueSnapshotLiveData<String> getHostPlayerUIDNewValueSnapshotLiveData(){
        return new NewValueSnapshotLiveData<>(hostPlayerUIDRef, String.class);
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

    public void setFirstPlayerSkippedTurns(int firstPlayerSkippedTurns) {
        this.firstPlayerSkippedTurns = firstPlayerSkippedTurns;
    }

    public void setSecondPlayerSkippedTurns(int secondPlayerSkippedTurns) {
        this.secondPlayerSkippedTurns = secondPlayerSkippedTurns;
    }

    public Turn getTurn() {
        return turn;
    }

    public void setTurn(Turn turn) {
        this.turn = turn;
    }

    public int getGameOverCode() {
        return gameOverCode;
    }

    public void setGameOverCode(int gameOverCode) {
        this.gameOverCode = gameOverCode;
    }

    public int getFirstPlayerStatusCode() {
        return firstPlayerStatusCode;
    }

    public void setFirstPlayerStatusCode(int firstPlayerStatusCode) {
        this.firstPlayerStatusCode = firstPlayerStatusCode;
    }

    public int getSecondPlayerStatusCode() {
        return secondPlayerStatusCode;
    }

    public void setSecondPlayerStatusCode(int secondPlayerStatusCode) {
        this.secondPlayerStatusCode = secondPlayerStatusCode;
    }

    public int getFirstPlayerSkippedTurns() {
        return firstPlayerSkippedTurns;
    }

    public int getSecondPlayerSkippedTurns() {
        return secondPlayerSkippedTurns;
    }
    //endregion

    @Override
    public String toString() {
        return "GameProcessData{" +
                "currGameProcessDataRef=" + currGameProcessDataRef +
                ", firstPlayerScore=" + firstPlayerScore +
                ", secondPlayerScore=" + secondPlayerScore +
                ", firstPlayerSkippedTurns=" + firstPlayerSkippedTurns +
                ", secondPlayerSkippedTurns=" + secondPlayerSkippedTurns +
//                ", firstPlayerStatusCode=" + firstPlayerStatusCode +
//                ", secondPlayerStatusCode=" + secondPlayerStatusCode +
                ", turn=" + turn +
                ", gameOverCode=" + gameOverCode +
                '}';
    }

    public enum GameOverCode{
        PLAYER_ONE_WIN(11),
        PLAYER_TWO_WIN(12),
        DRAW (13),
        PLAYER_ONE_SURRENDER(14),
        PLAYER_TWO_SURRENDER(15),
        PLAYER_ONE_SURRENDER_AND_LEAVE(16),
        PLAYER_TWO_SURRENDER_AND_LEAVE(17);
        private final int value;

        GameOverCode(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public enum PlayerStatusCode{
        ACTIVE(21),
        IN_BACKGROUND(22);

        private final int value;

        PlayerStatusCode(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}