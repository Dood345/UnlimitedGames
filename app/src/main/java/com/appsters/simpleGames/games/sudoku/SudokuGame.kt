package com.appsters.simpleGames.games.sudoku

import android.content.Context
import com.appsters.simpleGames.games.interfaces.IGame
import com.appsters.simpleGames.games.sudoku.repository.SudokuRepository

class SudokuGame(private val context: Context) : IGame {
    override fun clearUserData() {
        val repository = SudokuRepository(context)
        repository.clearAllData()
    }
}
