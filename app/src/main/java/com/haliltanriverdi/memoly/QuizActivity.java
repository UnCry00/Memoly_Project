package com.haliltanriverdi.memoly;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.haliltanriverdi.memoly.R;
import com.haliltanriverdi.memoly.databinding.ActivityQuizBinding;
import com.haliltanriverdi.memoly.databinding.DialogQuizResultBinding;
import com.haliltanriverdi.memoly.databinding.DialogNotEnoughWordsBinding;
import com.haliltanriverdi.memoly.quiz.QuizManager;
import com.haliltanriverdi.memoly.model.QuizQuestion;
import com.haliltanriverdi.memoly.model.QuizSession;
import com.haliltanriverdi.memoly.model.Word;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class QuizActivity extends AppCompatActivity {
    private ActivityQuizBinding binding;
    private QuizManager quizManager;
    private QuizSession quizSession;
    private boolean quizStarted = false;
    private FirebaseFirestore db;
    private String userId;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private static final int DELAY_NEXT_QUESTION = 1500; // 1.5 saniye
    private boolean isProcessingRadioChange = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQuizBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Quiz");
        }

        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userId = user != null ? user.getUid() : "";

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (quizStarted) {
                    showExitConfirmationDialog();
                } else {
                    finish();
                }
            }
        });

        quizManager = new QuizManager(this);

        loadQuizSettings();

        // Başlangıç ekranını göster
        showStartScreen();

        // Başla butonuna tıklama
        binding.btnStart.setOnClickListener(v -> {
            if (!quizStarted) {
                // Quiz henüz başlamadıysa, quiz'i hazırla
                prepareQuiz();
            }
        });

        // Seçenek grubuna tıklama
        binding.radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (quizStarted && !isProcessingRadioChange && checkedId != -1) {
                // Kullanıcı bir seçenek seçtiğinde
                checkAnswer(checkedId);
            }
        });
    }

    private void loadQuizSettings() {
        if (userId.isEmpty()) return;

        db.collection("users").document(userId).collection("settings").document("quiz")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long count = documentSnapshot.getLong("questionCount");
                        if (count != null) {
                            quizManager.setQuestionCount(count.intValue());
                        }
                    }
                });
    }

    private void showStartScreen() {
        binding.quizContentLayout.setVisibility(View.GONE);
        binding.startScreenLayout.setVisibility(View.VISIBLE);
        binding.btnStart.setVisibility(View.VISIBLE);
        binding.btnStart.setText("Başla");
        quizStarted = false;
    }

    private void showQuizScreen() {
        binding.startScreenLayout.setVisibility(View.GONE);
        binding.quizContentLayout.setVisibility(View.VISIBLE);
        binding.btnStart.setVisibility(View.GONE); // Başla butonu gizle
        quizStarted = true;
    }

    private void prepareQuiz() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnStart.setEnabled(false);

        quizManager.getAvailableWordsCount(new QuizManager.OnAvailableWordsCountListener() {
            @Override
            public void onCountReady(int count) {
                if (count < quizManager.getQuestionCount()) {
                    binding.progressBar.setVisibility(View.GONE);
                    showNotEnoughWordsDialog(count, quizManager.getQuestionCount());
                } else {
                    startQuiz();
                }
            }

            @Override
            public void onError(String errorMessage) {
                binding.progressBar.setVisibility(View.GONE);
                showErrorDialog(errorMessage);
            }
        });
    }

    private void startQuiz() {
        quizManager.prepareQuiz(new QuizManager.OnQuizReadyListener() {
            @Override
            public void onQuizReady(QuizSession session) {
                quizSession = session;
                showQuizScreen();
                displayCurrentQuestion();
                binding.progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onNotEnoughWords(int availableCount, int requiredCount) {
                binding.progressBar.setVisibility(View.GONE);
                showNotEnoughWordsDialog(availableCount, requiredCount);
            }

            @Override
            public void onError(String errorMessage) {
                binding.progressBar.setVisibility(View.GONE);
                showErrorDialog(errorMessage);
            }
        });
    }

    private void displayCurrentQuestion() {
        if (quizSession == null) return;

        QuizQuestion question = quizSession.getCurrentQuestion();
        if (question == null) return;

        // İşlem bayrağını ayarla
        isProcessingRadioChange = true;

        // Soru numarasını göster
        binding.textQuestionNumber.setText((quizSession.getCurrentQuestionIndex() + 1) + "/" + quizSession.getTotalQuestions());

        // Türkçe kelimeyi göster
        binding.textTurkishWord.setText(question.getCorrectWord().getTurkishWord());

        // Resmi göster
        String imagePath = question.getCorrectWord().getImagePath();
        if (imagePath != null && !imagePath.isEmpty()) {
            binding.imageWord.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(imagePath)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .into(binding.imageWord);
        } else {
            binding.imageWord.setVisibility(View.GONE);
        }

        // Seçenekleri göster
        binding.radioGroup.clearCheck();

        List<Word> options = question.getOptions();
        if (options.size() >= 1) binding.radioOption1.setText(options.get(0).getEnglishWord());
        if (options.size() >= 2) binding.radioOption2.setText(options.get(1).getEnglishWord());
        if (options.size() >= 3) binding.radioOption3.setText(options.get(2).getEnglishWord());
        if (options.size() >= 4) binding.radioOption4.setText(options.get(3).getEnglishWord());

        // Seçeneklerin arka plan renklerini sıfırla
        resetOptionBackgrounds();

        // Seçenekleri etkinleştir
        enableOptions();

        // İşlem bayrağını sıfırla
        isProcessingRadioChange = false;
    }

    private void resetOptionBackgrounds() {
        binding.radioOption1.setBackgroundResource(R.drawable.option_background);
        binding.radioOption2.setBackgroundResource(R.drawable.option_background);
        binding.radioOption3.setBackgroundResource(R.drawable.option_background);
        binding.radioOption4.setBackgroundResource(R.drawable.option_background);
    }

    private void checkAnswer(int checkedId) {
        if (quizSession == null) return;

        QuizQuestion currentQuestion = quizSession.getCurrentQuestion();
        if (currentQuestion == null) return;

        // Kullanıcının cevabını al
        int selectedIndex = -1;
        if (checkedId == R.id.radioOption1) selectedIndex = 0;
        else if (checkedId == R.id.radioOption2) selectedIndex = 1;
        else if (checkedId == R.id.radioOption3) selectedIndex = 2;
        else if (checkedId == R.id.radioOption4) selectedIndex = 3;

        // Geçersiz seçim kontrolü
        if (selectedIndex == -1) return;

        // Cevabı kaydet
        currentQuestion.setSelectedOptionIndex(selectedIndex);

        // Doğru cevap ise sayacı artır
        if (currentQuestion.isCorrect()) {
            quizSession.incrementCorrectAnswersCount();
        }

        // Doğru ve yanlış cevapları göster
        showAnswerFeedback(currentQuestion);

        // Seçeneklere tıklamayı devre dışı bırak
        disableOptions();

        // Belirli bir süre sonra sonraki soruya geç
        handler.postDelayed(() -> {
            if (quizSession.hasNextQuestion()) {
                quizSession.getNextQuestion();
                displayCurrentQuestion();
            } else {
                finishQuiz();
            }
        }, DELAY_NEXT_QUESTION);
    }

    private void disableOptions() {
        binding.radioOption1.setEnabled(false);
        binding.radioOption2.setEnabled(false);
        binding.radioOption3.setEnabled(false);
        binding.radioOption4.setEnabled(false);
    }

    private void enableOptions() {
        binding.radioOption1.setEnabled(true);
        binding.radioOption2.setEnabled(true);
        binding.radioOption3.setEnabled(true);
        binding.radioOption4.setEnabled(true);
    }

    private void showAnswerFeedback(QuizQuestion question) {
        int correctIndex = question.getCorrectOptionIndex();
        int selectedIndex = question.getSelectedOptionIndex();

        // Doğru cevabı yeşil yap
        RadioButton correctOption = null;
        switch (correctIndex) {
            case 0:
                correctOption = binding.radioOption1;
                break;
            case 1:
                correctOption = binding.radioOption2;
                break;
            case 2:
                correctOption = binding.radioOption3;
                break;
            case 3:
                correctOption = binding.radioOption4;
                break;
        }

        if (correctOption != null) {
            correctOption.setBackgroundResource(R.drawable.option_background_correct);
        }

        // Eğer yanlış cevap verilmişse, seçilen cevabı kırmızı yap
        if (selectedIndex != correctIndex) {
            RadioButton selectedOption = null;
            switch (selectedIndex) {
                case 0:
                    selectedOption = binding.radioOption1;
                    break;
                case 1:
                    selectedOption = binding.radioOption2;
                    break;
                case 2:
                    selectedOption = binding.radioOption3;
                    break;
                case 3:
                    selectedOption = binding.radioOption4;
                    break;
            }

            if (selectedOption != null) {
                selectedOption.setBackgroundResource(R.drawable.option_background_incorrect);
            }
        }
    }

    private void finishQuiz() {
        // Sonuçları kaydet
        quizManager.saveQuizResults(new QuizManager.OnQuizResultSavedListener() {
            @Override
            public void onResultSaved() {
                // Sonuç ekranını göster
                showQuizResultDialog();
            }

            @Override
            public void onError(String errorMessage) {
                showErrorDialog("Sonuçlar kaydedilemedi: " + errorMessage);
            }
        });
    }

    private void showQuizResultDialog() {
        if (quizSession == null) return;

        // Custom dialog oluştur
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        DialogQuizResultBinding dialogBinding = DialogQuizResultBinding.inflate(getLayoutInflater());
        builder.setView(dialogBinding.getRoot());

        // Dialog içeriğini ayarla
        dialogBinding.textScore.setText(quizSession.getCorrectAnswersCount() + " / " + quizSession.getTotalQuestions());

        // Dialog'u göster
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Devam et butonuna tıklama
        dialogBinding.btnContinue.setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });

        dialog.show();
    }

    private void showNotEnoughWordsDialog(int availableCount, int requiredCount) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        DialogNotEnoughWordsBinding dialogBinding = DialogNotEnoughWordsBinding.inflate(getLayoutInflater());
        builder.setView(dialogBinding.getRoot());

        // Dialog içeriğini ayarla
        dialogBinding.textMessage.setText("Quiz için yeterli kelime yok. " +
                availableCount + " kelime mevcut, " +
                requiredCount + " kelime gerekli.");

        // Dialog'u göster
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Vazgeç butonuna tıklama
        dialogBinding.btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
        });

        // Yeni kelime ekle butonuna tıklama
        dialogBinding.btnAddWord.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(QuizActivity.this, AddWordActivity.class);
            startActivity(intent);
            finish();
        });

        dialog.show();
    }

    private void showExitConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Quiz'den Çık");
        builder.setMessage("Quiz'den çıkmak istediğinize emin misiniz? İlerlemeniz kaydedilmeyecek.");
        builder.setPositiveButton("Evet", (dialog, which) -> {
            dialog.dismiss();
            finish();
        });
        builder.setNegativeButton("Hayır", (dialog, which) -> {
            dialog.dismiss();
        });
        builder.show();
    }

    private void showErrorDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Hata");
        builder.setMessage(message);
        builder.setPositiveButton("Tamam", (dialog, which) -> {
            dialog.dismiss();
        });
        builder.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_quiz, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(this, QuizSettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Quiz ayarlarını yeniden yükle (Kontrol)
        loadQuizSettings();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Handler'ı temizle
        handler.removeCallbacksAndMessages(null);
    }
}
