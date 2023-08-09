package com.example.baldawordgame;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

public class LetterCellCareTaker {
    private HashMap<DatabaseReference, LinkedList<LetterCellMemento>> refToMementos = new HashMap<>();
    private static final String TAG = "LetterCellCareTaker";

    public void add(DatabaseReference ref, LetterCellMemento memento) {
        LinkedList<LetterCellMemento> mementos = refToMementos.get(ref);
        if (mementos == null) {
            mementos = new LinkedList<>();
            refToMementos.put(ref, mementos);
        }
        Log.d(TAG, "addLast() memento: " + memento);
        mementos.addLast(memento);
    }

    public LetterCellMemento getLastMemento(@NonNull DatabaseReference ref) {
        if (refToMementos.containsKey(ref)) {
            LinkedList<LetterCellMemento> mementos = refToMementos.get(ref);
            if (mementos != null) {
                LetterCellMemento memento = mementos.getLast();
                Log.d(TAG, "getLast() memento: " + memento);
                mementos.removeLast();
                return memento;
            }
        }
        return null;
    }
}
