package com.example.baldawordgame.view_adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableArrayList;
import androidx.recyclerview.widget.RecyclerView;
;
import com.example.baldawordgame.model.LetterCell;
import com.example.baldawordgame.R;

public class ShowPanelAdapter extends RecyclerView.Adapter<ShowPanelAdapter.ViewHolder> {

    private ObservableArrayList<LetterCell> listOfLetterCells;

    public ShowPanelAdapter(ObservableArrayList<LetterCell> listOfLetterCells) {
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
        LetterCell letterCell = listOfLetterCells.get(position);
        holder.selectedLetter.setText(letterCell.getLetter());
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
