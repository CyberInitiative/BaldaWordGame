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

public class SurrenderDialogFragment extends DialogFragment {

    public static final String TAG = "SurrenderDialogFragment";

    private Button yesAnswerButton, noAnswerButton;

    public interface SurrenderDialogAnswerListener {
        public void onSurrenderYesAnswer(DialogFragment dialog);

        public void onSurrenderNoAnswer(DialogFragment dialog);
    }

    private SurrenderDialogAnswerListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_surrender_dialog, null);

        yesAnswerButton = view.findViewById(R.id.yesAnswerButton);
        noAnswerButton = view.findViewById(R.id.noAnswerButton);

        noAnswerButton.setOnClickListener(click -> {
            listener.onSurrenderNoAnswer(SurrenderDialogFragment.this);
            dismiss();
        });

        yesAnswerButton.setOnClickListener(click -> {
            listener.onSurrenderYesAnswer(SurrenderDialogFragment.this);
            dismiss();
        });

        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (SurrenderDialogFragment.SurrenderDialogAnswerListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.getClass().toString()
                    + " must implement SurrenderDialogFragment.SurrenderDialogAnswerListener");
        }
    }

}