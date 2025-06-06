package com.haliltanriverdi.memoly.wordle;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.haliltanriverdi.memoly.MainActivity;
import com.haliltanriverdi.memoly.R;
import com.haliltanriverdi.memoly.databinding.ActivityWordleGameBinding;
import com.haliltanriverdi.memoly.model.LearnedWord;
import com.haliltanriverdi.memoly.model.Word;
import com.haliltanriverdi.memoly.model.WordLearningStatus;
import com.haliltanriverdi.memoly.wordle.adapter.WordleCellAdapter;
import com.haliltanriverdi.memoly.wordle.adapter.WordleKeyboardAdapter;
import com.haliltanriverdi.memoly.wordle.model.WordleCell;
import com.haliltanriverdi.memoly.wordle.model.WordleKey;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class WordleGameActivity extends AppCompatActivity implements WordleKeyboardAdapter.OnKeyClickListener {
    private static final String TAG = "WordleGameActivity";
    private static final int MAX_ATTEMPTS = 3;

    private ActivityWordleGameBinding binding;
    private FirebaseFirestore db;
    private String userId;

    private List<LearnedWord> learnedWords;
    private List<String> playedWordsToday;
    private LearnedWord currentWord;
    private String targetWord;
    private int currentAttempt;
    private int wordleLevel;
    private int currentRowStartIndex;
    private int currentCellIndex;

    private List<WordleCell> gridCells;
    private List<WordleKey> keyboardKeys;
    private WordleCellAdapter gridAdapter;
    private WordleKeyboardAdapter keyboardAdapter;
    SharedPreferences sharedPreferences;


    private boolean gameActive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWordleGameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Wordle Oyunu");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WordleGameActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userId = user != null ? user.getUid() : "";

        learnedWords = new ArrayList<>();
        playedWordsToday = new ArrayList<>();
        gameActive = false;
        currentAttempt = 1;

        loadWordleLevel();

        setupKeyboard();

        binding.buttonSubmit.setOnClickListener(v -> submitGuess());

        sharedPreferences = getSharedPreferences("Memoly_wordle", MODE_PRIVATE);
        boolean isFirstRun = sharedPreferences.getBoolean("iswordleTutorial", true);

        if (isFirstRun) {
            showHowToPlay();
        }

        loadPlayedWordsToday();
    }

    private void loadWordleLevel() {
        if (userId.isEmpty()) {
            wordleLevel = 0;
            updateWordleLevelText();
            return;
        }

        db.collection("Users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.contains("wordleLevel")) {
                        wordleLevel = documentSnapshot.getLong("wordleLevel").intValue();
                    } else {
                        wordleLevel = 0;
                        // Wordle seviyesini kaydet
                        Map<String, Object> data = new HashMap<>();
                        data.put("wordleLevel", wordleLevel);
                        db.collection("Users").document(userId).update(data);
                    }
                    updateWordleLevelText();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Wordle seviyesi yüklenemedi: " + e.getMessage());
                    wordleLevel = 0;
                    updateWordleLevelText();
                });
    }

    private void updateWordleLevelText() {
        binding.textWordleLevel.setText(String.format(Locale.getDefault(), "Wordle Seviyesi: %d", wordleLevel));
    }

    private void loadPlayedWordsToday() {
        if (userId.isEmpty()) {
            loadLearnedWords();
            return;
        }

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        db.collection("users").document(userId).collection("wordlePlayed")
                .document(today)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("words")) {
                        List<String> words = (List<String>) documentSnapshot.get("words");
                        if (words != null) {
                            playedWordsToday = words;
                        }
                    }
                    loadLearnedWords();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Bugün oynanan kelimeler yüklenemedi: " + e.getMessage());
                    loadLearnedWords();
                });
    }

    private void savePlayedWordToday(String wordId) {
        if (userId.isEmpty()) return;

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        playedWordsToday.add(wordId);

        Map<String, Object> data = new HashMap<>();
        data.put("words", playedWordsToday);

        db.collection("users").document(userId).collection("wordlePlayed")
                .document(today)
                .set(data)
                .addOnFailureListener(e -> Log.e(TAG, "Oynanan kelime kaydedilemedi: " + e.getMessage()));
    }

    private void setupKeyboard() {
        keyboardKeys = new ArrayList<>();

        // İngilizce klavye düzeni (QWERTY)
        String[] rows = {
                "QWERTYUIOP",
                "ASDFGHJKL",
                "ZXCVBNM"
        };

        for (String row : rows) {
            for (char c : row.toCharArray()) {
                keyboardKeys.add(new WordleKey(c));
            }
        }

        // Backspace tuşu (← olarak gösteriliyor)
        keyboardKeys.add(new WordleKey('←', true));


        // Klavye adaptörünü ayarla
        keyboardAdapter = new WordleKeyboardAdapter(this, keyboardKeys, this);

        // Her satır için ayrı bir GridLayoutManager kullanarak klavyeyi ortala
        int maxRowLength = 10; // İlk satır (QWERTYUIOP) 10 tuş içeriyor
        GridLayoutManager keyboardLayout = new GridLayoutManager(this, maxRowLength);

        // Satır başlarını ve sonlarını ayarla
        keyboardLayout.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                // İlk satır (QWERTYUIOP): 10 tuş, her biri 1 span
                if (position < 10) {
                    return 1;
                }
                // İkinci satır (ASDFGHJKL): 9 tuş, her biri 1 span + başta ve sonda boşluk
                else if (position < 19) {
                    return 1;
                }
                // Üçüncü satır (ZXCVBNM): 7 tuş, her biri 1 span + başta ve sonda boşluk
                else {
                    return 1;
                }
            }
        });

        binding.recyclerKeyboard.setLayoutManager(keyboardLayout);
        binding.recyclerKeyboard.setAdapter(keyboardAdapter);
    }

    private void resetKeyboard() {
        Log.d(TAG, "Klavye sıfırlanıyor...");
        keyboardKeys.clear();
        String[] rows = {"QWERTYUIOP", "ASDFGHJKL", "ZXCVBNM"};
        for (String row : rows) {
            for (char c : row.toCharArray()) {
                keyboardKeys.add(new WordleKey(c));
            }
        }
        keyboardKeys.add(new WordleKey('←', true));
        keyboardAdapter.updateKeys(keyboardKeys);
        runOnUiThread(() -> {
            keyboardAdapter.notifyDataSetChanged();
            Log.d(TAG, "Klavye adaptörü güncellendi.");
        });
    }

    private void loadLearnedWords() {
        if (userId.isEmpty()) {
            showError("Kullanıcı oturum açmamış");
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);

        // Önce tüm kelimeleri yükle
        final Map<String, Word> wordsMap = new HashMap<>();
        db.collection("users").document(userId).collection("words")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            Word word = document.toObject(Word.class);
                            if (word != null) {
                                word.setId(document.getId());
                                wordsMap.put(document.getId(), word);
                            }
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
                        learnedWords.clear();

                        for (DocumentSnapshot document : task.getResult()) {
                            WordLearningStatus status = document.toObject(WordLearningStatus.class);
                            Word word = wordsMap.get(document.getId());

                            // Sadece seviye 1 ve üzeri kelimeleri ekle
                            if (word != null && status != null && status.getLevel() >= 1) {
                                LearnedWord learnedWord = new LearnedWord();
                                learnedWord.setWordId(word.getId());
                                learnedWord.setTurkishWord(word.getTurkishWord());
                                learnedWord.setEnglishWord(word.getEnglishWord());
                                learnedWord.setImagePath(word.getImagePath());
                                learnedWord.setLevel(status.getLevel());
                                learnedWord.setNextReviewDate(status.getNextReviewDate());
                                learnedWord.setLearned(status.isLearned());

                                // Bugün oynanmamış kelimeleri ekle
                                if (!playedWordsToday.contains(word.getId())) {
                                    learnedWords.add(learnedWord);
                                }
                            }
                        }

                        if (learnedWords.isEmpty()) {
                            showNoWordsDialog();
                        } else {
                            // Rastgele bir kelime seç ve oyunu başlat
                            selectRandomWord();
                        }
                    } else {
                        showError("Öğrenme durumları yüklenemedi: " + task.getException().getMessage());
                    }
                });
    }

    private void showNoWordsDialog() {
        Dialog dialog = new Dialog(this, R.style.FullScreenDialogStyle);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_wordle_no_words);
        dialog.setCancelable(false);

        Button buttonMainMenu = dialog.findViewById(R.id.buttonMainMenu);

        buttonMainMenu.setOnClickListener(v -> {
            dialog.dismiss();
            finish(); // Aktiviteyi kapat
        });

        dialog.show();
    }

    private void selectRandomWord() {
        if (learnedWords.isEmpty()) {
            showNoWordsDialog();
            return;
        }

        // Rastgele bir kelime seç
        Random random = new Random();
        int index = random.nextInt(learnedWords.size());
        currentWord = learnedWords.get(index);
        targetWord = currentWord.getEnglishWord().toUpperCase();

        // Kelime bilgilerini göster
        binding.textTurkishWord.setText(currentWord.getTurkishWord());

        // Kelime görselini yükle
        if (currentWord.getImagePath() != null && !currentWord.getImagePath().isEmpty()) {
            Glide.with(this)
                    .load(currentWord.getImagePath())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(binding.imageWord);
        } else {
            binding.imageWord.setImageResource(R.drawable.placeholder_image);
        }

        // Oyun değişkenlerini sıfırla
        currentAttempt = 1;
        binding.textAttempts.setText(String.format(Locale.getDefault(), "Deneme: %d/%d", currentAttempt, MAX_ATTEMPTS));
        currentRowStartIndex = 0;
        currentCellIndex = 0;

        // Klavye durumunu sıfırla
        resetKeyboard();

        // Wordle grid'ini oluştur
        setupWordleGrid();

        // Oyunu aktif et
        gameActive = true;
    }

    private void setupWordleGrid() {
        gridCells = new ArrayList<>();

        // Her deneme için bir satır oluştur (3 deneme)
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            // Her harf için bir hücre oluştur
            for (int j = 0; j < targetWord.length(); j++) {
                gridCells.add(new WordleCell());
            }
        }

        // Grid adaptörünü ayarla
        gridAdapter = new WordleCellAdapter(this, gridCells);
        GridLayoutManager layoutManager = new GridLayoutManager(this, targetWord.length());
        binding.recyclerWordleGrid.setLayoutManager(layoutManager);
        binding.recyclerWordleGrid.setAdapter(gridAdapter);
    }

    @Override
    public void onKeyClick(char key) {
        if (!gameActive) return;

        if (key == '←') {
            // Backspace - son harfi sil
            if (currentCellIndex > currentRowStartIndex) {
                currentCellIndex--;
                gridCells.get(currentCellIndex).setLetter(' ');
                gridCells.get(currentCellIndex).setState(WordleCell.STATE_EMPTY);
                gridAdapter.notifyItemChanged(currentCellIndex);
            }
        } else {
            // Harf ekle (eğer mevcut satır dolmamışsa)
            int rowEndIndex = currentRowStartIndex + targetWord.length();
            if (currentCellIndex < rowEndIndex) {
                gridCells.get(currentCellIndex).setLetter(key);
                gridAdapter.notifyItemChanged(currentCellIndex);
                currentCellIndex++;
            }
        }
    }

    private void submitGuess() {
        if (!gameActive) return;

        // Mevcut satırın tamamlanıp tamamlanmadığını kontrol et
        int rowEndIndex = currentRowStartIndex + targetWord.length();
        if (currentCellIndex < rowEndIndex) {
            Toast.makeText(this, "Lütfen " + targetWord.length() + " harfli bir kelime girin", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mevcut satırdaki harfleri al
        StringBuilder guess = new StringBuilder();
        for (int i = currentRowStartIndex; i < rowEndIndex; i++) {
            guess.append(gridCells.get(i).getLetter());
        }

        // Tahmini değerlendir ve grid'i güncelle
        evaluateGuess(guess.toString());

        // Sonraki deneme veya oyun sonu
        if (guess.toString().equals(targetWord)) {
            // Doğru tahmin
            gameActive = false;
            increaseWordleLevel();
            savePlayedWordToday(currentWord.getWordId());
            showSuccessDialog();
        } else if (currentAttempt >= MAX_ATTEMPTS) {
            // Deneme hakkı bitti
            gameActive = false;
            savePlayedWordToday(currentWord.getWordId());
            showFailureDialog();
        } else {
            // Sonraki deneme
            currentAttempt++;
            binding.textAttempts.setText(String.format(Locale.getDefault(), "Deneme: %d/%d", currentAttempt, MAX_ATTEMPTS));
            currentRowStartIndex = rowEndIndex;
            currentCellIndex = currentRowStartIndex;
        }
    }

    private void evaluateGuess(String guess) {
        // Hedef kelimede her harfin kaç kez geçtiğini say
        Map<Character, Integer> targetLetterCount = new HashMap<>();
        for (char c : targetWord.toCharArray()) {
            targetLetterCount.put(c, targetLetterCount.getOrDefault(c, 0) + 1);
        }

        // Önce doğru yerdeki harfleri işaretle
        char[] guessChars = guess.toCharArray();
        boolean[] marked = new boolean[guessChars.length];

        // İlk geçiş: Doğru yerdeki harfleri işaretle
        for (int i = 0; i < guessChars.length; i++) {
            char guessChar = guessChars[i];
            int gridIndex = currentRowStartIndex + i;

            if (guessChar == targetWord.charAt(i)) {
                // Doğru harf, doğru yer
                gridCells.get(gridIndex).setState(WordleCell.STATE_CORRECT);
                marked[i] = true;

                // Klavyede harfi güncelle
                updateKeyboardState(guessChar, WordleKey.STATE_CORRECT);

                // Hedef kelimede bu harfin sayısını azalt
                targetLetterCount.put(guessChar, targetLetterCount.get(guessChar) - 1);
            }
        }

        // İkinci geçiş: Yanlış yerdeki harfleri işaretle
        for (int i = 0; i < guessChars.length; i++) {
            if (marked[i]) continue; // Zaten işaretlenmiş harfleri atla

            char guessChar = guessChars[i];
            int gridIndex = currentRowStartIndex + i;

            if (targetLetterCount.containsKey(guessChar) && targetLetterCount.get(guessChar) > 0) {
                // Doğru harf, yanlış yer
                gridCells.get(gridIndex).setState(WordleCell.STATE_MISPLACED);

                // Klavyede harfi güncelle (eğer zaten CORRECT değilse)
                updateKeyboardState(guessChar, WordleKey.STATE_MISPLACED);

                // Hedef kelimede bu harfin sayısını azalt
                targetLetterCount.put(guessChar, targetLetterCount.get(guessChar) - 1);
            } else {
                // Yanlış harf
                gridCells.get(gridIndex).setState(WordleCell.STATE_WRONG);

                // Klavyede harfi güncelle
                updateKeyboardState(guessChar, WordleKey.STATE_WRONG);
            }
        }

        // Grid'i güncelle
        gridAdapter.notifyDataSetChanged();
    }

    private void updateKeyboardState(char key, int state) {
        for (WordleKey wordleKey : keyboardKeys) {
            if (wordleKey.getKey() == key) {
                wordleKey.setState(state);
                break;
            }
        }
        keyboardAdapter.notifyDataSetChanged();
    }

    private void increaseWordleLevel() {
        wordleLevel++;
        updateWordleLevelText();

        // Wordle seviyesini Firebase'e kaydet
        if (!userId.isEmpty()) {
            Map<String, Object> data = new HashMap<>();
            data.put("wordleLevel", wordleLevel);
            db.collection("Users").document(userId).update(data)
                    .addOnFailureListener(e -> Log.e(TAG, "Wordle seviyesi güncellenemedi: " + e.getMessage()));
        }
    }
    private void showHowToPlay() {
        Dialog dialog = new Dialog(this, R.style.FullScreenDialogStyle);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.wordle_how_to_play);
        dialog.setCancelable(false);

        Button button = dialog.findViewById(R.id.btnOkay);

        button.setOnClickListener(v -> {
            dialog.dismiss();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("iswordleTutorial", false);
            editor.apply();
        });

        dialog.show();
    }

    private void showSuccessDialog() {
        Dialog dialog = new Dialog(this, R.style.FullScreenDialogStyle);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_wordle_success);
        dialog.setCancelable(false);

        TextView textWordInfo = dialog.findViewById(R.id.textWordInfo);
        TextView textWordMeaning = dialog.findViewById(R.id.textWordMeaning);
        TextView textLevelUp = dialog.findViewById(R.id.textLevelUp);
        Button buttonNextWord = dialog.findViewById(R.id.buttonNextWord);

        textWordInfo.setText(String.format("Kelime: %s", currentWord.getEnglishWord()));
        textWordMeaning.setText(String.format("Anlamı: %s", currentWord.getTurkishWord()));
        textLevelUp.setText(String.format(Locale.getDefault(), "Wordle seviyeniz arttı: %d", wordleLevel));


        buttonNextWord.setOnClickListener(v -> {
            dialog.dismiss();
            // Kelimeyi oynanmış listesinden çıkar
            learnedWords.remove(currentWord);
            if (learnedWords.isEmpty()) {
                showNoWordsDialog();
            } else {
                selectRandomWord();
            }
        });

        dialog.show();
    }

    private void showFailureDialog() {
        Dialog dialog = new Dialog(this, R.style.FullScreenDialogStyle);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_wordle_failure);
        dialog.setCancelable(false);

        TextView textWordInfo = dialog.findViewById(R.id.textWordInfo);
        TextView textWordMeaning = dialog.findViewById(R.id.textWordMeaning);
        Button buttonNextWord = dialog.findViewById(R.id.buttonNextWord);

        textWordInfo.setText(String.format("Kelime: %s", currentWord.getEnglishWord()));
        textWordMeaning.setText(String.format("Anlamı: %s", currentWord.getTurkishWord()));

        buttonNextWord.setOnClickListener(v -> {
            dialog.dismiss();
            // Kelimeyi oynanmış listesinden çıkar
            learnedWords.remove(currentWord);
            if (learnedWords.isEmpty()) {
                showNoWordsDialog();
            } else {
                selectRandomWord();
            }
        });

        dialog.show();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        Log.e(TAG, message);
    }
}