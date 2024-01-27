package com.example.baldawordgame.model;

import androidx.annotation.Nullable;

import java.util.Objects;

public class FoundWord {
    private String word;
    private String playerKey;

    public static final String INITIAL_WORD_KEY = "INITIAL_WORD_KEY";

    public FoundWord() {
    }

    public FoundWord(String word, String playerKey) {
        this.word = word;
        this.playerKey = playerKey;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getPlayerKey() {
        return playerKey;
    }

    public void setPlayerKey(String playerKey) {
        this.playerKey = playerKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FoundWord foundWord = (FoundWord) o;
        return Objects.equals(word, foundWord.word) && Objects.equals(playerKey, foundWord.playerKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(word, playerKey);
    }

    @Override
    public String toString() {
        return "FoundWord{" +
                "word='" + word + '\'' +
                ", playerKey='" + playerKey + '\'' +
                '}';
    }
}
