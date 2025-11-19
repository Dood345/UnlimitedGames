package com.appsters.unlimitedgames.games.maze

object RunManager {
    // Run State
    var totalMoney: Int = 0
    var totalXP: Int = 0
    var currentLevel: Int = 1
    var roundNumber: Int = 1

    // Base Stats (Can be upgraded)
    var maxStamina: Float = 100f
    var staminaDrainRate: Float = 0.5f // Stamina lost per unit of movement/time
    var baseMaxSpeed: Float = 0.2f
    var baseAcceleration: Float = 0.02f

    fun startNewRun() {
        totalMoney = 0
        totalXP = 0
        currentLevel = 1
        roundNumber = 1
        android.util.Log.d("RunManager", "New Run Started. Round: $roundNumber")
        
        // Reset stats to defaults
        maxStamina = 25f
        staminaDrainRate = 0.5f
        baseMaxSpeed = 0.2f
        baseAcceleration = 0.02f
    }

    fun nextRound() {
        roundNumber++
        android.util.Log.d("RunManager", "Round incremented to: $roundNumber")
        // Potential difficulty scaling here
    }
}
