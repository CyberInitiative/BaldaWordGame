package com.example.baldawordgame;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class DictionaryManager {

    private final static String TAG = "DICTIONARY_MANAGER";

    private static ArrayList<String> dictionary = new ArrayList<>();
    private ArrayList<FoundedWord> playerFoundedWords = new ArrayList<>();
    private ArrayList<FoundedWord> opponentFoundedWords = new ArrayList<>();
    private ArrayList<String> listOfInitialWords = new ArrayList<>();
    private String gameRoomKey;
    private ChildEventListener foundedWordListener;

    public enum WordCheckResult {
        NOT_IN_THE_DICTIONARY,
        IN_FOUNDED_WORDS_DICTIONARY,
        NEW_FOUND_WORD
    }

    public DictionaryManager() {
    }

    public static Task<DataSnapshot> loadDictionaryFromFirebase() {
        Task<DataSnapshot> dictionaryTask = FirebaseDatabase.getInstance().getReference().child("dictionary").get();
        dictionaryTask.continueWith(new Continuation<DataSnapshot, Void>() {
            @Override
            public Void then(@NonNull Task<DataSnapshot> task) throws Exception {
                if (task.isSuccessful()) {
                    for (DataSnapshot dataSnapshot : task.getResult().getChildren()) {
                        String data = dataSnapshot.getValue(String.class);
                        if (data != null) {
                            dictionary.add(data);
                        }
                    }
                }
                return null;
            }
        });
        return dictionaryTask;
    }

    public void addFoundedWordsListener() {
        if (foundedWordListener == null) {
            foundedWordListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    FoundedWord foundedWord = snapshot.getValue(FoundedWord.class);
                    if (foundedWord != null) {
                        if (!foundedWord.getPlayerKey().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                            playerFoundedWords.add(foundedWord);
                        } else {
                            opponentFoundedWords.add(foundedWord);
                        }
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            };
            FirebaseDatabase.getInstance().getReference().child("foundedWords").child(gameRoomKey).addChildEventListener(foundedWordListener);
        }
    }

    public void removeFoundedWordsListener() {
        if (foundedWordListener != null) {
            foundedWordListener = null;
        }
    }

    public WordCheckResult confirmWord(@NonNull String word) {
        if (!dictionary.contains(word)) {
            return WordCheckResult.NOT_IN_THE_DICTIONARY;
        } else if (playerFoundedWords.contains(word)) {
            return WordCheckResult.IN_FOUNDED_WORDS_DICTIONARY;
        } else if (dictionary.contains(word) && !playerFoundedWords.contains(word)) {
            FirebaseDatabase.getInstance().getReference().child("foundedWords").child(gameRoomKey)
                    .push().setValue(new FoundedWord(word, FirebaseAuth.getInstance().getCurrentUser().getUid()));
            return WordCheckResult.NEW_FOUND_WORD;
        }
        return null;
    }

    public String getRandomWord(int length) {
        if (!dictionary.isEmpty() && length != 0) {
            if (listOfInitialWords.isEmpty()) {
                for (String str : dictionary) {
                    if (str.length() == length) {
                        listOfInitialWords.add(str);
                    }
                }
            }
            int min = 0;
            int max = listOfInitialWords.size();
            int randomWordIndex = (int) (Math.random() * (max + min + 1) + min);
            String randomWord = listOfInitialWords.get(randomWordIndex);
            Log.d(TAG, "RANDOM WORD INDEX: " + randomWordIndex + ";" + " RANDOM WORD IS: " + randomWord + ";");
            return randomWord;
        }
        return null;
    }

    public static ArrayList<String> getDictionary() {
        return dictionary;
    }

    public ArrayList<FoundedWord> getPlayerFoundedWords() {
        return playerFoundedWords;
    }

    public ArrayList<FoundedWord> getOpponentFoundedWords() {
        return opponentFoundedWords;
    }
}
