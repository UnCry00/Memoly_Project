package com.haliltanriverdi.memoly.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.haliltanriverdi.memoly.R;
import com.haliltanriverdi.memoly.model.Word;

import java.util.List;

public class WordAdapter extends RecyclerView.Adapter<WordAdapter.WordViewHolder> {

    private final Context context;
    private final List<Word> wordList;
    private final WordItemListener listener;

    public WordAdapter(Context context, List<Word> wordList, WordItemListener listener) {
        this.context = context;
        this.wordList = wordList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public WordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_word, parent, false);
        return new WordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WordViewHolder holder, int position) {
        Word word = wordList.get(position);

        holder.turkishWordTextView.setText(word.getTurkishWord());
        holder.englishWordTextView.setText(word.getEnglishWord());

        // Glide ile resim yükle
        if (word.getImagePath() != null && !word.getImagePath().isEmpty()) {
            Glide.with(context)
                 .load(word.getImagePath())
                 .placeholder(R.drawable.uploadimage)
                 .error(R.drawable.uploadimage)
                 .fitCenter()
                 .into(holder.wordImageView);
        } else {
            holder.wordImageView.setImageResource(R.drawable.uploadimage);
        }

        holder.editButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(word);
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(word);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onWordClick(word);
            }
        });

        holder.itemView.setAlpha(0f);
        holder.itemView.animate()
                .alpha(1f)
                .setDuration(300)
                .setStartDelay(position * 50L)
                .start();
    }

    @Override
    public int getItemCount() {
        return wordList.size();
    }


    static class WordViewHolder extends RecyclerView.ViewHolder {
        ImageView wordImageView;
        TextView turkishWordTextView;
        TextView englishWordTextView;
        ImageButton editButton;
        ImageButton deleteButton;

        WordViewHolder(@NonNull View itemView) {
            super(itemView);
            wordImageView = itemView.findViewById(R.id.image_word);
            turkishWordTextView = itemView.findViewById(R.id.text_turkish_word);
            englishWordTextView = itemView.findViewById(R.id.text_english_word);
            editButton = itemView.findViewById(R.id.button_edit);
            deleteButton = itemView.findViewById(R.id.button_delete);
        }
    }

    public interface WordItemListener {
        void onWordClick(Word word);
        void onEditClick(Word word);
        void onDeleteClick(Word word);
    }
}