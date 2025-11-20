package com.appsters.unlimitedgames.games.maze.model

import com.appsters.unlimitedgames.games.maze.GameConfig

class Player {
    // Base Stats
    var maxStamina: Float = GameConfig.BASE_MAX_STAMINA
    var staminaDrainRate: Float = GameConfig.BASE_STAMINA_DRAIN
    var baseSpeed: Float = GameConfig.BASE_MAX_SPEED
    var baseAcceleration: Float = GameConfig.BASE_ACCELERATION
    var baseVisibility: Int = GameConfig.BASE_VISIBILITY_RADIUS

    // State
    var currentStamina: Float = maxStamina
    val activeEffects = mutableListOf<MazeItem.PowerUp>()

    // Computed Properties
    val effectiveSpeed: Float
        get() {
            var multiplier = 1.0f
            // TODO: Iterate activeEffects and apply speed multipliers
            return baseSpeed * multiplier
        }

    val effectiveVisibility: Int
        get() {
            var bonus = 0
            // TODO: Iterate activeEffects and apply visibility bonuses
            return baseVisibility + bonus
        }

    fun reset() {
        maxStamina = GameConfig.BASE_MAX_STAMINA
        staminaDrainRate = GameConfig.BASE_STAMINA_DRAIN
        baseSpeed = GameConfig.BASE_MAX_SPEED
        baseAcceleration = GameConfig.BASE_ACCELERATION
        baseVisibility = GameConfig.BASE_VISIBILITY_RADIUS
        currentStamina = maxStamina
        activeEffects.clear()
    }

    fun addEffect(effect: MazeItem.PowerUp) {
        activeEffects.add(effect)
    }

    fun tickEffects() {
        // TODO: Decrement duration and remove expired effects
    }
}
