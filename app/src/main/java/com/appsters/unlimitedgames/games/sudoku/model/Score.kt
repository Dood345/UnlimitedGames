package com.appsters.unlimitedgames.games.sudoku.model

import com.appsters.unlimitedgames.games.sudoku.SudokuMenuFragment
import kotlin.math.max

data class Score(
    val difficulty: SudokuMenuFragment.Difficulty,
    val timeInSeconds: Long,
    val mistakes: Int,
    val hintsUsed: Int,
    val timestamp: Long = System.currentTimeMillis()
) {
    /**
     * Calculate high score based on:
     * - Base score from time (faster = better)
     * - Difficulty multiplier
     * - Penalties for mistakes and hints
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
    
    fun getFormattedTime(): String {
        val minutes = timeInSeconds / 60
        val seconds = timeInSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
    
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