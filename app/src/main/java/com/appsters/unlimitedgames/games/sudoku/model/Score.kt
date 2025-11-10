package com.appsters.unlimitedgames.games.sudoku.model

import com.appsters.unlimitedgames.games.sudoku.SudokuMenuFragment
import kotlin.math.max

/**
 * Represents the final score of a completed Sudoku game.
 * It provides a method to calculate the score based on difficulty, time, mistakes, and hints.
 *
 * @property difficulty The difficulty of the game.
 * @property timeInSeconds The total time taken to complete the puzzle.
 * @property mistakes The number of incorrect moves made.
 * @property hintsUsed The number of hints used.
 * @property timestamp The time when the score was recorded.
 */
data class Score(
    val difficulty: SudokuMenuFragment.Difficulty,
    val timeInSeconds: Long,
    val mistakes: Int,
    val hintsUsed: Int,
    val timestamp: Long = System.currentTimeMillis()
) {
    /**
     * Calculates the final score based on a formula that rewards speed and accuracy,
     * and applies a multiplier for higher difficulties.
     * Free-play games do not receive a score.
     *
     * @return The calculated score, with a minimum of 0.
     */
    fun calculateScore(): Int {
        if (!difficulty.isRanked()) return 0
        
        // Base score: 10000 points, minus time penalty
        // Each second reduces score by 1 point
        val baseScore = 10000 - timeInSeconds.toInt()
        
        // Apply difficulty multiplier
        val difficultyScore = (baseScore * difficulty.multiplier).toInt()
        
        // Penalties
        val mistakePenalty = mistakes * 50
        val hintPenalty = hintsUsed * 100
        
        // Final score (minimum 0)
        return maxOf(0, difficultyScore - mistakePenalty - hintPenalty)
    }
    
    /**
     * Formats the time taken into a "MM:SS" string.
     */
    fun getFormattedTime(): String {
        val minutes = timeInSeconds / 60
        val seconds = timeInSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
    
    /**
     * Provides a detailed breakdown of how the score was calculated.
     * This is useful for displaying on a results screen.
     *
     * @return A formatted string explaining the score calculation.
     */
    fun getScoreBreakdown(): String {
        if (!difficulty.isRanked()) return "Free Play - No Score"
        
        val baseScore = 10000 - timeInSeconds.toInt()
        val difficultyScore = (baseScore * difficulty.multiplier).toInt()
        val mistakePenalty = mistakes * 50
        val hintPenalty = hintsUsed * 100
        val finalScore = calculateScore()
        
        return """
            Time: ${getFormattedTime()} (${timeInSeconds}s)
            Base Score: $baseScore
            Difficulty: ${difficulty.getDisplayName()} (${difficulty.multiplier}x)
            Score after difficulty: $difficultyScore
            Mistakes: -$mistakePenalty (${mistakes} x 50)
            Hints: -$hintPenalty (${hintsUsed} x 100)
            Final Score: $finalScore
        """.trimIndent()
    }
}
