package com.example.baldawordgame.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Objects;

public class FoundWord {
    private String word;
    private String playerKey;
    private ArrayList<LetterCell> letters = new ArrayList<>();

    public static final String INITIAL_WORD_KEY = "INITIAL_WORD_KEY";

    public FoundWord() {
    }

    public FoundWord(@NonNull String word, @NonNull String playerKey, ArrayList<LetterCell> letters) {
        this.word = word;
        this.playerKey = playerKey;
        this.letters = letters;
    }

    public static FoundWord initialWord(@NonNull String word){
        return new FoundWord(word, INITIAL_WORD_KEY, null);
    }

    public String getWord() {
        return word;
    }

    public String getPlayerKey() {
        return playerKey;
    }

    public ArrayList<LetterCell> getLetters() {
        return letters;
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
