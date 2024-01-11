package com.example.baldawordgame.livedata;

import com.google.firebase.database.DatabaseReference;

public class NewValueSnapshotLiveData<T> extends SnapshotLiveData<T> {

    public NewValueSnapshotLiveData(DatabaseReference ref, Class<T> tClass) {
        super(ref, tClass);
    }

    @Override
    protected void setReceivedValueInLiveData(T value) {
        if (getValue() == null || !getValue().equals(value)) {
            setValue(value);
        }
    }

}