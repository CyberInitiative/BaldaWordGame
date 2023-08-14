package com.example.baldawordgame;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class GameViewModelFactory implements ViewModelProvider.Factory {
    private String gameRoomKey;

    public GameViewModelFactory(String gameRoomKey) {
        this.gameRoomKey = gameRoomKey;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        GameViewModel gameViewModel = new GameViewModel(gameRoomKey);
        return (T) gameViewModel;
    }
}
