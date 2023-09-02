package com.example.baldawordgame.model;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.TimerTask;

public class Timer {

    private static final String TAG = "Timer";

    //region PATH STRINGS
    public static final String TIMERS_PATH = "timers";
    public static final String TURN_STARTED_AT_PATH = "turnStartedAt";
    //endregion

    public static final DatabaseReference TIMERS_REF = FirebaseDatabase.getInstance().getReference().child(TIMERS_PATH);
    public static final DatabaseReference SERVER_TIME_OFFSET_REF = FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset");
    private final DatabaseReference currentTimerRef;
    private final DatabaseReference turnStartedAtRef;

    private final int turnDuration;

    private long serverTimeOffset = 0;
    private long turnStartedAt = 0;

    private HashMap<DatabaseReference, ValueEventListener> refToListener = new HashMap<>();

    public Timer(@NonNull String gameRoomKey, int turnDuration) {
        this.turnDuration = turnDuration;
        currentTimerRef = TIMERS_REF.child(gameRoomKey);
        turnStartedAtRef = currentTimerRef.child(TURN_STARTED_AT_PATH);
    }

    public LiveData<Long> getInterval() {
        MutableLiveData<Long> intervalLiveData = new MutableLiveData<>();

        ValueEventListener serverTimeOffsetListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long snapshotData = snapshot.getValue(Long.class);
                if (snapshotData != null) {
                    serverTimeOffset = snapshotData;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        ValueEventListener turnStartedAtListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long snapshotData = snapshot.getValue(Long.class);
                if (snapshotData != null) {
                    turnStartedAt = snapshotData;

                    java.util.Timer timer = new java.util.Timer();
                    TimerTask timerTask = new TimerTask() {
                        @Override
                        public void run() {
                            long currentTime = System.currentTimeMillis();
                            long amountToSubtract = currentTime + serverTimeOffset - turnStartedAt;

                            long timeLeft = (turnDuration * 1000L) - amountToSubtract;
                            if (timeLeft < 0) {
                                timer.cancel();
                                intervalLiveData.postValue(timeLeft);
                            } else {
                                intervalLiveData.postValue(timeLeft);
                            }
                        }
                    };
                    timer.scheduleAtFixedRate(timerTask, 0, 100);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        SERVER_TIME_OFFSET_REF.addValueEventListener(serverTimeOffsetListener);
        turnStartedAtRef.addValueEventListener(turnStartedAtListener);

        refToListener.put(SERVER_TIME_OFFSET_REF, serverTimeOffsetListener);
        refToListener.put(turnStartedAtRef, turnStartedAtListener);

        return intervalLiveData;
    }

    public void stopListeners(){
        if(refToListener.containsKey(SERVER_TIME_OFFSET_REF)){
            ValueEventListener serverTimeOffsetListener = refToListener.get(SERVER_TIME_OFFSET_REF);
            if(serverTimeOffsetListener != null) {
                SERVER_TIME_OFFSET_REF.removeEventListener(serverTimeOffsetListener);
            }
        }

        if(refToListener.containsKey(turnStartedAtRef)){
            ValueEventListener turnStartedAtListener = refToListener.get(turnStartedAtRef);
            if(turnStartedAtListener != null) {
                turnStartedAtRef.removeEventListener(turnStartedAtListener);
            }
        }
    }

    public Task<Void> writeTurnStartedAt() {
        return turnStartedAtRef.setValue(ServerValue.TIMESTAMP);
    }

    @Exclude
    public int getTurnDuration() {
        return turnDuration;
    }

    @Exclude
    public DatabaseReference getCurrentTimerRef() {
        return currentTimerRef;
    }

}

//    public FirebaseQueryLiveData getOffsetFirebaseQueryLiveData(){
//        return new FirebaseQueryLiveData(offsetRef);
//    }

//    public FirebaseQueryLiveData getTurnStartedAtFirebaseQueryLiveData(){
//        return new FirebaseQueryLiveData(turnStartedAtRef);
//    }


//    public void test() {
//        long offsetValue = 0;
//        DatabaseReference offsetRef = FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset");
//
//        offsetRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                offsetValue = snapshot.getValue(Long.class);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//
//
//
//
//
////        DatabaseReference turnStartedAtRef = currentTimerRef.child(TURN_STARTED_AT_PATH);
//
//        Task<DataSnapshot> getOffsetTask = offsetRef.get();
//        Task<DataSnapshot> getTurnStartedAt = currentTimerRef.child(TURN_STARTED_AT_PATH).get();
//
//        Tasks.whenAll(getOffsetTask, getOffsetTask).addOnCompleteListener(tasks -> {
//
//        });
//    }

//    DatabaseReference offsetRef = FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset");
//offsetRef.addValueEventListener(new
//
//    ValueEventListener() {
//        @Override
//        public void onDataChange (@NonNull DataSnapshot snapshot){
//            double offset = snapshot.getValue(Double.class);
//            double estimatedServerTimeMs = System.currentTimeMillis() + offset;
//        }
//
//        @Override
//        public void onCancelled (@NonNull DatabaseError error){
//            Log.w(TAG, "Listener was cancelled");
//        }
//    });


//    public static Task<Timer> createTimer(@NonNull String gameRoomKey) {
//        Timer timer = new Timer(gameRoomKey);
//        return TIMERS_REF.child(gameRoomKey).child(TURN_DURATION).get()
//                .continueWith(task -> {
//                    timer.turnDuration = task.getResult().getValue(String.class);
//                    return timer;
//                });
//    }
