package com.example.baldawordgame;

import android.content.Intent;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.baldawordgame.model.GameRoom;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class GameRoomAdapter extends RecyclerView.Adapter<GameRoomAdapter.ViewHolder> {

    private ArrayList<Pair<DatabaseReference, GameRoom>> listOfRefToGameRoomPairs;

    public GameRoomAdapter(ArrayList<Pair<DatabaseReference, GameRoom>> listOfRefToGameRoomPairs) {
        this.listOfRefToGameRoomPairs = listOfRefToGameRoomPairs;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.games_recycler_view_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Pair<DatabaseReference, GameRoom> refToGameRoom = listOfRefToGameRoomPairs.get(position);
        GameRoom gameRoom = refToGameRoom.second;
        if (gameRoom.getGameBoardSize() == 3) {
            holder.gameGridSizeTextView.setText(holder.itemView.getResources().getString(R.string.three_on_three_btn_text));
        } else if (gameRoom.getGameBoardSize() == 5) {
            holder.gameGridSizeTextView.setText(holder.itemView.getResources().getString(R.string.five_on_five_btn_text));
        } else {
            holder.gameGridSizeTextView.setText(holder.itemView.getResources().getString(R.string.seven_on_seven_btn_text));
        }

        if (gameRoom.getTurnDuration() == (30 * 1000)) {
            holder.turnTimeTextView.setText(holder.itemView.getResources().getString(R.string.thirty_seconds_radio_text));
        } else if (gameRoom.getTurnDuration() == (60 * 1000)) {
            holder.turnTimeTextView.setText(holder.itemView.getResources().getString(R.string.one_minute_radio_text));
        } else {
            holder.turnTimeTextView.setText(holder.itemView.getResources().getString(R.string.two_minutes_radio_text));
        }

        if (gameRoom.getHostUID().length() >= 10) {
            holder.opponentNameTextView.setText(String.valueOf(gameRoom.getHostUID().charAt(0)));
        } else {
            holder.opponentNameTextView.setText(gameRoom.getHostUID());
        }
    }

    @Override
    public int getItemCount() {
        return listOfRefToGameRoomPairs == null ? 0 : listOfRefToGameRoomPairs.size();
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

            joinRoomButton.setOnClickListener(click -> {
                Intent intent = new Intent(itemView.getContext(), GameActivity.class);
                Pair<DatabaseReference, GameRoom> pairInPos = listOfRefToGameRoomPairs.get(getAdapterPosition());

                DatabaseReference currentGameRef = pairInPos.first;
                GameRoom gameRoom = pairInPos.second;

                gameRoom.setGuestUID(User.getPlayerUid());

                currentGameRef.child(GameRoom.GUEST_UID_PATH).setValue(User.getPlayerUid());
                currentGameRef.child(GameRoom.GAME_ROOM_STATUS_PATH).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String snapshotData = snapshot.getValue(String.class);
                        if (snapshotData != null) {
                            if (snapshotData.equals(GameRoom.FULL_GAME_ROOM)) {
                                intent.putExtra(GameActivity.CURRENT_GAME_ROOM_KEY, currentGameRef.getKey());
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
