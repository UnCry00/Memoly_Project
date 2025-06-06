package com.haliltanriverdi.memoly;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.haliltanriverdi.memoly.R;
import com.haliltanriverdi.memoly.databinding.ActivityQuizSettingsBinding;
import com.haliltanriverdi.memoly.quiz.QuizManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class QuizSettingsActivity extends AppCompatActivity {
    private ActivityQuizSettingsBinding binding;
    private FirebaseFirestore db;
    private String userId;
    private int questionCount = 10; // Default değer

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQuizSettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Quiz Ayarları");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                saveSettings();
                finish();
            }
        });

        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userId = user != null ? user.getUid() : "";

        loadSettings();

        binding.seekBarQuestionCount.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Minimum 5, maksimum 20 soru
                questionCount = progress + 5;
                binding.textQuestionCount.setText(String.valueOf(questionCount));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Bu metod şu anda kullanılmıyor, ileride işlevsellik eklenebilir.
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Bu metod şu anda kullanılmıyor, ileride işlevsellik eklenebilir.
            }
        });

        binding.btnSave.setOnClickListener(v -> {
            saveSettings();
            Toast.makeText(this, "Ayarlar kaydedildi", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void loadSettings() {
        if (userId.isEmpty()) return;

        db.collection("users").document(userId).collection("settings").document("quiz")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long count = documentSnapshot.getLong("questionCount");
                        if (count != null) {
                            questionCount = count.intValue();
                        }
                    }

                    // UI'ı güncelle
                    updateUI();
                });
    }

    private void updateUI() {
        // SeekBar'ı ayarla (5-20 arası)
        binding.seekBarQuestionCount.setProgress(questionCount - 5);
        binding.textQuestionCount.setText(String.valueOf(questionCount));
    }

    private void saveSettings() {
        if (userId.isEmpty()) return;

        DocumentReference settingsRef = db.collection("users").document(userId)
                .collection("settings").document("quiz");

        Map<String, Object> settings = new HashMap<>();
        settings.put("questionCount", questionCount);

        settingsRef.set(settings)
                .addOnSuccessListener(aVoid -> {
                    // QuizManager'ı güncelle
                    QuizManager quizManager = new QuizManager(this);
                    quizManager.setQuestionCount(questionCount);
                })
                .addOnFailureListener(e -> {
                    // Hata durumunda işlem yapılabilir
                    Toast.makeText(this, "Ayarlar kaydedilemedi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            saveSettings();
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
