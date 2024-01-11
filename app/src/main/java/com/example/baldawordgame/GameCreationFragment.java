package com.example.baldawordgame;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.baldawordgame.model.GameRoom;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class GameCreationFragment extends Fragment {

    public static final String TAG = "GameCreationFragment";

    private GameRoom createdGameRoom;
    private DatabaseReference createdGameRoomReference;
    private ValueEventListener secondPlayerListener;
    private boolean opponentFounded = false;

    //region VIEWS
    private ToggleButton buttonGameCreationProceed;
    private TextView textViewGameGridFormatLabel;
    private TextView textViewGameTurnTimeLabel;
    private TextView textViewWaitingForOpponentLabel;

    private RadioGroup gameGridSizeRadioGroup, gameTimerRadioGroup;
    private RadioButton radioButtonThreeOnThree, radioButtonFiveOnFive, radioButtonSevenOnSeven;
    private RadioButton radioButtonTimerThirtySeconds, radioButtonTimerOnMinute, radioButtonTimerTwoMinutes;

    private ProgressBar progressBar;

    // Required empty public constructor
    public GameCreationFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_game_creation, container, false);

        viewsSettings(view);
        Log.d(TAG, "onCreateView() called;");
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart() called;");
    }

    @Override
    public void onStop() {
        super.onStop();
        if (buttonGameCreationProceed.isChecked()) {
            buttonGameCreationProceed.setChecked(false);
            gameSearchIsStop();
        }
        Log.d(TAG, "onStop() called;");
    }

    private void buttonGameCreationProceedClick() {
        buttonGameCreationProceed.setOnClickListener(event -> {
            if (buttonGameCreationProceed.isChecked()) {
                gameSearchStart();
            } else {
                gameSearchIsStop();
            }
        });
    }

    private void viewsSettings(@NonNull View view) {
        progressBar = view.findViewById(R.id.progressBar);

        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
        alphaAnimation.setDuration(600);
        alphaAnimation.setStartOffset(100);

        textViewGameGridFormatLabel = view.findViewById(R.id.textViewGameGridFormatLabel);
        textViewGameTurnTimeLabel = view.findViewById(R.id.textViewGameTurnTimeLabel);
        textViewWaitingForOpponentLabel = view.findViewById(R.id.textViewWaitingForOpponentLabel);

        buttonGameCreationProceed = view.findViewById(R.id.buttonGameCreationProceed);
        buttonGameCreationProceed.setTextOn("Остановить поиск");
        buttonGameCreationProceed.setTextOff("Начать поиск");
        buttonGameCreationProceed.setText("Начать поиск");

        radioButtonThreeOnThree = view.findViewById(R.id.radioButtonThreeOnThree);
        radioButtonFiveOnFive = view.findViewById(R.id.radioButtonFiveOnFive);
        radioButtonSevenOnSeven = view.findViewById(R.id.radioButtonSevenOnSeven);
        radioButtonFiveOnFive.setChecked(true);

        radioButtonTimerThirtySeconds = view.findViewById(R.id.radioButtonTimerThirtySeconds);
        radioButtonTimerOnMinute = view.findViewById(R.id.radioButtonTimerOnMinute);
        radioButtonTimerTwoMinutes = view.findViewById(R.id.radioButtonTimerTwoMinutes);
        radioButtonTimerTwoMinutes.setChecked(true);

        gameGridSizeRadioGroup = view.findViewById(R.id.gameGridSizeRadioGroup);
        gameTimerRadioGroup = view.findViewById(R.id.gameTimerRadioGroup);
        buttonGameCreationProceedClick();
    }

    private void gameSearchStart() {
        gameGridSizeRadioGroup.setVisibility(View.GONE);
        gameTimerRadioGroup.setVisibility(View.GONE);
        textViewGameGridFormatLabel.setVisibility(View.GONE);
        textViewGameTurnTimeLabel.setVisibility(View.GONE);

        textViewWaitingForOpponentLabel.setVisibility(buttonGameCreationProceed.isChecked() ? View.VISIBLE : View.INVISIBLE);

        progressBar.animate()
                .alpha(1f)
                .setDuration(600)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        progressBar.setVisibility(View.VISIBLE);
                    }
                });

        textViewWaitingForOpponentLabel.animate()
                .alpha(1f)
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        textViewWaitingForOpponentLabel.setVisibility(View.VISIBLE);
                    }
                });

        createdGameRoomReference = GameRoom.GAME_ROOMS_REF.push();
        createdGameRoom = new GameRoom(createdGameRoomReference.getKey(), getSelectedGridSize(), getSelectedTurnTime());

        createdGameRoomReference.setValue(createdGameRoom);

        secondPlayerListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String dataFromSnapshot = snapshot.getValue(String.class);
                if (dataFromSnapshot != null) {
                    createdGameRoomReference.removeEventListener(secondPlayerListener);
                    opponentFounded = true;
                    createdGameRoomReference
                            .child(GameRoom.GAME_ROOM_STATUS_PATH)
                            .setValue(GameRoom.RoomStatus.FULL_GAME_ROOM)
                            .addOnCompleteListener(task -> {
                        Intent gameActivityIntent = new Intent(getActivity(), GameActivity.class);
                        gameActivityIntent.putExtra(GameActivity.CURRENT_GAME_ROOM_KEY, createdGameRoomReference.getKey());
                        startActivity(gameActivityIntent);
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        createdGameRoomReference.child(GameRoom
                .SECOND_PLAYER_UID_PATH)
                .addValueEventListener(secondPlayerListener);
    }

//    private void dataWriting(){
//        createdGameRoom = GameRoom.createGameRoom();
//        createdGameRoomRef = GameRoom.GAME_ROOMS_REF.push();
//
//        Task<Void> writeGameTask = createdGameRoomRef.setValue(createdGameRoom);
//
//        String dataKey = createdGameRoomRef.getKey();
//    }

    private void gameSearchIsStop() {
        progressBar.animate()
                .alpha(1f)
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });

        textViewWaitingForOpponentLabel.animate()
                .alpha(1f)
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        textViewWaitingForOpponentLabel.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        gameGridSizeRadioGroup.setVisibility(View.VISIBLE);
                        gameTimerRadioGroup.setVisibility(View.VISIBLE);
                        textViewGameGridFormatLabel.setVisibility(View.VISIBLE);
                        textViewGameTurnTimeLabel.setVisibility(View.VISIBLE);
                    }
                });
        createdGameRoomReference.removeEventListener(secondPlayerListener);
        secondPlayerListener = null;
        if (!opponentFounded) {
            createdGameRoomReference.removeValue();
        }
    }

    private int getSelectedGridSize() {
        if (radioButtonThreeOnThree.isChecked()) {
            return 3;
        } else if (radioButtonFiveOnFive.isChecked()) {
            return 5;
        }
        return 7;
    }

    private int getSelectedTurnTime() {
        if (radioButtonTimerThirtySeconds.isChecked()) {
            return (30);
        } else if (radioButtonTimerOnMinute.isChecked()) {
            return (60);
        }
        return (10);
//        return (2 * 60);
    }

}