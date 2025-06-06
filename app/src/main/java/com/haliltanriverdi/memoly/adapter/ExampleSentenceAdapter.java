package com.haliltanriverdi.memoly.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.haliltanriverdi.memoly.R;

import java.util.List;

public class ExampleSentenceAdapter extends RecyclerView.Adapter<ExampleSentenceAdapter.SentenceViewHolder> {

    private final Context context;
    private final List<String> sentences;

    public ExampleSentenceAdapter(Context context, List<String> sentences) {
        this.context = context;
        this.sentences = sentences;
    }

    @NonNull
    @Override
    public SentenceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_example_sentence, parent, false);
        return new SentenceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SentenceViewHolder holder, int position) {
        String sentence = sentences.get(position);
        
        if (!TextUtils.isEmpty(sentence)) {
            holder.sentenceTextView.setText(sentence);
            holder.itemView.setVisibility(View.VISIBLE);
            
            // Add animations
            holder.itemView.setAlpha(0f);
            holder.itemView.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .setStartDelay(position * 50)
                    .start();
        } else {
            holder.itemView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return sentences.size();
    }

    static class SentenceViewHolder extends RecyclerView.ViewHolder {
        TextView sentenceTextView;

        SentenceViewHolder(@NonNull View itemView) {
            super(itemView);
            sentenceTextView = itemView.findViewById(R.id.text_example_sentence);
        }
    }
}