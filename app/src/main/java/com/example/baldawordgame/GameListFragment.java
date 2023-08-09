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
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class GameListFragment extends Fragment {

    public static final String TAG = "GAME_LIST_FRAGMENT";

    private RecyclerView recyclerViewGames;
    private GameRoomAdapter gameRoomAdapter;
    private ArrayList<GameRoom> openRoomsArrayList = new ArrayList<>();

    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference openRoomsReference = databaseReference.child("openRooms");

    private ChildEventListener openRoomsChildEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            GameRoom gameRoom = snapshot.getValue(GameRoom.class);
            openRoomsArrayList.add(0, gameRoom);
            gameRoomAdapter.notifyItemInserted(0);
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            GameRoom gameRoom = snapshot.getValue(GameRoom.class);
            if (gameRoom != null) {
                for (int i = 0; i < openRoomsArrayList.size(); i++) {
                    if (openRoomsArrayList.get(i).getGameRoomKey().equals(gameRoom.getGameRoomKey())) {
                        int position = i;
                        openRoomsArrayList.remove(openRoomsArrayList.get(i));
                        if (gameRoomAdapter != null) {
                            gameRoomAdapter.notifyItemRemoved(position);
                            break;
                        }
                    }
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
        init(view);

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
        openRoomsReference.addChildEventListener(openRoomsChildEventListener);
        Log.d(TAG, "onAttach() called;");
    }

    @Override
    public void onDetach() {
        openRoomsReference.removeEventListener(openRoomsChildEventListener);
        super.onDetach();
        Log.d(TAG, "onDetach() called;");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() called;");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart() called;");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop() called;");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() called;");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() called;");
    }

    private void init(View view) {
        recyclerViewGames = view.findViewById(R.id.recyclerViewGames);
        gameRoomAdapter = new GameRoomAdapter(openRoomsArrayList);
        recyclerViewGames.setAdapter(gameRoomAdapter);
        recyclerViewGames.setLayoutManager(new LinearLayoutManager(this.getContext()));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this.getContext(), DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(this.getContext(), R.drawable.transparent_divider));
        recyclerViewGames.addItemDecoration(dividerItemDecoration);
    }
}