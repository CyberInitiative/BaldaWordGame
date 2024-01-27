package com.example.baldawordgame.model;

import androidx.annotation.NonNull;

import com.example.baldawordgame.livedata.NewValueSnapshotLiveData;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;

public class User {

    public static final String USERS_PATH = "users";
    public static final String JOINED_GAME_ROOM_KEY_PATH = "joinedGameRoomKey";
    public static final DatabaseReference USERS_REF = FirebaseDatabase.getInstance().getReference().child(USERS_PATH);

    private String email;
    private String username;
    private String joinedGameRoomKey;
//    private int userStatus;

    public static final String NO_JOINED_GAME_ROOM = "NO_JOINED_GAME_ROOM";

    public User() {
    }

    public User(String email, String username) {
        this.email = email;
        this.username = username;
        this.joinedGameRoomKey = NO_JOINED_GAME_ROOM;
    }

    public static Task<User> fetchUser(){
        return USERS_REF.child(fetchPlayerUID()).get()
                .continueWith(task -> {
                    return task.getResult().getValue(User.class);
                });
    }

    public static String fetchPlayerUID(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            return user.getUid();
        }
        return null;
    }

    public static Task<Void> writeJoinedGameRoomKey(@NonNull String joinedGameRoomKey){
        return USERS_REF.child(fetchPlayerUID()).child(JOINED_GAME_ROOM_KEY_PATH).setValue(joinedGameRoomKey);
    }

    public NewValueSnapshotLiveData<Integer> getJoinedGameRoomKeyLiveData(){
        return new NewValueSnapshotLiveData<>(USERS_REF.child(fetchPlayerUID()).child(JOINED_GAME_ROOM_KEY_PATH), Integer.class);
    }

    //region GETTERS AND SETTERS

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

    public String getJoinedGameRoomKey() {
        return joinedGameRoomKey;
    }

    public void setJoinedGameRoomKey(String joinedGameRoomKey) {
        this.joinedGameRoomKey = joinedGameRoomKey;
    }

//    public int getUserStatus() {
//        return userStatus;
//    }

//    public void setUserStatus(int userStatus) {
//        this.userStatus = userStatus;
//    }
    //endregion

    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", gameRoomKey='" + joinedGameRoomKey + '\'' +
                '}';
    }

    public static class UserStatus{
        public static final int OFFLINE = 1;
        public static final int ONLINE = 2;
        public static final int PLAYING = 3;
    }

}
