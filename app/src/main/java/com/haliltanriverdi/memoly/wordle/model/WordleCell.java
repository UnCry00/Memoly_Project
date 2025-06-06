package com.haliltanriverdi.memoly.wordle.model;

public class WordleCell {
    public static final int STATE_EMPTY = 0;
    public static final int STATE_WRONG = 1;
    public static final int STATE_MISPLACED = 2;
    public static final int STATE_CORRECT = 3;

    private char letter;
    private int state;

    public WordleCell() {
        this.letter = ' ';
        this.state = STATE_EMPTY;
    }

    public WordleCell(char letter, int state) {
        this.letter = letter;
        this.state = state;
    }

    public char getLetter() {
        return letter;
    }

    public void setLetter(char letter) {
        this.letter = letter;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}