package com.example.baldawordgame;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.baldawordgame.model.GameRoom;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class GameListFragment extends Fragment {

    public static final String TAG = "GameListFragment";

    private RecyclerView gamesRecyclerView;
    private GameRoomAdapter gameRoomAdapter;
    private ArrayList<GameRoom> gameRooms = new ArrayList<>();
    private HashMap<DatabaseReference, ValueEventListener> refToListener = new HashMap<>();

    private Query roomsQuery = GameRoom.GAME_ROOMS_REF
            .orderByChild("gameRoomStatus")
            .equalTo(GameRoom.OPEN_GAME_ROOM);

    private ChildEventListener openRoomsChildEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            GameRoom gameRoom = snapshot.getValue(GameRoom.class);
            gameRooms.add(0, gameRoom);
            gameRoomAdapter.notifyItemInserted(0);

            ValueEventListener gameRoomStatusListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String gameRoomStatus = snapshot.getValue(String.class);
                    if(gameRoomStatus != null){
                        if(gameRoomStatus.equals(GameRoom.FULL_GAME_ROOM)){
                            refToListener.remove(snapshot.getRef().child("gameRoomStatus"));
                            snapshot.getRef().child("gameRoomStatus").removeEventListener(this);
                            snapshot.getRef().removeValue();
                            gameRooms.remove(gameRoom);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            };
            refToListener.put(snapshot.getRef().child("gameRoomStatus"), gameRoomStatusListener);
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//            GameRoom gameRoom = snapshot.getValue(GameRoom.class);
//            Log.d(TAG, "TRIGGERED;" + gameRoom);
//            if (gameRoom != null && gameRoom.getGameRoomStatus().equals(GameRoom.FULL_GAME_ROOM)) {
//                if (gameRooms.contains(gameRoom)) {
//                    gameRoomAdapter.notifyItemRemoved(gameRooms.indexOf(gameRoom));
//                    gameRooms.remove(gameRoom);
//                }
//            }
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            GameRoom gameRoom = snapshot.getValue(GameRoom.class);
            if (gameRoom != null) {
                if (gameRooms.contains(gameRoom)) {
                    gameRoomAdapter.notifyItemRemoved(gameRooms.indexOf(gameRoom));
                    gameRooms.remove(gameRoom);
                }
            }
        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    public GameListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_game_list, container, false);
        viewsSettings(view);

        Log.d(TAG, "onCreateView() called;");
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated() called;");
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        roomsQuery.addChildEventListener(openRoomsChildEventListener);
    }

    @Override
    public void onDetach() {
        roomsQuery.removeEventListener(openRoomsChildEventListener);
        super.onDetach();
    }

    private void viewsSettings(View view) {
        gamesRecyclerView = view.findViewById(R.id.recyclerViewGames);
        gameRoomAdapter = new GameRoomAdapter(gameRooms);
        gamesRecyclerView.setAdapter(gameRoomAdapter);
        gamesRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this.getContext(), DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(this.getContext(), R.drawable.transparent_divider));
        gamesRecyclerView.addItemDecoration(dividerItemDecoration);
    }
}