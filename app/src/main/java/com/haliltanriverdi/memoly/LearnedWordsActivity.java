package com.haliltanriverdi.memoly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.haliltanriverdi.memoly.quiz.adapter.LearnedWordAdapter;
import com.haliltanriverdi.memoly.databinding.ActivityLearnedWordsBinding;
import com.haliltanriverdi.memoly.model.LearnedWord;
import com.haliltanriverdi.memoly.model.Word;
import com.haliltanriverdi.memoly.model.WordLearningStatus;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LearnedWordsActivity extends AppCompatActivity {
    private static final String TAG = "LearnedWordsActivity";
    private ActivityLearnedWordsBinding binding;
    private LearnedWordAdapter adapter;
    private FirebaseFirestore db;
    private String userId;
    private List<LearnedWord> allLearnedWords;
    private List<LearnedWord> filteredLearnedWords;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLearnedWordsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Öğrenilen Kelimeler");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LearnedWordsActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userId = user != null ? user.getUid() : "";

        allLearnedWords = new ArrayList<>();
        filteredLearnedWords = new ArrayList<>();
        adapter = new LearnedWordAdapter(filteredLearnedWords);
        binding.recyclerLearnedWords.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerLearnedWords.setAdapter(adapter);

        setupSearchView();

        loadLearnedWords();

        sharedPreferences = getSharedPreferences("MemolyPrefs", MODE_PRIVATE);
        boolean isFirstRun = sharedPreferences.getBoolean("isFirstLearnedWordsTutorial", true);

        if (isFirstRun) {
            showLearnedWordsTutorial();
        }

    }

    private void showLearnedWordsTutorial() {
        TapTargetSequence sequence = new TapTargetSequence(this)
                .targets(
                        TapTarget.forView(binding.searchIcon, "Kelime Arama", "Aramak istediğin kelimeyi buraya yazabilirsin.")
                                .outerCircleColor(R.color.soft)
                                .targetCircleColor(android.R.color.white)
                                .titleTextColor(android.R.color.black)
                                .descriptionTextColor(android.R.color.black)
                                .titleTextSize(22)
                                .descriptionTextSize(16)
                                .targetRadius(75)
                                .textTypeface(Typeface.DEFAULT_BOLD)
                                .tintTarget(false)
                                .cancelable(false)
                                .transparentTarget(true),

                        TapTarget.forView(binding.progressBar, "Bilinen Kelimeler", "Quiz sorularında doğru cevap verdiğiniz kelimeler.")
                                .outerCircleColor(R.color.soft)
                                .targetCircleColor(android.R.color.white)
                                .titleTextColor(android.R.color.black)
                                .descriptionTextColor(android.R.color.black)
                                .titleTextSize(22)
                                .descriptionTextSize(16)
                                .targetRadius(60)
                                .textTypeface(Typeface.DEFAULT_BOLD)
                                .tintTarget(false)
                                .cancelable(false)
                                .transparentTarget(true),
                        TapTarget.forView(binding.progressBar, "Seviye", "1. seviye için arka arkaya iki kere doğru cevaplamış olmanız gerek.Diğer seviyeler için bir kere doğru bilmeniz yeterli. Doğru bilme serisini bozarsanız seviye sıfırlanır ve kelime listeden çıkarılır ")
                                .outerCircleColor(R.color.soft)
                                .targetCircleColor(android.R.color.white)
                                .titleTextColor(android.R.color.black)
                                .descriptionTextColor(android.R.color.black)
                                .titleTextSize(22)
                                .descriptionTextSize(16)
                                .targetRadius(60)
                                .textTypeface(Typeface.DEFAULT_BOLD)
                                .tintTarget(false)
                                .cancelable(false)
                                .transparentTarget(true),
                        TapTarget.forView(binding.progressBar, "Tarih", "Doğru bildiğiniz kelimenin quiz sorusu olarak bir daha ne zaman karşınıza geleceğini gösterir")
                                .outerCircleColor(R.color.soft)
                                .targetCircleColor(android.R.color.white)
                                .titleTextColor(android.R.color.black)
                                .descriptionTextColor(android.R.color.black)
                                .titleTextSize(22)
                                .descriptionTextSize(16)
                                .targetRadius(60)
                                .textTypeface(Typeface.DEFAULT_BOLD)
                                .tintTarget(false)
                                .cancelable(false)
                                .transparentTarget(true)
                )
                .listener(new TapTargetSequence.Listener() {
                    @Override
                    public void onSequenceFinish() {
                        Toast.makeText(LearnedWordsActivity.this, "Eğitim tamamlandı!", Toast.LENGTH_SHORT).show();
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("isFirstLearnedWordsTutorial", false);
                        editor.apply();
                    }

                    @Override
                    public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {

                    }

                    @Override
                    public void onSequenceCanceled(TapTarget lastTarget) {
                    }
                });

        sequence.start();
    }


    private void setupSearchView() {
        binding.searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                filterWords(s.toString());
            }
        });

        binding.clearSearchButton.setOnClickListener(v -> {
            binding.searchEditText.setText("");
        });
    }

    private void filterWords(String query) {
        filteredLearnedWords.clear();

        if (query.isEmpty()) {
            filteredLearnedWords.addAll(allLearnedWords);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (LearnedWord word : allLearnedWords) {
                if (word.getTurkishWord().toLowerCase().contains(lowerCaseQuery) ||
                        word.getEnglishWord().toLowerCase().contains(lowerCaseQuery)) {
                    filteredLearnedWords.add(word);
                }
            }
        }

        adapter.notifyDataSetChanged();

        if (filteredLearnedWords.isEmpty()) {
            binding.textNoWords.setVisibility(View.VISIBLE);
            binding.textNoWords.setText("Arama sonucu bulunamadı");
        } else {
            binding.textNoWords.setVisibility(View.GONE);
        }
    }

    private void loadLearnedWords() {
        if (userId.isEmpty()) {
            showError("Kullanıcı oturum açmamış");
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.textNoWords.setVisibility(View.GONE);

        // Önce tüm kelimeleri yükle
        final Map<String, Word> wordsMap = new HashMap<>();
        db.collection("users").document(userId).collection("words")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Word word = document.toObject(Word.class);
                            word.setId(document.getId());
                            wordsMap.put(document.getId(), word);
                        }

                        // Sonra öğrenme durumlarını yükle
                        loadLearningStatus(wordsMap);
                    } else {
                        binding.progressBar.setVisibility(View.GONE);
                        showError("Kelimeler yüklenemedi: " + task.getException().getMessage());
                    }
                });
    }

    private void loadLearningStatus(final Map<String, Word> wordsMap) {
        db.collection("users").document(userId).collection("learningStatus")
                .get()
                .addOnCompleteListener(task -> {
                    binding.progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        allLearnedWords.clear();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            WordLearningStatus status = document.toObject(WordLearningStatus.class);
                            Word word = wordsMap.get(document.getId());

                            // Sadece seviye 1 ve üzeri kelimeleri ekle
                            if (word != null && status.getLevel() >= 1) {
                                LearnedWord learnedWord = new LearnedWord();
                                learnedWord.setWordId(word.getId());
                                learnedWord.setTurkishWord(word.getTurkishWord());
                                learnedWord.setEnglishWord(word.getEnglishWord());
                                learnedWord.setImagePath(word.getImagePath());
                                learnedWord.setLevel(status.getLevel());
                                learnedWord.setNextReviewDate(status.getNextReviewDate());
                                learnedWord.setLearned(status.isLearned());

                                allLearnedWords.add(learnedWord);
                            }
                        }

                        // Filtrelenmiş listeyi güncelle
                        filteredLearnedWords.clear();
                        filteredLearnedWords.addAll(allLearnedWords);
                        adapter.notifyDataSetChanged();

                        if (allLearnedWords.isEmpty()) {
                            binding.textNoWords.setVisibility(View.VISIBLE);
                            binding.textNoWords.setText("Henüz öğrenilen kelime yok");
                        } else {
                            binding.textNoWords.setVisibility(View.GONE);
                        }

                        Log.d(TAG, "Yüklenen öğrenilen kelime sayısı: " + allLearnedWords.size());
                    } else {
                        showError("Öğrenme durumları yüklenemedi: " + task.getException().getMessage());
                    }
                });
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        Log.e(TAG, message);
    }
}
