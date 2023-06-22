package com.example.baldawordgame;

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
}
