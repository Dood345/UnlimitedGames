package com.appsters.unlimitedgames.data.gamedata;

public class SudokuData {
    private int[][] puzzle;
    private int[][] solution;
    private long startTime;
    private String difficulty;

    public SudokuData() {
        this.puzzle = new int[9][9];
        this.solution = new int[9][9];
        this.difficulty = "EASY";
    }

    // TODO: Add sudoku game logic
    // public void generatePuzzle(String difficulty) {}
    // public boolean isValidMove(int row, int col, int num) {}
    // public boolean checkSolution() {}
    // public int calculateScore() {}

    // Getters and setters

    public int[][] getPuzzle() {
        return puzzle;
    }

    public void setPuzzle(int[][] puzzle) {
        this.puzzle = puzzle;
    }

    public int[][] getSolution() {
        return solution;
    }

    public void setSolution(int[][] solution) {
        this.solution = solution;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }
}
