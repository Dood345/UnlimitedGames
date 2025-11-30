package com.appsters.unlimitedgames.games.sudoku.model

/**
 * Represents a single move made by the player.
 * This is used to implement undo/redo functionality.
 *
 * @property row The row of the cell that was changed.
 * @property col The column of the cell that was changed.
 * @property previousValue The value of the cell before the move.
 * @property newValue The new value of the cell after the move.
 * @property timestamp The time when the move was made.
 */
data class Move(
    val row: Int,
    val col: Int,
    val previousValue: Int,
    val newValue: Int,
    val timestamp: Long = System.currentTimeMillis()
)
