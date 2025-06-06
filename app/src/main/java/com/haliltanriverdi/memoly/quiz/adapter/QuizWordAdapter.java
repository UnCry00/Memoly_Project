package com.haliltanriverdi.memoly.quiz.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.haliltanriverdi.memoly.R;
import com.haliltanriverdi.memoly.databinding.ItemQuizWordBinding;
import com.haliltanriverdi.memoly.model.Word;

import java.util.ArrayList;
import java.util.List;

public class QuizWordAdapter extends RecyclerView.Adapter<QuizWordAdapter.WordViewHolder> {

    private List<Word> words;
    private boolean isEditable;
    private OnWordClickListener listener;

    public QuizWordAdapter(List<Word> words, boolean isEditable) {
        this.words = words != null ? words : new ArrayList<>();
        this.isEditable = isEditable;
    }

    public void setOnWordClickListener(OnWordClickListener listener) {
        this.listener = listener;
    }

    public void updateWords(List<Word> newWords) {
        this.words = newWords != null ? newWords : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemQuizWordBinding binding = ItemQuizWordBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new WordViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull WordViewHolder holder, int position) {
        Word word = words.get(position);
        holder.bind(word);

        // Animasyon ekle (adapter)
        holder.itemView.setAlpha(0f);
        holder.itemView.animate()
                .alpha(1f)
                .setDuration(300)
                .setStartDelay(position * 50L)
                .start();
    }

    @Override
    public int getItemCount() {
        return words.size();
    }

    public class WordViewHolder extends RecyclerView.ViewHolder {
        private final ItemQuizWordBinding binding;

        public WordViewHolder(ItemQuizWordBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Word word) {
            binding.textViewTurkishWord.setText(word.getTurkishWord());
            binding.textViewEnglishWord.setText(word.getEnglishWord());

            // Kelime görselini yükle
            if (word.getImagePath() != null && !word.getImagePath().isEmpty()) {
                Glide.with(binding.getRoot().getContext())
                        .load(word.getImagePath())
                        .placeholder(R.drawable.uploadimage)
                        .error(R.drawable.uploadimage)
                        .fitCenter()
                        .into(binding.imageViewWord);
            } else {
                binding.imageViewWord.setImageResource(R.drawable.uploadimage);
            }

            // Düzenleme ve silme butonlarını göster/gizle
            binding.buttonEdit.setVisibility(isEditable ? View.VISIBLE : View.GONE);
            binding.buttonDelete.setVisibility(isEditable ? View.VISIBLE : View.GONE);

            // Tıklama olayları
            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onWordClick(word);
                }
            });

            binding.buttonEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(word);
                }
            });

            binding.buttonDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(word);
                }
            });
        }
    }

    public interface OnWordClickListener {
        void onWordClick(Word word);
        void onEditClick(Word word);
        void onDeleteClick(Word word);
    }
}