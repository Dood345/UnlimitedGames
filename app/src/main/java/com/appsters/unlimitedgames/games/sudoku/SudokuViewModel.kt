package com.appsters.unlimitedgames.games.sudoku

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.appsters.unlimitedgames.app.data.repository.LeaderboardRepository
import com.appsters.unlimitedgames.app.data.repository.UserRepository
import com.appsters.unlimitedgames.app.util.GameType
import com.appsters.unlimitedgames.games.sudoku.model.Cell
import com.appsters.unlimitedgames.games.sudoku.model.GameState
import com.appsters.unlimitedgames.games.sudoku.model.PuzzleGenerator
import com.appsters.unlimitedgames.games.sudoku.model.Score
import com.appsters.unlimitedgames.games.sudoku.repository.SudokuRepository
import com.appsters.unlimitedgames.games.sudoku.util.SudokuTimer
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

/**
 * The ViewModel for the Sudoku game.
 * Manages the game state, business logic, timer, and communication with the repository.
 * It exposes LiveData objects for the UI to observe.
 */
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
    private val leaderboardRepository = LeaderboardRepository()
    private val userRepository = UserRepository()

    /**
     * Starts a new game with the specified parameters.
     * Generates a new puzzle on a background thread.
     *
     * @param difficulty The difficulty level.
     * @param isRanked Whether the game is ranked.
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
            _impossibleNumbers.postValue(emptySet())
            startTimer(newGameState)
        }
    }

    /**
     * Resumes an existing game state.
     *
     * @param gameState The saved game state to resume.
     */
    fun resumeGame(gameState: GameState) {
        _gameState.postValue(gameState)
        _selectedCell.postValue(null)
        _impossibleNumbers.postValue(emptySet())
        startTimer(gameState)
    }

    /**
     * Saves the current game state to the repository.
     * Does nothing if the game is already completed.
     */
    fun saveGame() {
        _gameState.value?.let { currentState ->
            if (currentState.isCompleted) return
            currentState.isPaused = true
            repository.saveGameState(currentState, currentState.difficulty)
            pauseTimer()
        }
    }

    /**
     * Explicitly deletes the saved game from the repository.
     * Typically called when the user finishes a game and returns to the menu.
     */
    fun deleteSavedGame() {
        _gameState.value?.let { currentState ->
            repository.clearSavedGame(currentState.difficulty)
        }
    }

    fun isRankedGame(): Boolean {
        return _gameState.value?.isRanked ?: false
    }

    /**
     * Toggles the "impossible" status of a number for the selected cell.
     * Used for marking numbers that the user believes cannot go in the cell.
     */
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

    /**
     * Handles the selection of a cell on the board.
     * Updates the selected cell state and exposes its impossible numbers.
     */
    fun onCellSelected(row: Int, col: Int) {
        val currentBoard = _gameState.value?.board ?: return
        val cell = currentBoard.getCell(row, col)
        _selectedCell.postValue(cell)
        _impossibleNumbers.postValue(cell.impossibleNumbers)
    }

    /**
     * Attempts to enter a number into the selected cell.
     * Validates the move, updates the board, checks for completion, and handles scoring.
     *
     * @param number The number to enter (1-9).
     */
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

                val finalScore = currentGameState.getScore()
                if (currentGameState.isRanked) {
                    repository.saveHighScore(finalScore)
                    submitScoreToLeaderboard(finalScore.calculateScore())
                }
                _gameCompletedEvent.postValue(finalScore)
            }
        } else {
            if (isRankedGame()) {
                currentGameState.mistakes++
            }
            _invalidMoveEvent.postValue(number)
        }
        _gameState.postValue(currentGameState)
    }

    private fun submitScoreToLeaderboard(score: Int) {
        val userId = FirebaseAuth.getInstance().uid ?: return
        userRepository.getUser(userId) { task ->
            if (task.isSuccessful) {
                val user = task.result
                if (user != null) {
                    val username = user.username
                    val scoreObject = com.appsters.unlimitedgames.app.data.model.Score(
                        null, userId, username, GameType.SUDOKU, score
                    )
                    leaderboardRepository.submitScore(scoreObject) { _, _, _ ->
                        // Optionally handle success or failure
                    }
                }
            }
        }
    }

    /**
     * Clears the value of the currently selected cell.
     * Only works if the cell is not fixed (part of the initial puzzle).
     */
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