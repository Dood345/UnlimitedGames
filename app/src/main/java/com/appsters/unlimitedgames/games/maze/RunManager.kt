package com.appsters.unlimitedgames.games.maze

object RunManager {
    // Run State
    var totalMoney: Int = 0
    var totalXP: Int = 0
    var currentLevel: Int = 1
    var roundNumber: Int = 1
    var isRunInProgress: Boolean = false

    // Player Instance
    val player = com.appsters.unlimitedgames.games.maze.model.Player()

    fun startNewRun() {
        totalMoney = 0
        totalXP = 0
        currentLevel = 1
        roundNumber = 1
        isRunInProgress = true
        android.util.Log.d("RunManager", "New Run Started. Round: $roundNumber")

        // Reset player stats
        player.reset()

        currentLevelXP = 0
        xpToNextLevel = GameConfig.XP_PER_LEVEL_BASE
    }

    fun nextRound() {
        roundNumber++
        android.util.Log.d("RunManager", "Round incremented to: $roundNumber")
        // Potential difficulty scaling here
    }

    fun addXP(amount: Int): Boolean {
        totalXP += amount
        currentLevelXP += amount
        return checkLevelUp()
    }

    // Internal tracker for bar progress
    var currentLevelXP: Int = 0
    var xpToNextLevel: Int = GameConfig.XP_PER_LEVEL_BASE

    private fun checkLevelUp(): Boolean {
        if (currentLevelXP >= xpToNextLevel) {
            currentLevelXP -= xpToNextLevel
            currentLevel++

            // Apply Bonus
            player.maxStamina += GameConfig.LEVEL_UP_STAMINA_BONUS

            // Calc next threshold
            xpToNextLevel = (xpToNextLevel * GameConfig.XP_SCALING_FACTOR).toInt()

            android.util.Log.d(
                "RunManager",
                "Level Up! New Level: $currentLevel, Max Stamina: ${player.maxStamina}"
            )
            return true
        }
        return false
    }
}
