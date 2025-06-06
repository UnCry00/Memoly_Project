package com.haliltanriverdi.memoly.model;

import java.util.Calendar;
import java.util.Date;

public class WordLearningStatus {
    private String wordId;
    private int level; // 0-7 arası seviye (0: henüz öğrenilmemiş, 7: tamamen öğrenilmiş)
    private int correctAnswerCount; // Arka arkaya doğru bilme sayısı
    private Date nextReviewDate;
    private boolean isLearned;

    public WordLearningStatus() {
        // Firestore için boş constructor
    }

    public WordLearningStatus(String wordId) {
        this.wordId = wordId;
        this.level = 0;
        this.correctAnswerCount = 0;
        this.nextReviewDate = new Date();
        this.isLearned = false;
    }

    public String getWordId() {
        return wordId;
    }

    public void setWordId(String wordId) {
        this.wordId = wordId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getCorrectAnswerCount() {
        return correctAnswerCount;
    }

    public void setCorrectAnswerCount(int correctAnswerCount) {
        this.correctAnswerCount = correctAnswerCount;
    }

    public Date getNextReviewDate() {
        return nextReviewDate;
    }

    public void setNextReviewDate(Date nextReviewDate) {
        this.nextReviewDate = nextReviewDate;
    }

    public boolean isLearned() {
        return isLearned;
    }

    public void setLearned(boolean learned) {
        isLearned = learned;
    }

    public void updateAfterAnswer(boolean isCorrect) {
        Calendar calendar = Calendar.getInstance();

        if (isCorrect) {
            // Doğru cevap verildi
            correctAnswerCount++;

            // Seviye 0'da arka arkaya 2 doğru cevap gerekiyor
            if (level == 0 && correctAnswerCount >= 2) {
                level = 1;
                correctAnswerCount = 0;

                // 1. seviye: 1 gün sonra tekrar
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                nextReviewDate = calendar.getTime();
            }
            // Diğer seviyelerde 1 doğru cevap yeterli
            else if (level > 0) {
                level++;
                correctAnswerCount = 0;

                // Seviyeye göre tekrar tarihini ayarla
                switch (level) {
                    case 2: // 2. seviye: 1 hafta sonra
                        calendar.add(Calendar.WEEK_OF_YEAR, 1);
                        break;
                    case 3: // 3. seviye: 1 ay sonra
                        calendar.add(Calendar.MONTH, 1);
                        break;
                    case 4: // 4. seviye: 3 ay sonra
                        calendar.add(Calendar.MONTH, 3);
                        break;
                    case 5: // 5. seviye: 6 ay sonra
                        calendar.add(Calendar.MONTH, 6);
                        break;
                    case 6: // 6. seviye: 1 yıl sonra
                        calendar.add(Calendar.YEAR, 1);
                        break;
                    case 7: // 7. seviye: tamamen öğrenildi
                        isLearned = true;
                        break;
                }

                nextReviewDate = calendar.getTime();
            }
        } else {
            // Yanlış cevap verildi - seviye sıfırlanır
            level = 0;
            correctAnswerCount = 0;
            isLearned = false;

            // Hemen tekrar göster
            nextReviewDate = calendar.getTime();
        }
    }
}
