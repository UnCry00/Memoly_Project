package com.haliltanriverdi.memoly.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuizQuestion {
    private Word correctWord;
    private List<Word> options;
    private int selectedOptionIndex = -1;
    private boolean answered = false;

    public QuizQuestion(Word correctWord, List<Word> wrongOptions) {
        this.correctWord = correctWord;

        // Seçenek Oluştur
        options = new ArrayList<>();
        options.add(correctWord); // Doğru Seçenek
        options.addAll(wrongOptions); // Yanlış Seçenekler

        // Seçenekleri karıştır
        Collections.shuffle(options);
    }

    public Word getCorrectWord() {
        return correctWord;
    }

    public List<Word> getOptions() {
        return options;
    }

    public int getCorrectOptionIndex() {
        for (int i = 0; i < options.size(); i++) {
            if (options.get(i).getId().equals(correctWord.getId())) {
                return i;
            }
        }
        return -1;
    }

    public int getSelectedOptionIndex() {
        return selectedOptionIndex;
    }

    public void setSelectedOptionIndex(int selectedOptionIndex) {
        this.selectedOptionIndex = selectedOptionIndex;
        this.answered = true;
    }

    public boolean isAnswered() {
        return answered;
    }

    public boolean isCorrect() {
        // Eğer henüz cevap verilmediyse veya geçersiz bir indeks seçildiyse
        if (!answered || selectedOptionIndex < 0 || selectedOptionIndex >= options.size()) {
            return false;
        }

        // Seçilen seçeneğin ID'si ile doğru kelimenin ID'sini karşılaştır
        return options.get(selectedOptionIndex).getId().equals(correctWord.getId());
    }
}
