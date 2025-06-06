package com.haliltanriverdi.memoly.wordle.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.haliltanriverdi.memoly.R;
import com.haliltanriverdi.memoly.wordle.model.WordleCell;

import java.util.List;

public class WordleCellAdapter extends RecyclerView.Adapter<WordleCellAdapter.CellViewHolder> {

    private List<WordleCell> cells;
    private Context context;

    public WordleCellAdapter(Context context, List<WordleCell> cells) {
        this.context = context;
        this.cells = cells;
    }

    @NonNull
    @Override
    public CellViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_wordle_cell, parent, false);
        return new CellViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CellViewHolder holder, int position) {
        WordleCell cell = cells.get(position);

        // Set letter
        holder.textLetter.setText(cell.getLetter() == ' ' ? "" : String.valueOf(cell.getLetter()));

        int backgroundColor;
        int textColor = ContextCompat.getColor(context, android.R.color.white);

        switch (cell.getState()) {
            case WordleCell.STATE_CORRECT:
                backgroundColor = ContextCompat.getColor(context, R.color.wordle_correct);
                break;
            case WordleCell.STATE_MISPLACED:
                backgroundColor = ContextCompat.getColor(context, R.color.wordle_misplaced);
                break;
            case WordleCell.STATE_WRONG:
                backgroundColor = ContextCompat.getColor(context, R.color.wordle_wrong);
                break;
            case WordleCell.STATE_EMPTY:
            default:
                backgroundColor = ContextCompat.getColor(context, android.R.color.white);
                textColor = ContextCompat.getColor(context, android.R.color.black);
                break;
        }

        holder.cardView.setCardBackgroundColor(backgroundColor);
        holder.textLetter.setTextColor(textColor);
    }

    @Override
    public int getItemCount() {
        return cells.size();
    }

    public void updateCells(List<WordleCell> newCells) {
        this.cells = newCells;
        notifyDataSetChanged();
    }

    static class CellViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView textLetter;

        CellViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            textLetter = itemView.findViewById(R.id.textLetter);
        }
    }
}