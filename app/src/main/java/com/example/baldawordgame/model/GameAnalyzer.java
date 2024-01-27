package com.example.baldawordgame.model;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.databinding.ObservableArrayList;

import java.util.ArrayList;

public class GameAnalyzer {

    public static final String TAG = "GameAnalyzer";

    private static int placedCounter = 0;
    private static ArrayList<String> approvedWords = new ArrayList<>();
    private static ArrayList<Combination> combinations = new ArrayList<>();

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void checkIfThereAreAvailableTurns(ArrayList<LetterCell> letterCells,
                                              ObservableArrayList<FoundWord> foundWords) {

        for (String word : Dictionary.getDictionaryArrayList()) {
            if (!GameVocabulary.checkIfVocabularyContainsWord(word, foundWords)) {
                searchLetter(word.trim(), letterCells);
            }
        }
        if(!approvedWords.isEmpty()){
            Log.d(TAG, "number of available: " + approvedWords);
            Log.d(TAG, "words: " + Dictionary.getDictionaryArrayList().size());
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private static void searchLetter(String word, ArrayList<LetterCell> letterCells){
        ArrayList<LetterCell> storage = new ArrayList<>();
        ArrayList<LetterCell> visited = new ArrayList<>();

        main: for(LetterCell letterCell : letterCells){
            if (!letterCell.getLetter().equals(LetterCell.NO_LETTER_PLUG) && word.startsWith(letterCell.getLetter())) {
                if (inner(word, 1, letterCell, letterCells, storage, visited)) {
                    storage.add(letterCell);
                    break;
                }
                visited.clear();
                placedCounter = 0;
            } else if (letterCell.getLetter().equals(LetterCell.NO_LETTER_PLUG)) {
                LetterCell substitution = new LetterCell(letterCell.getColumnIndex(), letterCell.getRowIndex());
                substitution.setLetter(String.valueOf(word.charAt(0)));
                substitution.setState(LetterCell.SUBSTITUTED_STATE);
                placedCounter = +1;
                if (inner(word, 1, substitution, letterCells, storage, visited)) {
                    storage.add(substitution);
                    break;
                }
                visited.clear();
                placedCounter = 0;
            }
        }
        StringBuilder builder = new StringBuilder(storage.size());
        Combination combination = new Combination();
        for (int i = storage.size() - 1; i >= 0; i--) {
            combination.getCombination().add(storage.get(i));
            builder.append(storage.get(i).getLetter());

        }
        if (builder.toString().equals(word)) {
            int counter = 0;
            for (int i = 0; i < combination.getCombination().size(); i++) {
                LetterCell lt = combination.getCombination().get(i);
                if (lt.getState().equals(LetterCell.SUBSTITUTED_STATE)) {
                    counter = counter + 1;
                }
            }
            if (counter == 1) {
                if (approvedWords.contains(builder.toString())) {
                    approvedWords.remove(builder.toString());
                    combinations.removeIf(i -> i.getContainedWord().equals(builder.toString()));
                }
                approvedWords.add(builder.toString());
                combination.setContainedWord(builder.toString());
                combinations.add(combination);
            }
        }
        visited.clear();

    }

    private static boolean inner(String word, int index, LetterCell prvLt,
                          ArrayList<LetterCell> letterCells,
                          ArrayList<LetterCell> storage,
                          ArrayList<LetterCell> visited) {

        visited.add(prvLt);
        if (index <= word.length() - 1) {
            for (LetterCell ltBox : letterCells) {
                if (!visited.contains(ltBox)) {
                    if (ltBox.getLetter().equals(String.valueOf(word.charAt(index))) && !ltBox.getLetter().equals(LetterCell.NO_LETTER_PLUG)) {
                        if (GameBoard.checkIfOneLetterIsCloseToAnother(prvLt, ltBox)) {
                            if (inner(word, index + 1, ltBox, letterCells, storage, visited)) {
                                storage.add(ltBox);
                                return true;
                            }
                        }
                    } else if (placedCounter == 0) {
                        if (ltBox.getLetter().equals(LetterCell.NO_LETTER_PLUG)) {
                            if (GameBoard.checkIfOneLetterIsCloseToAnother(prvLt, ltBox)) {
                                placedCounter += 1;
                                LetterCell substitution = new LetterCell(ltBox.getColumnIndex(), ltBox.getRowIndex());
                                substitution.setLetter(String.valueOf(word.charAt(index)));
                                substitution.setState(LetterCell.SUBSTITUTED_STATE);
                                if (inner(word, index + 1, substitution, letterCells, storage, visited)) {
                                    storage.add(substitution);
                                    return true;
                                }
                                placedCounter = 0;
                            }
                        }
                    }
                }
            }
        } else if (index >= word.length() - 1) {
            return true;
        }

        return false;
    }

    private static class Combination {

        private ArrayList<LetterCell> combination = new ArrayList<>();
        private String containedWord;

        public ArrayList<LetterCell> getCombination() {
            return combination;
        }

        public LetterCell getSubstitutedLetter() {
            for (LetterCell letter : combination) {
                if (letter.getState().equals(LetterCell.SUBSTITUTED_STATE)) {
                    return letter;
                }
            }
            return null;
        }

        public void setCombination(ArrayList<LetterCell> combination) {
            this.combination = combination;
        }

        public String getContainedWord() {
            return containedWord;
        }

        public void setContainedWord(String containedWord) {
            this.containedWord = containedWord;
        }
    }

}
