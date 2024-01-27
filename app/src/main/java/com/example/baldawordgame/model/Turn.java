package com.example.baldawordgame.model;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ServerValue;

import java.util.Map;
import java.util.Objects;

public class Turn {

    private long turnStartedAt;
    private String activePlayerKey;

    public Turn(){}

    public long getTurnStartedAt() {
        return turnStartedAt;
    }

    public String getActivePlayerKey() {
        return activePlayerKey;
    }

    @Override
    public String toString() {
        return "Turn{" +
                "turnStartedAt=" + turnStartedAt +
                ", activePlayerKey='" + activePlayerKey + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Turn turn = (Turn) o;
        return turnStartedAt == turn.turnStartedAt && Objects.equals(activePlayerKey, turn.activePlayerKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(turnStartedAt, activePlayerKey);
    }
}