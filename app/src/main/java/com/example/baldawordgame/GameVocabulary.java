package com.example.baldawordgame;

import androidx.annotation.NonNull;
import java.util.ArrayList;

public class GameVocabulary {

    private static final String TAG = "GAME_VOCABULARY";
    private String initialWord;
    private ArrayList<FoundedWord> playersVocabulary = new ArrayList<>();
    private ArrayList<FoundedWord> opponentsVocabulary = new ArrayList<>();
    private Coordinator coordinator;

    public enum WordCheckResult {
        NOT_IN_THE_DICTIONARY,
        ALREADY_FOUNDED,
        NEW_FOUND_WORD
    }

    public GameVocabulary(){
    }

    public GameVocabulary(Coordinator coordinator){
        this.coordinator = coordinator;
    }

    public WordCheckResult checkWord(String word){
        if(Dictionary.checkIfWordIsInDictionary(word)){
            return WordCheckResult.NOT_IN_THE_DICTIONARY;
        }
        for(FoundedWord foundedWord : opponentsVocabulary){
            if(foundedWord.getWord().equals(word)){
                return WordCheckResult.ALREADY_FOUNDED;
            }
        }
        for(FoundedWord foundedWord : playersVocabulary){
            if(foundedWord.getWord().equals(word)){
                return WordCheckResult.ALREADY_FOUNDED;
            }
        }
        return WordCheckResult.NEW_FOUND_WORD;
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

    public Coordinator getCoordinator() {
        return coordinator;
    }

    public void setCoordinator(Coordinator coordinator) {
        this.coordinator = coordinator;
    }
}