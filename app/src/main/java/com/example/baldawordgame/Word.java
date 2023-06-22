package com.example.baldawordgame;

public class Word {
    private String word;
    private String[] meaningsArr;

    public Word(String word, String... meanings) {
        this.word = word;
        meaningsArr = new String[meanings.length];
        for(int i = 0; i < meanings.length; i++){
            meaningsArr[i] = meanings[i];
        }
    }

    public String getWord() {
        return word;
    }

    public String[] getMeaningsArr() {
        return meaningsArr;
    }
}
