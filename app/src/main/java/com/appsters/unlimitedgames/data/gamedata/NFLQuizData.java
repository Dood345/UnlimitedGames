package com.appsters.unlimitedgames.data.gamedata;

import java.util.ArrayList;
import java.util.List;

public class NFLQuizData {
    private String player1;
    private String player2;
    private String correctAnswer;
    private List<String> options;
    private int currentScore;
    private int questionsAnswered;

    public NFLQuizData() {
        this.options = new ArrayList<>();
        this.currentScore = 0;
        this.questionsAnswered = 0;
    }

    // TODO: Add quiz logic
    // public void generateQuestion() {}
    // public boolean checkAnswer(String answer) {}
    // public void nextQuestion() {}

    // Getters and setters

    public String getPlayer1() {
        return player1;
    }

    public void setPlayer1(String player1) {
        this.player1 = player1;
    }

    public String getPlayer2() {
        return player2;
    }

    public void setPlayer2(String player2) {
        this.player2 = player2;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public int getCurrentScore() {
        return currentScore;
    }

    public void setCurrentScore(int currentScore) {
        this.currentScore = currentScore;
    }

    public int getQuestionsAnswered() {
        return questionsAnswered;
    }

    public void setQuestionsAnswered(int questionsAnswered) {
        this.questionsAnswered = questionsAnswered;
    }
}
