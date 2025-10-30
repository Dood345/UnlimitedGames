package com.appsters.unlimitedgames.data.gamedata;

public class Game2048Data {
    private int[][] board;
    private int currentScore;
    private boolean gameOver;

    public Game2048Data() {
        this.board = new int[4][4];
        this.currentScore = 0;
        this.gameOver = false;
    }

    // TODO: Add methods for game logic
    // public void moveUp() {}
    // public void moveDown() {}
    // public void moveLeft() {}
    // public void moveRight() {}
    // public void spawnNewTile() {}
    // public boolean isGameOver() {}

    // Getters and setters
    public int[][] getBoard() {
        return board;
    }

    public void setBoard(int[][] board) {
        this.board = board;
    }

    public int getCurrentScore() {
        return currentScore;
    }

    public void setCurrentScore(int currentScore) {
        this.currentScore = currentScore;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }
}
