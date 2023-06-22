package com.example.baldawordgame;

import java.util.List;

public class User {
    private String email;
    private String username;
    private int gamesPlayed, wins, score = 0;

    public User() {
    }

    public User(String email, String username) {
        this.email = email;
        this.username = username;
    }

    public User(String email, String username, int gamesPlayed, int wins) {
        this.email = email;
        this.username = username;
        this.gamesPlayed = gamesPlayed;
        this.wins = wins;
    }

    public void increaseGamesPlayedCounter(){
        gamesPlayed=+1;
    }

    public void increaseGamesPlayedAndWinsCounter(){
        gamesPlayed=+1;
        wins=+1;
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

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public int getWins() {
        return wins;
    }

    public int getScore() {
        return score;
    }

    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", gamesPlayed=" + gamesPlayed +
                ", wins=" + wins +
                ", score=" + score +
                '}';
    }
}
