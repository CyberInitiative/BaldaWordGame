package com.example.baldawordgame.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.baldawordgame.R;

public class SkipTurnDialogFragment extends DialogFragment {

    public static final String TAG = "SkipTurnDialog";

    private Button yesAnswerButton, noAnswerButton;

    public interface SkipTurnDialogAnswerListener {
        void onSkipTurnYesAnswer(DialogFragment dialog);

        void onSkipTurnNoAnswer(DialogFragment dialog);
    }

    private SkipTurnDialogAnswerListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_skip_turn_dialog, null);

        yesAnswerButton = view.findViewById(R.id.yesAnswerButton);
        noAnswerButton = view.findViewById(R.id.noAnswerButton);

        noAnswerButton.setOnClickListener(click -> {
            listener.onSkipTurnNoAnswer(SkipTurnDialogFragment.this);
            dismiss();
        });

        yesAnswerButton.setOnClickListener(click -> {
            listener.onSkipTurnYesAnswer(SkipTurnDialogFragment.this);
            dismiss();
        });

        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (SkipTurnDialogAnswerListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.getClass().toString()
                    + " must implement SkipTurnDialog.SkipTurnDialogAnswerListener interface");
        }
    }

}
