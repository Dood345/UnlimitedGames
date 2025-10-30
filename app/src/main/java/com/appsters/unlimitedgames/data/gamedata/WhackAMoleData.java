package com.appsters.unlimitedgames.data.gamedata;

import java.util.ArrayList;
import java.util.List;

public class WhackAMoleData {
    private List<Boolean> molePositions;
    private int currentScore;
    private long timeRemaining;
    private int lives;

    public WhackAMoleData() {
        this.molePositions = new ArrayList<>();
        this.currentScore = 0;
        this.lives = 3;
    }

    // TODO: Add whack-a-mole logic
    // public void spawnMole() {}
    // public void hitMole(int position) {}
    // public boolean isGameOver() {}

    // Getters and setters

    public List<Boolean> getMolePositions() {
        return molePositions;
    }

    public void setMolePositions(List<Boolean> molePositions) {
        this.molePositions = molePositions;
    }

    public int getCurrentScore() {
        return currentScore;
    }

    public void setCurrentScore(int currentScore) {
        this.currentScore = currentScore;
    }

    public long getTimeRemaining() {
        return timeRemaining;
    }

    public void setTimeRemaining(long timeRemaining) {
        this.timeRemaining = timeRemaining;
    }

    public int getLives() {
        return lives;
    }

    public void setLives(int lives) {
        this.lives = lives;
    }
}
