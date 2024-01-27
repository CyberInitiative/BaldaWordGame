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

public class SurrenderAndLeaveDialogFragment extends DialogFragment {

    public static final String TAG = "SurrenderAndLeaveDialogFragment";

    private Button yesAnswerButton, noAnswerButton;

    public interface SurrenderAndLeaveDialogAnswerListener {
        void onSurrenderAndLeaveYesAnswer(DialogFragment dialog);

        void onSurrenderAndLeaveNoAnswer(DialogFragment dialog);
    }

    private SurrenderAndLeaveDialogAnswerListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_surrender_and_leave_dialog, null);

        yesAnswerButton = view.findViewById(R.id.yesAnswerButton);
        noAnswerButton = view.findViewById(R.id.noAnswerButton);

        noAnswerButton.setOnClickListener(click -> {
            listener.onSurrenderAndLeaveNoAnswer(SurrenderAndLeaveDialogFragment.this);
            dismiss();
        });

        yesAnswerButton.setOnClickListener(click -> {
            listener.onSurrenderAndLeaveYesAnswer(SurrenderAndLeaveDialogFragment.this);
            dismiss();
        });

        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (SurrenderAndLeaveDialogFragment.SurrenderAndLeaveDialogAnswerListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.getClass().toString()
                    + " must implement SurrenderAndLeaveDialogFragment.SurrenderAndLeaveDialogAnswerListener");
        }
    }
}
