package com.appsters.unlimitedgames.games.sudoku

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appsters.unlimitedgames.games.sudoku.model.Cell
import com.appsters.unlimitedgames.games.sudoku.model.GameState
import com.appsters.unlimitedgames.games.sudoku.model.PuzzleGenerator
import com.appsters.unlimitedgames.games.sudoku.util.SudokuTimer
import kotlinx.coroutines.launch

class SudokuViewModel : ViewModel() {

    private val _gameState = MutableLiveData<GameState>()
    val gameState: LiveData<GameState> = _gameState

    private val _timer = MutableLiveData<String>()
    val timer: LiveData<String> = _timer

    private val _selectedCell = MutableLiveData<Cell?>()
    val selectedCell: LiveData<Cell?> = _selectedCell

    private lateinit var sudokuTimer: SudokuTimer

    fun createNewGame(difficulty: SudokuMenuFragment.Difficulty) {
        viewModelScope.launch {
            val board = PuzzleGenerator.generate(difficulty)
            val newGameState = GameState(board, difficulty)
            _gameState.postValue(newGameState)
            _selectedCell.postValue(null)
            startTimer(newGameState)
        }
    }

    private fun startTimer(gameState: GameState) {
        sudokuTimer = SudokuTimer { elapsedTime ->
            gameState.elapsedTime = elapsedTime
            _timer.postValue(gameState.getFormattedTime())
        }
        sudokuTimer.start()
    }

    fun onCellSelected(row: Int, col: Int) {
        val currentBoard = _gameState.value?.board ?: return
        _selectedCell.postValue(currentBoard.getCell(row, col))
    }

    fun enterValue(number: Int) {
        val selected = _selectedCell.value ?: return
        val currentBoard = _gameState.value?.board ?: return

        if (selected.isFixed) return

        val currentGameState = _gameState.value!!

        if (currentBoard.isValid(selected.row, selected.col, number)) {
            currentBoard.setCell(selected.row, selected.col, number)
            if (currentBoard.isSolved()) {
                currentGameState.isCompleted = true
                sudokuTimer.pause()
            }
        } else {
            currentGameState.mistakes++
        }

        _gameState.postValue(currentGameState)
    }
    
    fun clearCell() {
        val selected = _selectedCell.value ?: return
        val currentBoard = _gameState.value?.board ?: return
        val currentGameState = _gameState.value!!

        if (selected.isFixed) return

        // Set the cell's value back to 0
        currentBoard.setCell(selected.row, selected.col, 0)

        // Post the updated state to the UI
        _gameState.postValue(currentGameState)
    }

    override fun onCleared() {
        super.onCleared()
        if (::sudokuTimer.isInitialized) {
            sudokuTimer.pause()
        }
    }
}
