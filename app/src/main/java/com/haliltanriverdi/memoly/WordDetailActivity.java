package com.haliltanriverdi.memoly;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.haliltanriverdi.memoly.adapter.ExampleSentenceAdapter;
import com.haliltanriverdi.memoly.databinding.ActivityWordDetailBinding;
import com.haliltanriverdi.memoly.firebase.FirebaseManager;
import com.haliltanriverdi.memoly.model.Word;

public class WordDetailActivity extends AppCompatActivity {

    public static final String EXTRA_WORD_ID = "word_id";

    private ActivityWordDetailBinding binding;
    private FirebaseManager firebaseManager;
    private Word word;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWordDetailBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        firebaseManager = FirebaseManager.getInstance();

        String wordId = getIntent().getStringExtra(EXTRA_WORD_ID);
        if (wordId == null) {
            finish();
            return;
        }

        loadWordData(wordId);
    }

    private void loadWordData(String wordId) {
        firebaseManager.getWord(wordId, new FirebaseManager.OnWordOperationListener() {
            @Override
            public void onSuccess(Word loadedWord) {
                word = loadedWord;
                displayWordDetails();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(WordDetailActivity.this, "Kelime yüklenemedi: " + error, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void displayWordDetails() {
        binding.textTurkishWord.setText(word.getTurkishWord());
        binding.textEnglishWord.setText(word.getEnglishWord());

        if (word.getImagePath() != null && !word.getImagePath().isEmpty()) {
            Glide.with(this)
                    .load(word.getImagePath())
                    .placeholder(R.drawable.uploadimage)
                    .error(R.drawable.uploadimage)
                    .fitCenter()
                    .into(binding.imageWord);
        } else {
            binding.imageWord.setImageResource(R.drawable.uploadimage);
        }

        binding.recyclerExamples.setLayoutManager(new LinearLayoutManager(this));
        ExampleSentenceAdapter adapter = new ExampleSentenceAdapter(this, word.getExampleSentences());
        binding.recyclerExamples.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}