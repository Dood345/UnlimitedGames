package com.appsters.simpleGames.games.sudoku.model

import com.appsters.simpleGames.games.sudoku.SudokuMenuFragment

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
    val isRanked: Boolean,
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
        if (!isRanked) return 0
        val timeScore = 10000 - timeInSeconds.toInt()
        val mistakePenalty = mistakes * 50
        val finalScore = (timeScore - mistakePenalty) * difficulty.multiplier

        //can be negative now
        return finalScore.toInt()
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
     * Provides a detailed breakdown of how the score was calculated, formatted like a receipt.
     * This is useful for displaying on a results screen.
     *
     * @return A formatted string explaining the score calculation.
     */
    fun getScoreBreakdown(): String {
        if (!isRanked) {
            return Quotes.list.random()
        }

        val timeScore = 10000 - timeInSeconds.toInt()
        val mistakePenalty = mistakes * 50
        val finalScore = (timeScore - mistakePenalty) * difficulty.multiplier
        val difficultyBonus = finalScore.toInt() - timeScore + mistakePenalty


        fun formatLine(label: String, value: Any): String {
            val valueStr = value.toString()
            val lineLength = 35
            val padding = ".".repeat(maxOf(0, lineLength - label.length - valueStr.length))
            return "$label$padding$value"
        }

        val separator = ".".repeat(35)

        return """
            ${formatLine("Time Bonus", timeScore)}
            ${formatLine("Mistake Penalty (${mistakes}x50)", "-$mistakePenalty")}
            ${formatLine("Difficulty Bonus (x${difficulty.multiplier})", "+$difficultyBonus")}
            $separator
            ${formatLine("Total Score", calculateScore())}
        """.trimIndent()
    }
}
