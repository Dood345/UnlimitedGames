package com.appsters.unlimitedgames.games.sudoku.model

import com.appsters.unlimitedgames.games.sudoku.SudokuMenuFragment

/**
 * Represents the complete state of a Sudoku game at any given time.
 * This includes the board, difficulty, timer, and player progress.
 *
 * @property board The current state of the Sudoku [Board].
 * @property difficulty The selected [SudokuMenuFragment.Difficulty] for the game.
 * @property startTime The system time when the game was started.
 * @property elapsedTime The total time in milliseconds that the game has been running.
 * @property isPaused `true` if the game is currently paused.
 * @property isCompleted `true` if the puzzle has been successfully solved.
 * @property mistakes The number of incorrect moves the player has made.
 * @property hintsUsed The number of hints the player has used.
 */
data class GameState(
    val board: Board,
    val difficulty: SudokuMenuFragment.Difficulty,
    val startTime: Long = System.currentTimeMillis(),
    var elapsedTime: Long = 0L, // in milliseconds
    var isPaused: Boolean = false,
    var isCompleted: Boolean = false,
    var mistakes: Int = 0,
    var hintsUsed: Int = 0
) {
    /**
     * Formats the elapsed time into a "MM:SS" string.
     */
    fun getFormattedTime(): String {
        val totalSeconds = elapsedTime / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    /**
     * Checks if the current game is ranked (i.e., not in free play mode).
     */
    fun isRanked(): Boolean = difficulty.isRanked()

    /**
     * Creates a [Score] object from the current game state.
     * This can be used to calculate and display the final score.
     */
    fun getScore(): Score {
        return Score(
            difficulty = difficulty,
            timeInSeconds = elapsedTime / 1000,
            mistakes = mistakes,
            hintsUsed = hintsUsed
        )
    }
}
