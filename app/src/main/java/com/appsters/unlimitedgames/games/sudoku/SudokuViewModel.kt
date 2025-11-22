package com.appsters.unlimitedgames.games.sudoku

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.appsters.unlimitedgames.games.sudoku.model.Cell
import com.appsters.unlimitedgames.games.sudoku.model.GameState
import com.appsters.unlimitedgames.games.sudoku.model.PuzzleGenerator
import com.appsters.unlimitedgames.games.sudoku.model.Score
import com.appsters.unlimitedgames.games.sudoku.repository.SudokuRepository
import com.appsters.unlimitedgames.games.sudoku.util.SudokuTimer
import kotlinx.coroutines.launch

class SudokuViewModel(private val repository: SudokuRepository) : ViewModel() {

    private val _gameState = MutableLiveData<GameState>()
    val gameState: LiveData<GameState> = _gameState

    private val _timer = MutableLiveData<String>()
    val timer: LiveData<String> = _timer

    private val _selectedCell = MutableLiveData<Cell?>()
    val selectedCell: LiveData<Cell?> = _selectedCell

    private val _impossibleNumbers = MutableLiveData<Set<Int>>()
    val impossibleNumbers: LiveData<Set<Int>> = _impossibleNumbers

    private val _invalidMoveEvent = MutableLiveData<Int>()
    val invalidMoveEvent: LiveData<Int> = _invalidMoveEvent

    private val _gameCompletedEvent = MutableLiveData<Score>()
    val gameCompletedEvent: LiveData<Score> = _gameCompletedEvent

    private lateinit var sudokuTimer: SudokuTimer

    fun startNewGame(difficulty: SudokuMenuFragment.Difficulty, isRanked: Boolean = true) {
        viewModelScope.launch {
            val board = PuzzleGenerator.generate(difficulty)
            val newGameState = GameState(
                board = board,
                difficulty = difficulty,
                isRanked = isRanked
            )
            _gameState.postValue(newGameState)
            _selectedCell.postValue(null)
            _impossibleNumbers.postValue(emptySet())
            startTimer(newGameState)
        }
    }

    fun resumeGame(gameState: GameState) {
        _gameState.postValue(gameState)
        _selectedCell.postValue(null)
        _impossibleNumbers.postValue(emptySet())
        startTimer(gameState)
    }

    fun saveGame() {
        _gameState.value?.let { currentState ->
            if (currentState.isCompleted) return
            currentState.isPaused = true
            repository.saveGameState(currentState, currentState.difficulty)
            pauseTimer()
        }
    }

    fun deleteSavedGame() {
        _gameState.value?.let { currentState ->
            repository.clearSavedGame(currentState.difficulty)
        }
    }

    fun isRankedGame(): Boolean {
        return _gameState.value?.isRanked ?: false
    }

    fun toggleImpossibleNumber(number: Int) {
        val selected = _selectedCell.value ?: return
        val currentGameState = _gameState.value!!

        if (selected.isFixed || selected.value != 0) return

        if (selected.impossibleNumbers.contains(number)) {
            selected.impossibleNumbers.remove(number)
        } else {
            selected.impossibleNumbers.add(number)
        }
        _impossibleNumbers.postValue(selected.impossibleNumbers)
        _gameState.postValue(currentGameState)
    }

    fun pauseTimer() {
        if (::sudokuTimer.isInitialized) {
            sudokuTimer.pause()
        }
    }

    fun resumeTimer() {
        if (::sudokuTimer.isInitialized) {
            sudokuTimer.resume()
        }
    }

    private fun startTimer(gameState: GameState) {
        sudokuTimer = SudokuTimer { elapsedTime ->
            gameState.elapsedTime = elapsedTime
            _timer.postValue(gameState.getFormattedTime())
        }
        sudokuTimer.setElapsedTime(gameState.elapsedTime)
        sudokuTimer.start()
    }

    fun onCellSelected(row: Int, col: Int) {
        val currentBoard = _gameState.value?.board ?: return
        val cell = currentBoard.getCell(row, col)
        _selectedCell.postValue(cell)
        _impossibleNumbers.postValue(cell.impossibleNumbers)
    }

    fun enterValue(number: Int) {
        val selected = _selectedCell.value ?: return
        val currentBoard = _gameState.value?.board ?: return

        if (selected.isFixed) return

        val currentGameState = _gameState.value!!

        if (currentBoard.isValid(selected.row, selected.col, number)) {
            currentBoard.setCell(selected.row, selected.col, number)
            selected.impossibleNumbers.clear()
            _impossibleNumbers.postValue(emptySet())

            if (currentBoard.isSolved()) {
                currentGameState.isCompleted = true
                sudokuTimer.pause()
                repository.saveGameState(currentGameState, currentGameState.difficulty)
                currentGameState.getScore()?.let { finalScore ->
                    repository.saveHighScore(finalScore)
                    _gameCompletedEvent.postValue(finalScore)
                }
            }
        } else {
            if (isRankedGame()) {
                currentGameState.mistakes++
            }
            _invalidMoveEvent.postValue(number)
        }
        _gameState.postValue(currentGameState)
    }

    fun clearCell() {
        val selected = _selectedCell.value ?: return
        val currentBoard = _gameState.value?.board ?: return
        val currentGameState = _gameState.value!!

        if (selected.isFixed) return

        currentBoard.setCell(selected.row, selected.col, 0)
        selected.impossibleNumbers.clear()
        _impossibleNumbers.postValue(emptySet())

        _gameState.postValue(currentGameState)
    }

    override fun onCleared() {
        super.onCleared()
        pauseTimer()
    }
}

class SudokuViewModelFactory(private val repository: SudokuRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SudokuViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SudokuViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}