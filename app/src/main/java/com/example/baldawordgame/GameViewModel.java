package com.example.baldawordgame;

import androidx.lifecycle.ViewModel;

public class GameViewModel extends ViewModel {

    private final DictionaryManager dictionaryManager  = new DictionaryManager();
    private GameRoom gameRoom;
    private GameCellsManager gameCellsManager;

    public GameViewModel() {
    }

    public GameRoom getGameRoom() {
        return gameRoom;
    }

    public void setGameRoom(GameRoom gameRoom) {
        this.gameRoom = gameRoom;
    }

    public GameCellsManager getGameCellsManager() {
        return gameCellsManager;
    }

    public void setGameCellsManager(GameCellsManager gameCellsManager) {
        this.gameCellsManager = gameCellsManager;
    }

    public DictionaryManager getDictionaryManager() {
        return dictionaryManager;
    }
}