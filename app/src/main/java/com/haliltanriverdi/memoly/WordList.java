package com.haliltanriverdi.memoly;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.google.firebase.auth.FirebaseAuth;
import com.haliltanriverdi.memoly.adapter.WordAdapter;
import com.haliltanriverdi.memoly.databinding.ActivityWordListBinding;
import com.haliltanriverdi.memoly.databinding.DialogDeleteWordBinding;
import com.haliltanriverdi.memoly.firebase.FirebaseManager;
import com.haliltanriverdi.memoly.model.Word;
import com.haliltanriverdi.memoly.model.WordLearningStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WordList extends AppCompatActivity implements WordAdapter.WordItemListener {

    private ActivityWordListBinding binding;
    private WordAdapter wordAdapter;
    private List<Word> wordList;
    private FirebaseManager firebaseManager;
    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWordListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startLoginActivity();
            return;
        }

        setSupportActionBar(binding.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Kelime Listesi");

        firebaseManager = FirebaseManager.getInstance();

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        wordList = new ArrayList<>();
        wordAdapter = new WordAdapter(this, wordList, this);
        binding.recyclerView.setAdapter(wordAdapter);

        binding.editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    loadWords();
                } else {
                    searchWords(query);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        loadWords();

        sharedPreferences = getSharedPreferences("Memoly_wordlist", MODE_PRIVATE);
        boolean isFirstRun = sharedPreferences.getBoolean("isFirstWordListTutorial", true);

        if (isFirstRun) {
            showWordListTutorial();
        }
    }

    private void showWordListTutorial() {
        TapTargetSequence sequence = new TapTargetSequence(this)
                .targets(
                        TapTarget.forView(binding.editSearch, "Kelime Arama", "Eklediğiniz kelimeleri İngilizce ve Türkçe olarak arayabilrisiniz.")
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
                        TapTarget.forView(binding.buttonAddWord, "Kelime Ekleme", "Yeni kelimeler ekleyerek öğrenme serüvenine Başlayabilrisiniz. Diğer bölümleri kullanabilmek için en az 5 kelime eklemeniz gerek !")
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
                                .transparentTarget(true)
                )
                .listener(new TapTargetSequence.Listener() {
                    @Override
                    public void onSequenceFinish() {
                        Toast.makeText(WordList.this, "Eğitim tamamlandı!", Toast.LENGTH_SHORT).show();
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("isFirstWordListTutorial", false);
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


    public void onAddWordClick(View view) {
        Intent intent = new Intent(WordList.this, AddWordActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadWords();
    }

    private void startLoginActivity() {
        Toast.makeText(this, "Lütfen önce giriş yapın", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void loadWords() {
        firebaseManager.getAllWords(new FirebaseManager.OnWordsLoadedListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onWordsLoaded(List<Word> words) {
                wordList.clear();
                wordList.addAll(words);
                wordAdapter.notifyDataSetChanged();

                if (wordList.isEmpty()) {
                    binding.recyclerView.setVisibility(View.GONE);
                    binding.emptyView.setVisibility(View.VISIBLE);
                } else {
                    binding.recyclerView.setVisibility(View.VISIBLE);
                    binding.emptyView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(WordList.this, "Kelimeler yüklenemedi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchWords(String query) {
        firebaseManager.searchWords(query, new FirebaseManager.OnWordsLoadedListener() {
            @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
            @Override
            public void onWordsLoaded(List<Word> words) {
                wordList.clear();
                wordList.addAll(words);
                wordAdapter.notifyDataSetChanged();

                if (wordList.isEmpty()) {
                    binding.recyclerView.setVisibility(View.GONE);
                    binding.emptyView.setVisibility(View.VISIBLE);
                    binding.emptyView.setText("Aramanızla eşleşen kelime bulunamadı");
                } else {
                    binding.recyclerView.setVisibility(View.VISIBLE);
                    binding.emptyView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(WordList.this, "Arama yapılamadı: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onWordClick(Word word) {
        Intent intent = new Intent(WordList.this, WordDetailActivity.class);
        intent.putExtra(WordDetailActivity.EXTRA_WORD_ID, word.getId());
        startActivity(intent);
    }

    @Override
    public void onEditClick(Word word) {
        Intent intent = new Intent(WordList.this, AddWordActivity.class);
        intent.putExtra(AddWordActivity.EXTRA_WORD_ID, word.getId());
        intent.putExtra(AddWordActivity.EXTRA_EDIT_MODE, true);
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(Word word) {
        showDeleteConfirmationDialog(word);
    }

    private void showDeleteConfirmationDialog(Word word) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        DialogDeleteWordBinding dialogBinding = DialogDeleteWordBinding.inflate(getLayoutInflater());
        dialog.setContentView(dialogBinding.getRoot());

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.8);
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            params.gravity = Gravity.CENTER;
            window.setAttributes(params);
        }

        dialogBinding.textTurkishWord.setText(word.getTurkishWord());
        dialogBinding.textEnglishWord.setText(word.getEnglishWord());

        if (word.getImagePath() != null && !word.getImagePath().isEmpty()) {
            Glide.with(this)
                    .load(word.getImagePath())
                    .placeholder(R.drawable.uploadimage)
                    .error(R.drawable.uploadimage)
                    .fitCenter()
                    .into(dialogBinding.imageWord);
        } else {
            dialogBinding.imageWord.setImageResource(R.drawable.uploadimage);
        }

        dialogBinding.buttonDelete.setOnClickListener(v -> onDeleteConfirmClick(word, dialog));
        dialogBinding.buttonCancel.setOnClickListener(v -> onCancelClick(dialog));

        dialog.show();
    }

    private void onDeleteConfirmClick(Word word, Dialog dialog) {
        firebaseManager.deleteWord(word, new FirebaseManager.OnWordOperationListener() {
            @Override
            public void onSuccess(Word word) {
                Toast.makeText(WordList.this, "Kelime silindi", Toast.LENGTH_SHORT).show();
                loadWords();
                dialog.dismiss();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(WordList.this, "Kelime silinemedi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onCancelClick(Dialog dialog) {
        dialog.dismiss();
    }
}