package com.example.baldawordgame.livedata;

import android.util.Log;

import androidx.lifecycle.LiveData;

import java.util.Timer;
import java.util.TimerTask;

public class TurnTimerLiveData extends LiveData<Long> {
    private static final String TAG = "TurnTimer Class";

    private final int turnDuration;
    private Timer timer;
    private boolean isTimerRunning = false;

    public TurnTimerLiveData(int turnDuration) {
        this.turnDuration = turnDuration;
    }

    public void stopTimer() {
        if (timer != null) {
            this.timer.cancel();
        }
    }

    public void startTimer(long turnStartedAt, long serverTimeOffset) {
        if(timer != null){
            timer.cancel();
        }
        timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                long amountToSubtract = currentTime + serverTimeOffset - turnStartedAt;

//                long turnMustStopAt = turnStartedAt + (2 * 60 * 1000);

                long timeLeft = (TurnTimerLiveData.this.turnDuration * 1000L) - amountToSubtract;

                Log.d(TAG, "TIME LEFT: " + timeLeft);
//                Log.d(TAG, "TURN MUST END AT: " + turnMustStopAt);
                Log.d(TAG, "CURRENT TIME: " + (currentTime + serverTimeOffset));

                if (timeLeft < 0) {
                    timer.cancel();
                }
                postValue(timeLeft);

            }
        };

        timer.scheduleAtFixedRate(timerTask, 0, 100);
    }

    @Override
    protected void onActive() {
        super.onActive();
    }

    @Override
    protected void onInactive() {
        super.onInactive();
    }
}