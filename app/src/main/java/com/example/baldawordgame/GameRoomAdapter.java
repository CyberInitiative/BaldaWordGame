package com.example.baldawordgame;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class GameRoomAdapter extends RecyclerView.Adapter<GameRoomAdapter.ViewHolder> {

    private ArrayList<GameRoom> listOfOpenGameRooms;
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    public GameRoomAdapter(ArrayList<GameRoom> data) {
        listOfOpenGameRooms = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.games_recycler_view_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GameRoom gameRoom = listOfOpenGameRooms.get(position);
        if (gameRoom.getGameGridSize() == 3) {
            holder.gameGridSizeTextView.setText(holder.itemView.getResources().getString(R.string.three_on_three_btn_text));
        } else if (gameRoom.getGameGridSize() == 5) {
            holder.gameGridSizeTextView.setText(holder.itemView.getResources().getString(R.string.five_on_five_btn_text));
        } else {
            holder.gameGridSizeTextView.setText(holder.itemView.getResources().getString(R.string.seven_on_seven_btn_text));
        }

        if (gameRoom.getTurnTimeInMillis() == (30 * 1000)) {
            holder.turnTimeTextView.setText(holder.itemView.getResources().getString(R.string.thirty_seconds_radio_text));
        } else if (gameRoom.getTurnTimeInMillis() == (60 * 1000)) {
            holder.turnTimeTextView.setText(holder.itemView.getResources().getString(R.string.one_minute_radio_text));
        } else {
            holder.turnTimeTextView.setText(holder.itemView.getResources().getString(R.string.two_minutes_radio_text));
        }

        if (gameRoom.getPlayerOneUID().length() >= 10) {
            holder.opponentNameTextView.setText(String.valueOf(gameRoom.getPlayerOneUID().charAt(0)));
        } else {
            holder.opponentNameTextView.setText(gameRoom.getPlayerOneUID());
        }
    }

    @Override
    public int getItemCount() {
        return listOfOpenGameRooms == null ? 0 : listOfOpenGameRooms.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView opponentNameTextView;
        public TextView gameGridSizeTextView;
        public TextView turnTimeTextView;
        public Button joinRoomButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            opponentNameTextView = itemView.findViewById(R.id.opponentNameTextView);
            gameGridSizeTextView = itemView.findViewById(R.id.gameGridSizeTextView);
            turnTimeTextView = itemView.findViewById(R.id.turnTimeTextView);
            joinRoomButton = itemView.findViewById(R.id.joinRoomButton);
            joinRoomButton.setOnClickListener(l -> {
                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                Intent intent = new Intent(itemView.getContext(), GameActivity.class);
                GameRoom gameRoom = listOfOpenGameRooms.get(getAdapterPosition());
                gameRoom.setPlayerTwoUID(firebaseAuth.getCurrentUser().getUid());
                databaseReference.child("openRooms").child(gameRoom.getGameRoomKey()).child("playerTwoUID").setValue(firebaseAuth.getCurrentUser().getUid());
                databaseReference.child("openRooms").child(gameRoom.getGameRoomKey()).child("gameRoomStatus").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String snapshotData = snapshot.getValue(String.class);
                        if (snapshotData != null) {
                            if (snapshotData.equals(GameRoom.GAME_IS_READY)) {
                                intent.putExtra(GameActivity.CURRENT_GAME_ROOM_KEY, gameRoom.getGameRoomKey());
                                itemView.getContext().startActivity(intent);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            });
        }
    }
}
