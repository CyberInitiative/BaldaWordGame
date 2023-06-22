package com.example.baldawordgame;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.Exclude;

public class LetterCell extends androidx.appcompat.widget.AppCompatButton implements GameCell.Subscriber {

    private String state;

    private static final int[] STATE_AVAILABLE_WITHOUT_LETTER = {R.attr.available_without_letter};
    private static final int[] STATE_UNAVAILABLE_WITHOUT_LETTER = {R.attr.unavailable_without_letter};
    private static final int[] STATE_WITH_LETTER = {R.attr.with_letter};
    private static final int[] STATE_PART_OF_COMBINATION = {R.attr.part_of_combination};
    private static final int[] STATE_INTENDED_LETTER = {R.attr.intended_letter};

    public LetterCell(@NonNull Context context) {
        super(context);
        this.setBackground(ContextCompat.getDrawable(this.getContext(), R.drawable.letter_cell));
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 6);
        if(state != null) {
            switch (state) {
                case GameCell.LETTER_CELL_UNAVAILABLE_WITHOUT_LETTER_STATE:
                    mergeDrawableStates(drawableState, STATE_UNAVAILABLE_WITHOUT_LETTER);
                    break;
                case GameCell.LETTER_CELL_AVAILABLE_WITHOUT_LETTER_STATE:
                    mergeDrawableStates(drawableState, STATE_AVAILABLE_WITHOUT_LETTER);
                    break;
                case GameCell.LETTER_CELL_WITH_LETTER_STATE:
                    mergeDrawableStates(drawableState, STATE_WITH_LETTER);
                    break;
                case GameCell.LETTER_CELL_SELECTED_AS_PART_OF_COMBINATION_STATE:
                case GameCell.LETTER_CELL_INTENDED_SELECTED_AS_PART_OF_COMBINATION_STATE:
                    mergeDrawableStates(drawableState, STATE_PART_OF_COMBINATION);
                    break;
                case GameCell.LETTER_CELL_INTENDED_STATE:
                    mergeDrawableStates(drawableState, STATE_INTENDED_LETTER);
                    break;
            }
        }
        return drawableState;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
        refreshDrawableState();
    }

    @Override
    public void processUpdatedGameCellState(String cellState) {
        setState(cellState);
    }

    @Override
    public void processUpdatedGameCellLetter(String letterInCell) {
        this.setText(letterInCell);
    }
}
