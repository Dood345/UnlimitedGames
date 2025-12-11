package com.appsters.unlimitedgames.games.sudoku

import android.content.Context
import com.appsters.unlimitedgames.games.interfaces.IGame
import com.appsters.unlimitedgames.games.sudoku.repository.SudokuRepository

class SudokuGame(private val context: Context) : IGame {
    override fun clearUserData() {
        val repository = SudokuRepository(context)
        repository.clearAllData()
    }
}
