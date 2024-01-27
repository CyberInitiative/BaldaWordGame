package com.example.baldawordgame.model;

import java.util.Objects;

public class Rematch {

    private long rematchOfferedAt;
    private int firstPlayerRematchStatusCode, secondPlayerRematchStatusCode;

    public Rematch() {}

    private Rematch(int firstPlayerRematchStatusCode, int secondPlayerRematchStatusCode) {
        this.firstPlayerRematchStatusCode = firstPlayerRematchStatusCode;
        this.secondPlayerRematchStatusCode = secondPlayerRematchStatusCode;
    }

    public static Rematch firstPlayerOfferedRematch(){
        return new Rematch(RematchStatusCode.ACCEPT.value, 0);
    }

    public static Rematch secondPlayerOfferedRematch(){
        return new Rematch(0, RematchStatusCode.ACCEPT.value);
    }

    public long getRematchOfferedAt() {
        return rematchOfferedAt;
    }

    public int getFirstPlayerRematchStatusCode() {
        return firstPlayerRematchStatusCode;
    }

    public int getSecondPlayerRematchStatusCode() {
        return secondPlayerRematchStatusCode;
    }

    public enum RematchStatusCode{
        ACCEPT(31),
        DECLINE(32);

        private final int value;

        RematchStatusCode(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    @Override
    public String toString() {
        return "Rematch{" +
                "rematchOfferedAt=" + rematchOfferedAt +
                ", firstPlayerRematchStatusCode=" + firstPlayerRematchStatusCode +
                ", secondPlayerRematchStatusCode=" + secondPlayerRematchStatusCode +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rematch rematch = (Rematch) o;
        return rematchOfferedAt == rematch.rematchOfferedAt;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rematchOfferedAt);
    }
}
