package com.appsters.unlimitedgames.games.sudoku.model

data class Cell(
    val row: Int,
    val col: Int,
    var value: Int = 0,           // 0 means empty
    val isFixed: Boolean = false, // True if part of initial puzzle
    var notes: MutableSet<Int> = mutableSetOf() // For pencil marks (1-9)
) {
    fun isEmpty(): Boolean = value == 0
    
    fun getBox(): Int {
        // Returns which 3x3 box this cell belongs to (0-8)
        return (row / 3) * 3 + (col / 3)
    }
}
