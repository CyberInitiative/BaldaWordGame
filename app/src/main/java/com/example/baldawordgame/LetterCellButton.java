package com.example.baldawordgame;

import android.content.Context;
import android.view.Gravity;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

public class LetterCellButton extends androidx.appcompat.widget.AppCompatButton implements LetterCell.Subscriber {

    private String buttonState;

    private static final int[] STATE_AVAILABLE_WITHOUT_LETTER = {R.attr.available_without_letter};
    private static final int[] STATE_UNAVAILABLE_WITHOUT_LETTER = {R.attr.unavailable_without_letter};
    private static final int[] STATE_WITH_LETTER = {R.attr.with_letter};
    private static final int[] STATE_PART_OF_COMBINATION = {R.attr.part_of_combination};
    private static final int[] STATE_INTENDED_LETTER = {R.attr.intended_letter};

    public LetterCellButton(@NonNull Context context) {
        super(context);
        this.setCursorVisible(false);
        this.setGravity(Gravity.CENTER);
        this.setBackground(ContextCompat.getDrawable(this.getContext(), R.drawable.letter_cell));
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 6);
        if (buttonState != null) {
            switch (buttonState) {
                case LetterCell.LETTER_CELL_UNAVAILABLE_WITHOUT_LETTER_STATE:
                    mergeDrawableStates(drawableState, STATE_UNAVAILABLE_WITHOUT_LETTER);
                    break;
                case LetterCell.LETTER_CELL_AVAILABLE_WITHOUT_LETTER_STATE:
                    mergeDrawableStates(drawableState, STATE_AVAILABLE_WITHOUT_LETTER);
                    break;
                case LetterCell.LETTER_CELL_WITH_LETTER_STATE:
                    mergeDrawableStates(drawableState, STATE_WITH_LETTER);
                    break;
                case LetterCell.LETTER_CELL_SELECTED_AS_PART_OF_COMBINATION_STATE:
                case LetterCell.LETTER_CELL_INTENDED_SELECTED_AS_PART_OF_COMBINATION_STATE:
                    mergeDrawableStates(drawableState, STATE_PART_OF_COMBINATION);
                    break;
                case LetterCell.LETTER_CELL_INTENDED_STATE:
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

    public String getButtonState() {
        return buttonState;
    }

    public void setButtonState(String buttonState) {
        this.buttonState = buttonState;
        refreshDrawableState();
    }

    @Override
    public void processUpdatedGameCellState(String cellState) {
        setButtonState(cellState);
    }

    @Override
    public void processUpdatedGameCellLetter(String letterInCell) {
        if (letterInCell.equals(LetterCell.NO_LETTER_PLUG)) {
            this.setText("");
        } else {
            this.setText(letterInCell);
        }
    }
}