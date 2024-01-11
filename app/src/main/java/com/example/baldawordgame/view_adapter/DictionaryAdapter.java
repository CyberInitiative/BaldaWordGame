package com.example.baldawordgame.view_adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.baldawordgame.R;
import com.example.baldawordgame.model.FoundedWord;

import java.util.ArrayList;

public class DictionaryAdapter extends RecyclerView.Adapter<DictionaryAdapter.ViewHolder> {

    private ArrayList<FoundedWord> listOfFoundedWords;

    public DictionaryAdapter(ArrayList<FoundedWord> listOfFoundedWords) {
        this.listOfFoundedWords = listOfFoundedWords;
    }

    @NonNull
    @Override
    public DictionaryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dictionary_recycler_view_item, parent, false);;
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DictionaryAdapter.ViewHolder holder, int position) {
        FoundedWord foundedWord = listOfFoundedWords.get(position);
        holder.textViewWordInDictionary.setText(foundedWord.getWord());
    }

    @Override
    public int getItemCount() {
        return listOfFoundedWords == null ? 0 : listOfFoundedWords.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewWordInDictionary;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewWordInDictionary = itemView.findViewById(R.id.textViewWordInDictionary);
        }
    }
}
