package com.haliltanriverdi.memoly.quiz.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.haliltanriverdi.memoly.R;
import com.haliltanriverdi.memoly.databinding.ItemLearnedWordBinding;
import com.haliltanriverdi.memoly.model.LearnedWord;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class LearnedWordAdapter extends RecyclerView.Adapter<LearnedWordAdapter.LearnedWordViewHolder> {
    private List<LearnedWord> learnedWords;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());


    public LearnedWordAdapter(List<LearnedWord> learnedWords) {
        this.learnedWords = learnedWords;
    }

    @NonNull
    @Override
    public LearnedWordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemLearnedWordBinding binding = ItemLearnedWordBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new LearnedWordViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull LearnedWordViewHolder holder, int position) {
        LearnedWord learnedWord = learnedWords.get(position);
        holder.bind(learnedWord);
    }

    @Override
    public int getItemCount() {
        return learnedWords.size();
    }

    class LearnedWordViewHolder extends RecyclerView.ViewHolder {
        private ItemLearnedWordBinding binding;

        public LearnedWordViewHolder(ItemLearnedWordBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(LearnedWord learnedWord) {
            // Türkçe kelimeyi ayarla
            binding.textTurkishWord.setText(learnedWord.getTurkishWord());

            // İngilizce kelimeyi ayarla
            binding.textEnglishWord.setText(learnedWord.getEnglishWord());

            // Resmi yükle
            if (learnedWord.getImagePath() != null && !learnedWord.getImagePath().isEmpty()) {
                Glide.with(binding.imageWord.getContext())
                        .load(learnedWord.getImagePath())
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.placeholder_image)
                        .fitCenter()
                        .into(binding.imageWord);
            } else {
                binding.imageWord.setImageResource(R.drawable.placeholder_image);
            }

            // Seviyeyi ayarla
            String levelText = "Seviye " + learnedWord.getLevel();
            binding.textLevel.setText(levelText);

            // Tamamen öğrenilmiş kelimeleri yeşil yap
            if (learnedWord.isLearned()) {
                binding.cardLearnedWord.setCardBackgroundColor(Color.parseColor("#E8F5E9"));
                binding.textLevel.setTextColor(Color.parseColor("#4CAF50"));
            } else {
                binding.cardLearnedWord.setCardBackgroundColor(Color.parseColor("#FFFFFF"));
                binding.textLevel.setTextColor(Color.parseColor("#673AB7"));
            }

            // Bir sonraki tekrar tarihini ayarla
            if (learnedWord.getNextReviewDate() != null) {
                String dateText = dateFormat.format(learnedWord.getNextReviewDate());
                binding.textNextReview.setText(dateText);

                // Eğer tamamen öğrenilmişse, tekrar tarihi yerine "Öğrenildi" yaz
                if (learnedWord.isLearned()) {
                    binding.textNextReview.setText("Öğrenildi");
                    binding.textNextReview.setTextColor(Color.parseColor("#4CAF50"));
                } else {
                    binding.textNextReview.setTextColor(Color.parseColor("#673AB7"));
                }
            } else {
                binding.textNextReview.setText("Belirsiz");
            }
        }
    }
    public LearnedWordAdapter(){}
}
