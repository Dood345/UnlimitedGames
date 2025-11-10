package com.appsters.unlimitedgames.games.sudoku.model

data class Move(
    val row: Int,
    val col: Int,
    val previousValue: Int,
    val newValue: Int,
    val timestamp: Long = System.currentTimeMillis()
)
