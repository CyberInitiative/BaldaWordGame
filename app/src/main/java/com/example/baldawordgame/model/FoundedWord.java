package com.example.baldawordgame.model;

import androidx.annotation.Nullable;

public class FoundedWord {
    private String word;
    private String playerKey;

    public FoundedWord() {
    }

    public FoundedWord(String word, String playerKey) {
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
    public boolean equals(@Nullable Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != this.getClass()) {
            return false;
        }
        FoundedWord foundedWord = (FoundedWord) obj;
        if (this.word != null && foundedWord.word != null)
            if (this.word.equals(foundedWord.word)) {
                return true;
            }
        return false;
    }
}
