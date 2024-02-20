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

public class EnemySurrendersAndLeavesDialogFragment extends DialogFragment {

    public static final String TAG = "EnemySurrenderedAndLeftDialogFragment";

    private Button leaveButton;

    public interface EnemySurrendersAndLeavesAnswerListener {
        void onEnemySurrendersAndLeavesYesAnswer(DialogFragment dialog);
    }

    private EnemySurrendersAndLeavesAnswerListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_enemy_surrenders_and_leaves_dialog, null);

        leaveButton = view.findViewById(R.id.leaveButton);

        leaveButton.setOnClickListener(click -> {
            listener.onEnemySurrendersAndLeavesYesAnswer(EnemySurrendersAndLeavesDialogFragment.this);
            dismiss();
        });

        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (EnemySurrendersAndLeavesDialogFragment.EnemySurrendersAndLeavesAnswerListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.getClass().toString()
                    + " must implement EnemySurrendersAndLeavesDialogFragment.EnemySurrendersAndLeavesAnswerListener");
        }
    }

}
