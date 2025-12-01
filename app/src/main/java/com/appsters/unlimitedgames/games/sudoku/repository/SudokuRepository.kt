package com.appsters.unlimitedgames.games.sudoku.repository

import android.content.Context
import android.graphics.Color
import com.appsters.unlimitedgames.games.sudoku.SudokuMenuFragment
import com.appsters.unlimitedgames.games.sudoku.model.Score

/**
 * A repository for handling Sudoku game data, such as high scores and user preferences.
 * This class uses SharedPreferences for local data persistence.
 */
class SudokuRepository(context: Context) {

    private val prefs = context.getSharedPreferences("sudoku_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val HIGH_SCORE_KEY_PREFIX = "high_score_"
        private const val LAST_COLOR_KEY = "last_color"
        private const val SAVED_GAME_STATE_KEY = "saved_game_state"
    }

    /**
     * Saves a new score if it's higher than the existing high score for that difficulty.
     */
    fun saveHighScore(score: Score) {
        val newScoreValue = score.calculateScore()
        val key = HIGH_SCORE_KEY_PREFIX + score.difficulty.name
        val currentHighScore = prefs.getInt(key, 0)

        if (newScoreValue > currentHighScore) {
            prefs.edit().putInt(key, newScoreValue).apply()
        }
    }

    /**
     * Retrieves the high score for a specific difficulty level.
     */
    fun getHighScore(difficulty: SudokuMenuFragment.Difficulty): Int {
        val key = HIGH_SCORE_KEY_PREFIX + difficulty.name
        return prefs.getInt(key, 0)
    }

    /**
     * Saves the last color selected by the player.
     */
    fun saveLastColor(color: Int) {
        prefs.edit().putInt(LAST_COLOR_KEY, color).apply()
    }

    /**
     * Retrieves the last color selected by the player.
     * @return The saved color, or [Color.BLACK] if none is saved.
     */
    fun getLastColor(): Int {
        return prefs.getInt(LAST_COLOR_KEY, Color.BLACK)
    }

    /**
     * Saves the current game state to SharedPreferences.
     */
    fun saveGameState(gameState: com.appsters.unlimitedgames.games.sudoku.model.GameState, difficulty: SudokuMenuFragment.Difficulty) {
        val json = com.google.gson.Gson().toJson(gameState)
        val key = SAVED_GAME_STATE_KEY + "_" + difficulty.name
        prefs.edit().putString(key, json).apply()
    }

    /**
     * Retrieves the saved game state from SharedPreferences.
     * @return The saved [GameState], or null if no game is saved.
     */
    fun getSavedGameState(difficulty: SudokuMenuFragment.Difficulty): com.appsters.unlimitedgames.games.sudoku.model.GameState? {
        val key = SAVED_GAME_STATE_KEY + "_" + difficulty.name
        val json = prefs.getString(key, null) ?: return null
        return try {
            com.google.gson.Gson().fromJson(json, com.appsters.unlimitedgames.games.sudoku.model.GameState::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Checks if there is a saved game state.
     */
    fun hasSavedGame(difficulty: SudokuMenuFragment.Difficulty): Boolean {
        val key = SAVED_GAME_STATE_KEY + "_" + difficulty.name
        return prefs.contains(key)
    }

    /**
     * Clears the saved game state.
     */
    fun clearSavedGame(difficulty: SudokuMenuFragment.Difficulty) {
        val key = SAVED_GAME_STATE_KEY + "_" + difficulty.name
        prefs.edit().remove(key).apply()
    }
}