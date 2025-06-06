package com.haliltanriverdi.memoly.wordle.model;

public class WordleKey {
    public static final int STATE_UNUSED = 0;
    public static final int STATE_WRONG = 1;
    public static final int STATE_MISPLACED = 2;
    public static final int STATE_CORRECT = 3;

    private char key;

    private int state;
    private boolean isSpecial;

    public WordleKey(char key) {
        this.key = key;
        this.state = STATE_UNUSED;
        this.isSpecial = false;
    }

    public WordleKey(char key, boolean isSpecial) {
        this.key = key;
        this.state = STATE_UNUSED;
        this.isSpecial = isSpecial;
    }

    public char getKey() {
        return key;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        // Yalnızca yeni durum mevcut durumdan 'daha doğru' ise durumu güncelle
            if (state > this.state) {
            this.state = state;
        }
    }

    public boolean isSpecial() {
        return isSpecial;
    }
}