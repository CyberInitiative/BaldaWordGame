package com.example.baldawordgame;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class GameEntry {
    private String gameKey;
    private String playerOneUID;
    private String playerTwoUID;
    private int gameGridSize;
    private long turnTimeInMillis;
    @Exclude
    private GameEntryObservable gameEntryObservable;

    public interface GameEntryObservable {
        void update();
    }

    public GameEntry() {
    }

    public GameEntry(@NonNull GameEntryObservable gameEntryObservable, @NonNull String gameKey, @NonNull String playerOneUID, int gameGridSize, long turnTimeInMillis) {
        this.gameEntryObservable = gameEntryObservable;
        this.playerOneUID = playerOneUID;
        this.gameGridSize = gameGridSize;
        this.turnTimeInMillis = turnTimeInMillis;
        this.gameKey = gameKey;
        DatabaseReference playerTwoUIDRef = FirebaseDatabase.getInstance().getReference().child("gameEntries").child(gameKey).child("playerTwoUID");
        playerTwoUIDRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String snapshotData = snapshot.getValue(String.class);
                if (snapshotData != null) {
                    playerTwoUID = snapshotData;
                    playerTwoUIDRef.removeEventListener(this);
                    gameEntryObservable.update();
                    playerTwoUIDRef.removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void setPlayerTwoUID(String playerTwoUID){
        this.playerTwoUID = playerTwoUID;
        FirebaseDatabase.getInstance().getReference().child("gameEntries").child(gameKey).child("playerTwoUID").setValue(playerTwoUID);
    }

    public String getPlayerOneUID() {
        return playerOneUID;
    }

    public String getPlayerTwoUID() {
        return playerTwoUID;
    }

    public int getGameGridSize() {
        return gameGridSize;
    }

    public long getTurnTimeInMillis() {
        return turnTimeInMillis;
    }

    public String getGameKey() {
        return gameKey;
    }
}
