package com.example.baldawordgame;

import androidx.annotation.NonNull;
import java.util.ArrayList;

public class GameVocabulary {

    private static final String TAG = "GAME_VOCABULARY";
    private String initialWord;
    private ArrayList<FoundedWord> playersVocabulary = new ArrayList<>();
    private ArrayList<FoundedWord> opponentsVocabulary = new ArrayList<>();

    public enum WordCheckResult {
        NOT_IN_THE_DICTIONARY,
        ALREADY_FOUNDED,
        NEW_FOUND_WORD
    }

    public ArrayList<FoundedWord> getPlayersVocabulary() {
        return playersVocabulary;
    }

    public ArrayList<FoundedWord> getOpponentsVocabulary() {
        return opponentsVocabulary;
    }

    public String getInitialWord() {
        return initialWord;
    }

    public void setInitialWord(String initialWord) {
        this.initialWord = initialWord;
    }
}