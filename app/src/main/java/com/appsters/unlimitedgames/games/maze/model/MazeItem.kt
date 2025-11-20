package com.appsters.unlimitedgames.games.maze.model

sealed class MazeItem(val x: Int, val y: Int) {
    data class Artifact(val col: Int, val row: Int, val value: Int) : MazeItem(col, row)
    data class XpOrb(val col: Int, val row: Int, val xpValue: Int) : MazeItem(col, row)
    
    data class PowerUp(val col: Int, val row: Int, val type: PowerUpType, val durationMs: Long) : MazeItem(col, row)
}

enum class PowerUpType {
    STAMINA_REFILL,
    SPEED_BOOST,
    VISION_EXPAND
}
