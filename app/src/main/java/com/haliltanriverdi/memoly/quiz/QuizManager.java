package com.haliltanriverdi.memoly.quiz;

import android.content.Context;
import android.util.Log;

import com.haliltanriverdi.memoly.model.QuizQuestion;
import com.haliltanriverdi.memoly.model.QuizSession;
import com.haliltanriverdi.memoly.model.Word;
import com.haliltanriverdi.memoly.model.WordLearningStatus;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuizManager {
    private static final String TAG = "QuizManager";
    private final FirebaseFirestore db;
    private final String userId;
    private List<Word> allWords;
    private List<Word> availableWords;
    private Map<String, WordLearningStatus> learningStatusMap;
    private QuizSession currentSession;
    private int questionCount = 10; // Varsayılan soru sayısı

    public interface OnQuizReadyListener {
        void onQuizReady(QuizSession session);
        void onNotEnoughWords(int availableCount, int requiredCount);
        void onError(String errorMessage);
    }

    public interface OnQuizResultSavedListener {
        void onResultSaved();
        void onError(String errorMessage);
    }

    public interface OnAvailableWordsCountListener {
        void onCountReady(int count);
        void onError(String errorMessage);
    }

    public QuizManager(Context context) {
        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userId = user != null ? user.getUid() : "";
        allWords = new ArrayList<>();
        availableWords = new ArrayList<>();
        learningStatusMap = new HashMap<>();
    }

    public void setQuestionCount(int count) {
        this.questionCount = count;
    }

    public int getQuestionCount() {
        return questionCount;
    }

    public void prepareQuiz(final OnQuizReadyListener listener) {
        if (userId.isEmpty()) {
            listener.onError("Kullanıcı oturum açmamış");
            return;
        }

        loadWords(new OnWordsLoadedListener() {
            @Override
            public void onWordsLoaded(List<Word> words, Map<String, WordLearningStatus> statusMap) {
                allWords = words;
                learningStatusMap = statusMap;

                // Bugün gösterilmesi gereken kelimeleri filtrele
                filterAvailableWords();

                Log.d(TAG, "Kullanılabilir kelime sayısı: " + availableWords.size());

                if (availableWords.size() < questionCount) {
                    listener.onNotEnoughWords(availableWords.size(), questionCount);
                    return;
                }

                // Quiz oturumu oluştur
                createQuizSession();
                listener.onQuizReady(currentSession);
            }

            @Override
            public void onError(String errorMessage) {
                listener.onError(errorMessage);
            }
        });
    }

    public void getAvailableWordsCount(final OnAvailableWordsCountListener listener) {
        if (userId.isEmpty()) {
            listener.onError("Kullanıcı oturum açmamış");
            return;
        }

        loadWords(new OnWordsLoadedListener() {
            @Override
            public void onWordsLoaded(List<Word> words, Map<String, WordLearningStatus> statusMap) {
                allWords = words;
                learningStatusMap = statusMap;

                // Bugün gösterilmesi gereken kelimeleri filtrele
                filterAvailableWords();

                listener.onCountReady(availableWords.size());
            }

            @Override
            public void onError(String errorMessage) {
                listener.onError(errorMessage);
            }
        });
    }

    private void filterAvailableWords() {
        availableWords.clear();
        Date now = new Date();

        for (Word word : allWords) {
            WordLearningStatus status = learningStatusMap.get(word.getId());

            // Eğer kelime için öğrenme durumu yoksa veya bugün gösterilmesi gerekiyorsa
            if (status == null || !status.isLearned() && !now.before(status.getNextReviewDate())) {
                availableWords.add(word);
            }
        }
    }

    private void createQuizSession() {
        currentSession = new QuizSession();

        // Kullanılabilir kelimelerden rastgele seç
        Collections.shuffle(availableWords);
        int questionSize = Math.min(questionCount, availableWords.size());
        List<Word> selectedWords = availableWords.subList(0, questionSize);

        for (Word targetWord : selectedWords) {
            // Yanlış seçenekler için kelimeler seç
            List<Word> wrongOptions = new ArrayList<>(allWords);
            wrongOptions.remove(targetWord);
            Collections.shuffle(wrongOptions);

            // En fazla 3 yanlış seçenek al
            List<Word> wrongOptionsSubset = wrongOptions.subList(0, Math.min(3, wrongOptions.size()));

            // QuizQuestion oluştur
            QuizQuestion question = new QuizQuestion(targetWord, wrongOptionsSubset);
            currentSession.addQuestion(question);
        }
    }

    public void saveQuizResults(final OnQuizResultSavedListener listener) {
        if (currentSession == null || userId.isEmpty()) {
            listener.onError("Geçerli bir quiz oturumu bulunamadı");
            return;
        }

        final List<QuizQuestion> questions = currentSession.getQuestions();
        final int totalQuestions = questions.size();
        final List<String> processedQuestions = new ArrayList<>();

        for (final QuizQuestion question : questions) {
            if (!question.isAnswered()) continue;

            final String wordId = question.getCorrectWord().getId();
            final boolean isCorrect = question.isCorrect();

            // Öğrenme durumunu güncelle
            updateLearningStatus(wordId, isCorrect, new OnStatusUpdatedListener() {
                @Override
                public void onStatusUpdated() {
                    processedQuestions.add(wordId);

                    if (processedQuestions.size() == totalQuestions) {
                        listener.onResultSaved();
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    listener.onError(errorMessage);
                }
            });
        }
    }

    private void updateLearningStatus(final String wordId, final boolean isCorrect, final OnStatusUpdatedListener listener) {
        final DocumentReference statusRef = db.collection("users").document(userId)
                .collection("learningStatus").document(wordId);

        statusRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                WordLearningStatus status;

                if (document.exists()) {
                    status = document.toObject(WordLearningStatus.class);
                } else {
                    status = new WordLearningStatus(wordId);
                }

                // Öğrenme durumunu güncelle
                status.updateAfterAnswer(isCorrect);

                // Firestore'a kaydet
                statusRef.set(status)
                        .addOnSuccessListener(aVoid -> listener.onStatusUpdated())
                        .addOnFailureListener(e -> listener.onError("Öğrenme durumu güncellenemedi: " + e.getMessage()));
            } else {
                listener.onError("Öğrenme durumu alınamadı: " + task.getException().getMessage());
            }
        });
    }

    private interface OnWordsLoadedListener {
        void onWordsLoaded(List<Word> words, Map<String, WordLearningStatus> statusMap);
        void onError(String errorMessage);
    }

    private interface OnStatusUpdatedListener {
        void onStatusUpdated();
        void onError(String errorMessage);
    }

    private void loadWords(final OnWordsLoadedListener listener) {
        final List<Word> words = new ArrayList<>();
        final Map<String, WordLearningStatus> statusMap = new HashMap<>();

        // Kelimeleri yükle
        db.collection("users").document(userId).collection("words")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Word word = document.toObject(Word.class);
                            word.setId(document.getId());
                            words.add(word);
                        }

                        // Öğrenme durumlarını yükle
                        loadLearningStatus(words, statusMap, listener);
                    } else {
                        listener.onError("Kelimeler yüklenemedi: " + task.getException().getMessage());
                    }
                });
    }

    private void loadLearningStatus(final List<Word> words, final Map<String, WordLearningStatus> statusMap,
                                    final OnWordsLoadedListener listener) {
        if (words.isEmpty()) {
            listener.onWordsLoaded(words, statusMap);
            return;
        }

        db.collection("users").document(userId).collection("learningStatus")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            WordLearningStatus status = document.toObject(WordLearningStatus.class);
                            statusMap.put(document.getId(), status);
                        }

                        listener.onWordsLoaded(words, statusMap);
                    } else {
                        listener.onError("Öğrenme durumları yüklenemedi: " + task.getException().getMessage());
                    }
                });
    }

    public QuizSession getCurrentSession() {
        return currentSession;
    }
}
