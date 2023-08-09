package com.example.baldawordgame;

public class LetterCellMemento {

    private String state;
    private String letter;

    public LetterCellMemento(String state, String letter) {
        this.state = state;
        this.letter = letter;
    }

    public String getState() {
        return state;
    }

    public String getLetter(){
        return letter;
    }
    @Override
    public String toString() {
        return "LetterCellMemento: {" +
                "state='" + state + '\'' +
                ", letter='" + letter + '\'' +
                '}';
    }
}
