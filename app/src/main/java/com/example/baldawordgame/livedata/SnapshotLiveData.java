package com.example.baldawordgame.livedata;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class SnapshotLiveData<T> extends LiveData<T> {

    public static final String TAG = "SnapshotLiveData";

    protected final Class<T> tClass;
    protected final DatabaseReference ref;

    protected ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            T item = snapshot.getValue(tClass);
            setReceivedValueInLiveData(item);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    public SnapshotLiveData(DatabaseReference ref, Class<T> tClass) {
        this.ref = ref;
        this.tClass = tClass;
    }

    protected void setReceivedValueInLiveData(T value) {
        Log.d(TAG, "NEW VALUE IS: " + value);
        setValue(value);
    }

    @Override
    public String toString() {
        return "SnapshotLiveData{" +
                "tClass=" + tClass +
                ", ref=" + ref +
                ", valueEventListener=" + valueEventListener +
                '}';
    }

    @Override
    protected void onActive() {
        Log.d(TAG, "onActive");
        ref.addValueEventListener(valueEventListener);
    }

    @Override
    protected void onInactive() {
        Log.d(TAG, "onInactive");
        ref.removeEventListener(valueEventListener);
    }
}