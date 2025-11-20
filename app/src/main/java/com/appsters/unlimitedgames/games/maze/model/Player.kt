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
    var skillPoints: Int = 0
    var isWallSmashUnlocked: Boolean = false
    private val activeEffects = mutableListOf<ActiveEffect>()

    data class ActiveEffect(val type: PowerUpType, var remainingDuration: Long)

    // Computed Properties
    val effectiveSpeed: Float
        get() {
            var multiplier = 1.0f
            for (effect in activeEffects) {
                if (effect.type == PowerUpType.SPEED_BOOST) {
                    multiplier += 0.5f // 50% speed boost
                }
            }
            return baseSpeed * multiplier
        }

    val effectiveVisibility: Int
        get() {
            var bonus = 0
            for (effect in activeEffects) {
                if (effect.type == PowerUpType.VISION_EXPAND) {
                    bonus += 2 // +2 tiles visibility
                }
            }
            return baseVisibility + bonus
        }

    fun reset() {
        maxStamina = GameConfig.BASE_MAX_STAMINA
        staminaDrainRate = GameConfig.BASE_STAMINA_DRAIN
        baseSpeed = GameConfig.BASE_MAX_SPEED
        baseAcceleration = GameConfig.BASE_ACCELERATION
        baseVisibility = GameConfig.BASE_VISIBILITY_RADIUS
        currentStamina = maxStamina
        skillPoints = 0
        isWallSmashUnlocked = false
        activeEffects.clear()
    }

    fun addEffect(effect: MazeItem.PowerUp) {
        // Check if effect of same type exists
        val existing = activeEffects.find { it.type == effect.type }
        if (existing != null) {
            // Extend duration (Max of current remaining or new duration? Or just add?)
            // Let's just reset to max duration for simplicity
            existing.remainingDuration = kotlin.math.max(existing.remainingDuration, effect.durationMs)
        } else {
            activeEffects.add(ActiveEffect(effect.type, effect.durationMs))
        }
    }

    fun clearEffects() {
        activeEffects.clear()
    }

    fun tickEffects() {
        val iterator = activeEffects.iterator()
        while (iterator.hasNext()) {
            val effect = iterator.next()
            effect.remainingDuration -= 16 // Approx 60 FPS (16ms per frame)
            if (effect.remainingDuration <= 0) {
                iterator.remove()
            }
        }
    }
}
