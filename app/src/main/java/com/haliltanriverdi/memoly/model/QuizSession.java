package com.haliltanriverdi.memoly.model;

import java.util.ArrayList;
import java.util.List;

public class QuizSession {
    private List<QuizQuestion> questions;
    private int currentQuestionIndex;
    private int correctAnswersCount;

    public QuizSession() {
        questions = new ArrayList<>();
        currentQuestionIndex = 0;
        correctAnswersCount = 0;
    }

    public List<QuizQuestion> getQuestions() {
        return questions;
    }

    public void addQuestion(QuizQuestion question) {
        questions.add(question);
    }

    public int getCurrentQuestionIndex() {
        return currentQuestionIndex;
    }

    public void setCurrentQuestionIndex(int currentQuestionIndex) {
        this.currentQuestionIndex = currentQuestionIndex;
    }

    public int getCorrectAnswersCount() {
        return correctAnswersCount;
    }

    public void incrementCorrectAnswersCount() {
        correctAnswersCount++;
    }

    public boolean hasNextQuestion() {
        return currentQuestionIndex < questions.size() - 1;
    }

    public QuizQuestion getCurrentQuestion() {
        if (currentQuestionIndex >= 0 && currentQuestionIndex < questions.size()) {
            return questions.get(currentQuestionIndex);
        }
        return null;
    }

    public QuizQuestion getNextQuestion() {
        if (hasNextQuestion()) {
            currentQuestionIndex++;
            return getCurrentQuestion();
        }
        return null;
    }

    public int getTotalQuestions() {
        return questions.size();
    }
}
