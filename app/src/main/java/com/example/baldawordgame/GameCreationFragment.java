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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class GameCreationFragment extends Fragment implements GameRoom.GameRoomObserver {
    public static final String TAG = "GAME_CREATION_FRAGMENT";

    private GameRoom currentGameRoom;

    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference openRoomsReference = databaseReference.child("openRooms");
    private DatabaseReference currentGameRoomRef;

    private boolean searchingIsStarted = false;

    private LinearLayout main;

    private AlphaAnimation alphaAnimation;

    private TextView textViewGameGridFormatLabel;
    private TextView textViewGameTurnTimeLabel;
    private TextView textViewWaitingForOpponentLabel;

    private SearchGameButton buttonGameCreationProceed;

    private RadioGroup gameGridSizeRadioGroup;
    private RadioButton radioButtonThreeOnThree;
    private RadioButton radioButtonFiveOnFive;
    private RadioButton radioButtonSevenOnSeven;

    private RadioGroup gameTimerRadioGroup;
    private RadioButton radioButtonTimerThirtySeconds;
    private RadioButton radioButtonTimerOnMinute;
    private RadioButton radioButtonTimerTwoMinutes;

    private ProgressBar progressBar;

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

    @Override
    public void update() {
        Intent gameActivityIntent = new Intent(getActivity(), GameActivity.class);
        gameActivityIntent.putExtra(GameActivity.CURRENT_GAME_ROOM_KEY, currentGameRoomRef.getKey());
        startActivity(gameActivityIntent);
    }

    private int getCheckedGridSize(){
        if (radioButtonThreeOnThree.isChecked()) {
            return  3;
        } else if (radioButtonFiveOnFive.isChecked()) {
            return  5;
        }
        return  7;
    }

    private long getCheckedTurnTime(){
        if (radioButtonTimerThirtySeconds.isChecked()) {
            return  (30 * 1000);
        } else if (radioButtonTimerOnMinute.isChecked()) {
            return (60 * 1000);
        }
        return (2 * 60 * 1000);
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

        buttonGameCreationProceed.setOnClickListener(event -> {
//            GameCell gameCell = new GameCell(1 ,2);
//            GameCell gameCell = new GameCell();
////            FirebaseDatabase.getInstance().getReference().child("cell").push().setValue(gameCell);
//            FirebaseDatabase.getInstance().getReference().child("cell").child("-NVozy4DmeMOPYAefAF-")
//                    .child("letterInCell").addValueEventListener(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//                            String string = snapshot.getValue(String.class);
//                            if(string != null){
//                                gameCell.getCellStateObservable().set(string);
//                                Log.d(TAG, String.valueOf(gameCell));
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError error) {
//
//                        }
//                    });

            buttonGameCreationProceed.changeState();
            if (!searchingIsStarted) {
                gameGridSizeRadioGroup.setVisibility(View.GONE);
                gameTimerRadioGroup.setVisibility(View.GONE);
                textViewGameGridFormatLabel.setVisibility(View.GONE);
                textViewGameTurnTimeLabel.setVisibility(View.GONE);
            }
            textViewWaitingForOpponentLabel.setVisibility(searchingIsStarted ? View.VISIBLE : View.INVISIBLE);
            if (!searchingIsStarted) {
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
            }

            if (searchingIsStarted) {
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
            }

            searchingIsStarted = !searchingIsStarted;
            if (searchingIsStarted) {
                Log.d(TAG, "buttonGameCreationProceed pressed; Game is searching");
                currentGameRoomRef = openRoomsReference.push();
                currentGameRoom = GameRoom.gameCreationStage(this, currentGameRoomRef.getKey(),
                        FirebaseAuth.getInstance().getUid(), getCheckedGridSize(), getCheckedTurnTime());
                currentGameRoomRef.setValue(currentGameRoom);
            } else {
                Log.d(TAG, "buttonGameCreationProceed pressed; Game is not searching");
            }
        });

    }
}