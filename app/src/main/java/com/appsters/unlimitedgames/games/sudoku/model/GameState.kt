package com.appsters.unlimitedgames.games.sudoku.model

import com.appsters.unlimitedgames.games.sudoku.SudokuMenuFragment

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
    fun getFormattedTime(): String {
        val totalSeconds = elapsedTime / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    fun isRanked(): Boolean = difficulty.isRanked()

    fun getScore(): Score {
        return Score(
            difficulty = difficulty,
            timeInSeconds = elapsedTime / 1000,
            mistakes = mistakes,
            hintsUsed = hintsUsed
        )
    }
}