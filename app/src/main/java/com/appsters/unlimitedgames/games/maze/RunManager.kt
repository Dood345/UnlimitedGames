package com.appsters.unlimitedgames.games.maze

object RunManager {
    // Run State
    var totalMoney: Int = 0
    var totalXP: Int = 0
    var currentLevel: Int = 1
    var roundNumber: Int = 1
    var isRunInProgress: Boolean = false

    // Base Stats (Can be upgraded)
    var maxStamina: Float = GameConfig.BASE_MAX_STAMINA
    var staminaDrainRate: Float = GameConfig.BASE_STAMINA_DRAIN
    var baseMaxSpeed: Float = GameConfig.BASE_MAX_SPEED
    var baseAcceleration: Float = GameConfig.BASE_ACCELERATION

    fun startNewRun() {
        totalMoney = 0
        totalXP = 0
        currentLevel = 1
        roundNumber = 1
        isRunInProgress = true
        android.util.Log.d("RunManager", "New Run Started. Round: $roundNumber")
        
        // Reset stats to defaults
        maxStamina = GameConfig.BASE_MAX_STAMINA
        staminaDrainRate = GameConfig.BASE_STAMINA_DRAIN
        baseMaxSpeed = GameConfig.BASE_MAX_SPEED
        baseAcceleration = GameConfig.BASE_ACCELERATION
    }

    fun nextRound() {
        roundNumber++
        android.util.Log.d("RunManager", "Round incremented to: $roundNumber")
        // Potential difficulty scaling here
    }
}
