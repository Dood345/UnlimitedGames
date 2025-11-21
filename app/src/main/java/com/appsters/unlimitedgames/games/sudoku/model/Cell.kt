package com.appsters.unlimitedgames.games.sudoku.model

/**
 * Represents a single cell on the Sudoku board.
 *
 * @property row The row index (0-8) of the cell.
 * @property col The column index (0-8) of the cell.
 * @property value The number currently in the cell (1-9), or 0 if it's empty.
 * @property isFixed `true` if the cell is part of the initial puzzle and cannot be changed.
 * @property notes A set of "pencil marks" or candidate numbers that the user might enter.
 */
data class Cell(
    val row: Int,
    val col: Int,
    var value: Int = 0,           // 0 means empty
    var isFixed: Boolean = false, // True if part of initial puzzle
    var notes: MutableSet<Int> = mutableSetOf() // For pencil marks (1-9)
) {
    /**
     * Checks if the cell is empty (contains a 0).
     */
    fun isEmpty(): Boolean = value == 0
    
    /**
     * Calculates which 3x3 box this cell belongs to.
     * @return The index of the box (0-8), from top-left to bottom-right.
     */
    fun getBox(): Int {
        return (row / 3) * 3 + (col / 3)
    }
}
