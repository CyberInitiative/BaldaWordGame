package com.example.baldawordgame.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.baldawordgame.R;

public class DefaultGameEndDialogFragment extends DialogFragment {

    public static final String TAG = "DefaultGameEndDialogFragment";
    public static final String GAME_RESULT_KEY = "gameResultKey";

    public static final int PLAYER_LOSE_CODE = 1;
    public static final int PLAYER_WIN_CODE = 2;

    private Button yesAnswerButton, noAnswerButton;
    private TextView gameResultTextView;

    public interface RematchAnswerListener {
        void onRematchYesAnswer(DialogFragment dialog);

        void onRematchNoAnswer(DialogFragment dialog);
    }

    private RematchAnswerListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        Bundle bundle = getArguments();
        int resultCode = bundle.getInt(GAME_RESULT_KEY);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_default_dialog, null);

        yesAnswerButton = view.findViewById(R.id.yesAnswerButton);
        noAnswerButton = view.findViewById(R.id.noAnswerButton);
        gameResultTextView = view.findViewById(R.id.gameResultTextView);

        noAnswerButton.setOnClickListener(click -> {
//            listener.onSkipTurnNoAnswer(PlayerWinDialogFragment.this);
            dismiss();
        });

        yesAnswerButton.setOnClickListener(click -> {
//            listener.onSkipTurnYesAnswer(PlayerWinDialogFragment.this);
            dismiss();
        });

        if(resultCode == PLAYER_LOSE_CODE){
            gameResultTextView.setText(R.string.player_lose);
        }else if (resultCode == PLAYER_WIN_CODE){
            gameResultTextView.setText(R.string.player_win);
        }

        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (RematchAnswerListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.getClass().toString()
                    + " must implement DefaultGameEndDialogFragment.RematchAnswerListener");
        }
    }

}
