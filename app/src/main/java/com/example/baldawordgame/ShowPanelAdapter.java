package com.example.baldawordgame;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
;
import java.util.LinkedList;

public class ShowPanelAdapter extends RecyclerView.Adapter<ShowPanelAdapter.ViewHolder> {

    private LinkedList<GameCell> listOfLetterCells;

    public ShowPanelAdapter(LinkedList<GameCell> listOfLetterCells) {
        this.listOfLetterCells = listOfLetterCells;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.show_panel_recycler_view_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GameCell gameCell = listOfLetterCells.get(position);
        holder.selectedLetter.setText(gameCell.getLetterInCell());
    }

    @Override
    public int getItemCount() {
        return listOfLetterCells == null ? 0 : listOfLetterCells.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView selectedLetter;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            selectedLetter = itemView.findViewById(R.id.selectedLetter);
        }
    }
}
