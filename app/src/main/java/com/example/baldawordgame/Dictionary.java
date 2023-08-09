package com.example.baldawordgame;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Random;

public class Dictionary {
    private final static String TAG = "DICTIONARY";

    private static ArrayList<String> dictionaryArrayList = new ArrayList<>();

    //Подавление создания конструктора по умолчанию
    //для достижения неинстанцируемости
    private Dictionary(){
        throw new AssertionError();
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
                            dictionaryArrayList.add(data);
                        }
                    }
                }
                return null;
            }
        });
        return dictionaryTask;
    }

    public static String getRandomWordOfACertainLength(int wordLength) {
        if (!dictionaryArrayList.isEmpty() && wordLength != 0) {
            ArrayList<String> filteredList = new ArrayList<>();
            for (String word : dictionaryArrayList) {
                if (word.length() == wordLength) {
                    filteredList.add(word);
                }
            }
            Random random = new Random();
            String randomWord = filteredList.get(random.nextInt(filteredList.size()));
            Log.d(TAG, "RANDOM WORD IS: " + randomWord + ";");
            return randomWord;
        }
        return null;
    }

    public static ArrayList<String> getDictionaryArrayList() {
        return dictionaryArrayList;
    }

    public static boolean checkIfWordIsInDictionary(String word) {
        return dictionaryArrayList.contains(word);
    }

}
