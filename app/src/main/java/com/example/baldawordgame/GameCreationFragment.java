package com.example.baldawordgame;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.baldawordgame.model.GameRoom;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class GameCreationFragment extends Fragment {
    public static final String TAG = "GAME_CREATION_FRAGMENT";

    private GameRoom currentGameRoom;

    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference openRoomsReference = databaseReference.child("openRooms");
    private DatabaseReference currentGameRoomRef;
    private ValueEventListener listener;

    private LinearLayout main;

    private ToggleButton buttonGameCreationProceed;

    private TextView textViewGameGridFormatLabel;
    private TextView textViewGameTurnTimeLabel;
    private TextView textViewWaitingForOpponentLabel;

    private RadioGroup gameGridSizeRadioGroup;
    private RadioButton radioButtonThreeOnThree;
    private RadioButton radioButtonFiveOnFive;
    private RadioButton radioButtonSevenOnSeven;

    private RadioGroup gameTimerRadioGroup;
    private RadioButton radioButtonTimerThirtySeconds;
    private RadioButton radioButtonTimerOnMinute;
    private RadioButton radioButtonTimerTwoMinutes;

    private ProgressBar progressBar;

    private AlphaAnimation alphaAnimation;
    private boolean opponentFounded = false;

    public GameCreationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_game_creation, container, false);

        init(view);
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

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() called;");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() called;");
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated() called;");
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach() called;");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach() called;");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() called;");
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

    private void init(View view) {
        main = view.findViewById(R.id.main);
        progressBar = view.findViewById(R.id.progressBar);

        alphaAnimation = new AlphaAnimation(0, 1);
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

        currentGameRoomRef = openRoomsReference.push();
        currentGameRoom = new GameRoom(currentGameRoomRef.getKey(), FirebaseAuth.getInstance().getUid(), getCheckedGridSize(), getCheckedTurnTime());
        currentGameRoomRef.setValue(currentGameRoom);

        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String dataFromSnapshot = snapshot.getValue(String.class);
                if (dataFromSnapshot != null) {
                    currentGameRoomRef.removeEventListener(listener);
                    opponentFounded = true;

                    Intent gameActivityIntent = new Intent(getActivity(), GameActivity.class);
                    gameActivityIntent.putExtra(GameActivity.CURRENT_GAME_ROOM_KEY, currentGameRoomRef.getKey());
                    startActivity(gameActivityIntent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        currentGameRoomRef.child("playerTwoUID").addValueEventListener(listener);
    }

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
        currentGameRoomRef.removeEventListener(listener);
        if (!opponentFounded) {
            currentGameRoomRef.removeValue();
        }
    }

    private int getCheckedGridSize() {
        if (radioButtonThreeOnThree.isChecked()) {
            return 3;
        } else if (radioButtonFiveOnFive.isChecked()) {
            return 5;
        }
        return 7;
    }

    private long getCheckedTurnTime() {
        if (radioButtonTimerThirtySeconds.isChecked()) {
            return (30 * 1000);
        } else if (radioButtonTimerOnMinute.isChecked()) {
            return (60 * 1000);
        }
        return (2 * 60 * 1000);
    }

}