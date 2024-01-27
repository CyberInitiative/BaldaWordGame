package com.example.baldawordgame.livedata;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.example.baldawordgame.model.LetterCell;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class LetterCellLiveData extends LiveData<LetterCell> {
    private static final String TAG = "LetterCellLiveData";

    private class MyValueEventListener implements ValueEventListener {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            LetterCell letterCell = dataSnapshot.getValue(LetterCell.class);
            if (letterCell != null) {
                LetterCellLiveData.this.setValue(letterCell);
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.e(TAG, "Can't listen to query " + query, databaseError.toException());
        }
    }

    private final Query query;
    private final LetterCellLiveData.MyValueEventListener listener = new LetterCellLiveData.MyValueEventListener();

    public LetterCellLiveData(Query query) {
        this.query = query;
    }

    public LetterCellLiveData(DatabaseReference ref) {
        this.query = ref;
    }

    @Override
    protected void onActive() {
        Log.d(TAG, "onActive");
        query.addValueEventListener(listener);
    }

    @Override
    protected void onInactive() {
        Log.d(TAG, "onInactive");
        query.removeEventListener(listener);
    }

    @Override
    public String toString() {
        return "FirebaseLiveData{" +
                "query=" + query +
                ", listener=" + listener +
                '}';
    }

}