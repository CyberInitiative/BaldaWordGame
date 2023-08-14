package com.example.baldawordgame;

import androidx.annotation.NonNull;

import com.example.baldawordgame.model.GameProcessData;
import com.example.baldawordgame.model.GameRoom;

public class Coordinator {

    private GameRoom gameRoom;
    private GameProcessData gameProcessData;
    private GameVocabulary gameVocabulary;
    private GameBoard gameBoard;
    private MessageReceiver messageReceiver;

    public interface MessageReceiver {
        void process(Message message);
    }
    public enum Message{}

    @NonNull public String requestGameRoomKey(){
        return gameRoom.getGameRoomKey();
    }

    public void checkCombinedWord(String word) {
        if(gameVocabulary.checkWord(word).equals(GameVocabulary.WordCheckResult.NEW_FOUND_WORD)){
            GameVocabularyAccessor.addWord(requestGameRoomKey(), word);
        }
    }

    public void subscribeReceiver(MessageReceiver messageReceiver){
        this.messageReceiver = messageReceiver;
    }

    public void unsubscribeReceiver(){
        messageReceiver = null;
    }

    public void sendMessage(Message message){
        messageReceiver.process(message);
    }

    //region GETTERS_AND_SETTERS
    public GameProcessData getGameProcessData() {
        return gameProcessData;
    }

    public void setGameProcessData(GameProcessData gameProcessData) {
        this.gameProcessData = gameProcessData;
    }

    public GameVocabulary getGameVocabulary() {
        return gameVocabulary;
    }

    public void setGameVocabulary(GameVocabulary gameVocabulary) {
        this.gameVocabulary = gameVocabulary;
    }

    public GameBoard getGameBoard() {
        return gameBoard;
    }

    public void setGameBoard(GameBoard gameBoard) {
        this.gameBoard = gameBoard;
    }

    public GameRoom getGameRoom() {
        return gameRoom;
    }

    public void setGameRoom(GameRoom gameRoom) {
        this.gameRoom = gameRoom;
    }

    //endregion
}