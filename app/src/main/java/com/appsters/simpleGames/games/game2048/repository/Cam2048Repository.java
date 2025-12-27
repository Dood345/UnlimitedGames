package com.appsters.simpleGames.games.game2048.repository;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Cam2048Repository {

    private static final String PREFS_NAME = "Game2048Prefs";
    private static final String HIGH_SCORE_KEY_PREFIX = "high_score_";
    private static final String SAVED_BOARD_KEY_PREFIX = "saved_board_";
    private static final String SAVED_SCORE_KEY_PREFIX = "saved_score_";

    private final SharedPreferences sharedPreferences;

    public Cam2048Repository(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private FirebaseUser getCurrentUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public int getHighScore() {
        FirebaseUser user = getCurrentUser();
        if (user == null)
            return 0;
        return sharedPreferences.getInt(HIGH_SCORE_KEY_PREFIX + user.getUid(), 0);
    }

    public void saveHighScore(int score) {
        FirebaseUser user = getCurrentUser();
        if (user == null)
            return;
        int currentHighScore = getHighScore();
        if (score > currentHighScore) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(HIGH_SCORE_KEY_PREFIX + user.getUid(), score);
            editor.apply();
        }
    }

    public void saveGameState(int[][] board, int score) {
        FirebaseUser user = getCurrentUser();
        if (user == null)
            return;
        SharedPreferences.Editor editor = sharedPreferences.edit();

        StringBuilder boardString = new StringBuilder();
        for (int[] row : board) {
            for (int cell : row) {
                boardString.append(cell).append(",");
            }
        }

        editor.putString(SAVED_BOARD_KEY_PREFIX + user.getUid(), boardString.toString());
        editor.putInt(SAVED_SCORE_KEY_PREFIX + user.getUid(), score);
        editor.apply();
    }

    public int[][] getSavedBoard() {
        FirebaseUser user = getCurrentUser();
        if (user == null)
            return null;
        String boardString = sharedPreferences.getString(SAVED_BOARD_KEY_PREFIX + user.getUid(), null);
        if (boardString == null || boardString.isEmpty())
            return null;

        String[] values = boardString.split(",");
        if (values.length != 16)
            return null;

        int[][] board = new int[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                board[i][j] = Integer.parseInt(values[i * 4 + j]);
            }
        }
        return board;
    }

    public int getSavedScore() {
        FirebaseUser user = getCurrentUser();
        if (user == null)
            return -1;
        return sharedPreferences.getInt(SAVED_SCORE_KEY_PREFIX + user.getUid(), -1);
    }

    public void clearSavedState() {
        FirebaseUser user = getCurrentUser();
        if (user == null)
            return;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(SAVED_BOARD_KEY_PREFIX + user.getUid());
        editor.remove(SAVED_SCORE_KEY_PREFIX + user.getUid());
        editor.apply();
    }

    public void clearHighScore() {
        FirebaseUser user = getCurrentUser();
        if (user == null)
            return;
        android.util.Log.d("Cam2048Repository", "Clearing high score for user: " + user.getUid());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(HIGH_SCORE_KEY_PREFIX + user.getUid());
        editor.apply();
    }
}
