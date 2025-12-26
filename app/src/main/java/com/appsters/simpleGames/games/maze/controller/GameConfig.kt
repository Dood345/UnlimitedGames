package com.appsters.simpleGames.games.maze.controller

object GameConfig {
    // Base Stats
    const val BASE_MAX_STAMINA = 100f
    const val BASE_STAMINA_DRAIN = 0.5f
    const val BASE_MAX_SPEED = 0.2f
    const val BASE_ACCELERATION = 0.02f
    const val BASE_VISIBILITY_RADIUS = 4

    // Upgrade Increments
    const val UPGRADE_STAMINA_AMOUNT = 10f
    const val UPGRADE_SPEED_AMOUNT = 0.05f
    const val UPGRADE_EFFICIENCY_MULTIPLIER = 0.9f // 10% reduction in drain

    // Costs
    const val COST_UPGRADE_STAMINA = 50
    const val COST_UPGRADE_SPEED = 100
    const val COST_UPGRADE_EFFICIENCY = 75

    // XP & Leveling
    const val XP_PER_LEVEL_BASE = 30
    const val XP_SCALING_FACTOR = 1.5f // Next level needs 1.5x previous XP
    const val LEVEL_UP_STAMINA_BONUS = 5f

    // Gameplay
    const val MAZE_WIDTH = 15
    const val MAZE_HEIGHT = 15
}