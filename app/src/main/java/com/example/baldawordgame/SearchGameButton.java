package com.example.baldawordgame;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class SearchGameButton extends androidx.appcompat.widget.AppCompatButton {

    private boolean active = false;

    public SearchGameButton(@NonNull Context context) {
        super(context);
        this.setBackground(ContextCompat.getDrawable(this.getContext(), R.drawable.search_button_drawable));
        this.setText(getContext().getResources().getString(R.string.button_game_room_creation_proceed_not_active));
    }

    public SearchGameButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.setBackground(ContextCompat.getDrawable(this.getContext(), R.drawable.search_button_drawable));
        this.setText(getContext().getResources().getString(R.string.button_game_room_creation_proceed_not_active));
    }

    public void changeState(){
        active = !active;
        if(!active){
            this.setText(getContext().getResources().getString(R.string.button_game_room_creation_proceed_not_active));
            this.setTextColor(getContext().getResources().getColor(R.color.black));
        }else if(active){
            this.setText(getContext().getResources().getString(R.string.button_game_room_creation_proceed_active));
            this.setTextColor(getContext().getResources().getColor(R.color.golden_rice));
        }
        refreshDrawableState();
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 2);
        if (!active) {
            mergeDrawableStates(drawableState, NOT_ACTIVE);
        }
        else if(active){
            mergeDrawableStates(drawableState, ACTIVE);
        }
        return drawableState;
    }

    private static final int[] ACTIVE = {R.attr.active};
    private static final int[] NOT_ACTIVE = {R.attr.not_active};
}
