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

/**
 * ViewModel for the Sudoku game, responsible for managing the game's state, logic, and user interactions.
 *
 * This ViewModel holds the [GameState], communicates with the UI through [LiveData], and handles
 * user inputs like selecting a cell or entering a number.
 */
class SudokuViewModel(private val repository: SudokuRepository) : ViewModel() {

    private val _gameState = MutableLiveData<GameState>()
    /** LiveData holding the current state of the game, including the board and player progress. */
    val gameState: LiveData<GameState> = _gameState

    private val _timer = MutableLiveData<String>()
    /** LiveData providing the formatted elapsed time as a string (e.g., "01:23"). */
    val timer: LiveData<String> = _timer

    private val _selectedCell = MutableLiveData<Cell?>()
    /** LiveData representing the currently selected cell on the board. Null if no cell is selected. */
    val selectedCell: LiveData<Cell?> = _selectedCell

    private val _invalidMoveEvent = MutableLiveData<Int>()
    /** LiveData event that triggers when the user makes an invalid move. It holds the invalid number. */
    val invalidMoveEvent: LiveData<Int> = _invalidMoveEvent

    private val _gameCompletedEvent = MutableLiveData<Score>()
    /** LiveData event that triggers when the puzzle is successfully solved. It holds the final [Score]. */
    val gameCompletedEvent: LiveData<Score> = _gameCompletedEvent

    private val _isNoteMode = MutableLiveData<Boolean>(false)
    /** LiveData indicating if the user is currently in "Note Mode" (pencil marks). */
    val isNoteMode: LiveData<Boolean> = _isNoteMode

    private lateinit var sudokuTimer: SudokuTimer

    /**
     * Creates and initializes a new Sudoku game with the specified difficulty.
     * Generates a new puzzle, sets up the game state, and starts the timer.
     *
     * @param difficulty The difficulty level for the new game.
     * @param isRanked Whether the game is ranked or free play.
     */
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
            startTimer(newGameState)
        }
    }

    /**
     * Resumes a previously saved game.
     */
    fun resumeGame(gameState: GameState) {
        _gameState.postValue(gameState)
        _selectedCell.postValue(null)
        startTimer(gameState)
    }

    /**
     * Saves the current game state to the repository.
     */
    fun saveGame() {
        _gameState.value?.let { currentState ->
            currentState.isPaused = true
            repository.saveGameState(currentState)
            pauseTimer()
        }
    }

    fun isRankedGame(): Boolean {
        return _gameState.value?.isRanked ?: false
    }

    /**
     * Toggles the Note Mode on or off.
     */
    fun toggleNoteMode() {
        _isNoteMode.value = !(_isNoteMode.value ?: false)
    }

    /**
     * Pauses the game timer.
     */
    fun pauseTimer() {
        if (::sudokuTimer.isInitialized) {
            sudokuTimer.pause()
        }
    }

    /**
     * Resumes the game timer.
     */
    fun resumeTimer() {
        if (::sudokuTimer.isInitialized) {
            sudokuTimer.resume()
        }
    }

    /**
     * Starts the game timer and updates the timer LiveData every second.
     * @param gameState The current game state to update with the elapsed time.
     */
    private fun startTimer(gameState: GameState) {
        sudokuTimer = SudokuTimer { elapsedTime ->
            gameState.elapsedTime = elapsedTime
            _timer.postValue(gameState.getFormattedTime())
        }
        sudokuTimer.setElapsedTime(gameState.elapsedTime)
        sudokuTimer.start()
    }

    /**
     * Handles the user's selection of a cell on the board.
     * Updates the [_selectedCell] LiveData with the new cell.
     *
     * @param row The row of the selected cell (0-8).
     * @param col The column of the selected cell (0-8).
     */
    fun onCellSelected(row: Int, col: Int) {
        val currentBoard = _gameState.value?.board ?: return
        _selectedCell.postValue(currentBoard.getCell(row, col))
    }

    /**
     * Processes a number input from the user for the currently selected cell.
     * If the move is valid, the cell's value is updated. If the board is solved, the game is marked as completed.
     * If the move is invalid, the mistake count is incremented and an [_invalidMoveEvent] is triggered.
     *
     * @param number The number (1-9) entered by the user.
     */
    fun enterValue(number: Int) {
        val selected = _selectedCell.value ?: return
        val currentBoard = _gameState.value?.board ?: return

        if (selected.isFixed) return

        val currentGameState = _gameState.value!!

        if (_isNoteMode.value == true) {
            // Toggle note
            if (selected.notes.contains(number)) {
                selected.notes.remove(number)
            } else {
                selected.notes.add(number)
            }
            _gameState.postValue(currentGameState)
        } else {
            // Normal number entry
            if (currentBoard.isValid(selected.row, selected.col, number)) {
                currentBoard.setCell(selected.row, selected.col, number)
                // Clear notes in the row, col, and box that match this number
                currentBoard.clearNotes(selected.row, selected.col, number)

                if (currentBoard.isSolved()) {
                    currentGameState.isCompleted = true
                    sudokuTimer.pause()
                    currentGameState.getScore()?.let { finalScore ->
                        repository.saveHighScore(finalScore)
                        repository.clearSavedGame() // Clear saved game on completion
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
    }

    /**
     * Clears the value of the currently selected cell, if it's not a fixed part of the puzzle.
     * The cell's value is reset to 0.
     */
    fun clearCell() {
        val selected = _selectedCell.value ?: return
        val currentBoard = _gameState.value?.board ?: return
        val currentGameState = _gameState.value!!

        if (selected.isFixed) return

        // Set the cell's value back to 0
        currentBoard.setCell(selected.row, selected.col, 0)
        selected.notes.clear() // Also clear notes

        // Post the updated state to the UI
        _gameState.postValue(currentGameState)
    }

    /**
     * Called when the ViewModel is about to be destroyed.
     * Pauses the timer to prevent memory leaks.
     */
    override fun onCleared() {
        super.onCleared()
        pauseTimer()
    }
}

/**
 * A factory for creating [SudokuViewModel] instances with a [SudokuRepository] dependency.
 */
class SudokuViewModelFactory(private val repository: SudokuRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SudokuViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SudokuViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}