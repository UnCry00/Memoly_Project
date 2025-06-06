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
import com.haliltanriverdi.memoly.wordle.model.WordleKey;

import java.util.List;

public class WordleKeyboardAdapter extends RecyclerView.Adapter<WordleKeyboardAdapter.KeyViewHolder> {

    private List<WordleKey> keys;
    private Context context;
    private OnKeyClickListener listener;

    public interface OnKeyClickListener {
        void onKeyClick(char key);
    }

    public WordleKeyboardAdapter(Context context, List<WordleKey> keys, OnKeyClickListener listener) {
        this.context = context;
        this.keys = keys;
        this.listener = listener;
    }

    @NonNull
    @Override
    public KeyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_keyboard_key, parent, false);
        return new KeyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull KeyViewHolder holder, int position) {
        WordleKey key = keys.get(position);

        holder.textKey.setText(String.valueOf(key.getKey()));

        int backgroundColor;
        int textColor = ContextCompat.getColor(context, android.R.color.white);

        switch (key.getState()) {
            case WordleKey.STATE_CORRECT:
                backgroundColor = ContextCompat.getColor(context, R.color.wordle_correct);
                break;
            case WordleKey.STATE_MISPLACED:
                backgroundColor = ContextCompat.getColor(context, R.color.wordle_misplaced);
                break;
            case WordleKey.STATE_WRONG:
                backgroundColor = ContextCompat.getColor(context, R.color.wordle_wrong);
                break;
            case WordleKey.STATE_UNUSED:
                backgroundColor = ContextCompat.getColor(context, R.color.keyboard_default);
                textColor = ContextCompat.getColor(context, android.R.color.black);
                break;
            default:
                backgroundColor = ContextCompat.getColor(context, R.color.keyboard_default);
                textColor = ContextCompat.getColor(context, android.R.color.black);
                break;
        }

        holder.cardView.setCardBackgroundColor(backgroundColor);
        holder.textKey.setTextColor(textColor);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onKeyClick(key.getKey());
            }
        });
    }

    @Override
    public int getItemCount() {
        return keys.size();
    }

    public void updateKeys(List<WordleKey> newKeys) {
        this.keys = newKeys;
        notifyDataSetChanged();
    }

    static class KeyViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView textKey;

        KeyViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            textKey = itemView.findViewById(R.id.textKey);
        }
    }
}