package com.haliltanriverdi.memoly.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Word implements Serializable {
    private String id;
    private String turkishWord;
    private String englishWord;
    private List<String> exampleSentences;
    private String imagePath;
    private String userId;
    
    public Word() {
        exampleSentences = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            exampleSentences.add("");
        }
    }
    
    public Word(String id, String turkishWord, String englishWord, List<String> exampleSentences, String imagePath, String userId) {
        this.id = id;
        this.turkishWord = turkishWord;
        this.englishWord = englishWord;
        this.exampleSentences = exampleSentences;
        this.imagePath = imagePath;
        this.userId = userId;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getTurkishWord() {
        return turkishWord;
    }
    
    public void setTurkishWord(String turkishWord) {
        this.turkishWord = turkishWord;
    }
    
    public String getEnglishWord() {
        return englishWord;
    }
    
    public void setEnglishWord(String englishWord) {
        this.englishWord = englishWord;
    }
    
    public List<String> getExampleSentences() {
        return exampleSentences;
    }
    
    public void setExampleSentences(List<String> exampleSentences) {
        this.exampleSentences = exampleSentences;
    }
    
    public String getImagePath() {
        return imagePath;
    }
    
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
}