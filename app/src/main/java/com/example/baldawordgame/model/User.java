package com.example.baldawordgame.model;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.Exclude;

public class User {
    private String email;
    private String username;
    private String gameRoomKey;

    public User() {
    }

    public User(String email, String username) {
        this.email = email;
        this.username = username;
    }

    //region GETTERS AND SETTERS
    @Exclude
    public static String getPlayerUid(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            return user.getUid();
        }
        return null;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getGameRoomKey() {
        return gameRoomKey;
    }

    public void setGameRoomKey(String gameRoomKey) {
        this.gameRoomKey = gameRoomKey;
    }

    //endregion


    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", gameRoomKey='" + gameRoomKey + '\'' +
                '}';
    }
}
