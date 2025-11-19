package com.appsters.unlimitedgames.games.maze

import androidx.lifecycle.ViewModel
import com.appsters.unlimitedgames.games.maze.model.Maze

class MazeViewModel : ViewModel() {

    var maze: Maze? = null
        private set

    // Player State
    var playerX: Float = 0.5f
    var playerY: Float = 0.5f

    // Stamina
    private val _currentStamina = androidx.lifecycle.MutableLiveData<Float>()
    val currentStamina: androidx.lifecycle.LiveData<Float> = _currentStamina

    val maxStamina: Float
        get() = RunManager.maxStamina
    
    // Game State
    private val _isGameOver = androidx.lifecycle.MutableLiveData<Boolean>(false)
    val isGameOver: androidx.lifecycle.LiveData<Boolean> = _isGameOver

    private val _isLevelComplete = androidx.lifecycle.MutableLiveData<Boolean>(false)
    val isLevelComplete: androidx.lifecycle.LiveData<Boolean> = _isLevelComplete

    // Physics (Dynamic)
    var currentMaxSpeed: Float = RunManager.baseMaxSpeed
        private set
    var currentAcceleration: Float = RunManager.baseAcceleration
        private set

    // Run State
    private val _currentRunMoney = androidx.lifecycle.MutableLiveData<Int>(0)
    val currentRunMoney: androidx.lifecycle.LiveData<Int> = _currentRunMoney
    
    private val _currentRunXP = androidx.lifecycle.MutableLiveData<Int>(0)
    val currentRunXP: androidx.lifecycle.LiveData<Int> = _currentRunXP

    private val _currentRound = androidx.lifecycle.MutableLiveData<Int>(1)
    val currentRound: androidx.lifecycle.LiveData<Int> = _currentRound

    fun generateMaze(width: Int, height: Int) {
        if (maze == null) {
            createMaze(width, height)
        }
        // Sync initial run state
        updateRunState()
    }

    fun updateRunState() {
        _currentRunMoney.value = RunManager.totalMoney
        _currentRunXP.value = RunManager.totalXP
        _currentRound.value = RunManager.roundNumber
    }

    fun resetGame(width: Int, height: Int) {
        maze = null
        _isGameOver.value = false
        _isLevelComplete.value = false
        _currentStamina.value = maxStamina
        createMaze(width, height)
    }

    private fun createMaze(width: Int, height: Int) {
        val newMaze = Maze(width, height)
        newMaze.generate()
        maze = newMaze
        
        // Reset player position
        playerX = 0.5f
        playerY = 0.5f
        
        // Reset Stamina
        if (_currentStamina.value == null || _currentStamina.value!! <= 0f) {
            _currentStamina.value = maxStamina
        }
        _isGameOver.value = false
        _isLevelComplete.value = false

        spawnItems(width, height)
    }

    private fun spawnItems(width: Int, height: Int) {
        val m = maze ?: return
        // Simple spawning logic: Spawn a few items within 10 tiles of start (0,0) but not AT start.
        // User said: "artifacts that spawn within 10 tiles from the player"
        
        val numberOfItems = 3 // Configurable
        var itemsSpawned = 0
        val attempts = 0
        val maxAttempts = 20

        while (itemsSpawned < numberOfItems && attempts < maxAttempts) {
            val rCol = (0 until width).random()
            val rRow = (0 until height).random()
            
            // Check distance (Manhattan or Euclidean? Manhattan is easier for tiles)
            val dist = kotlin.math.abs(rCol - 0) + kotlin.math.abs(rRow - 0)
            
            if (dist in 2..10) { // Not at start, but within 10
                 // Avoid walls? Items are usually inside cells.
                 // Check if item already exists there
                 if (m.items.none { it.x == rCol && it.y == rRow }) {
                     // 50/50 Artifact or PowerUp
                     if (kotlin.random.Random.nextBoolean()) {
                         m.items.add(com.appsters.unlimitedgames.games.maze.model.MazeItem.Artifact(rCol, rRow, 10))
                     } else {
                         m.items.add(com.appsters.unlimitedgames.games.maze.model.MazeItem.PowerUp(rCol, rRow, com.appsters.unlimitedgames.games.maze.model.PowerUpType.STAMINA_REFILL, 0))
                     }
                     itemsSpawned++
                 }
            }
        }
    }

    fun onStepTaken() {
        if (_isGameOver.value == true) return

        // Drain Stamina
        val current = _currentStamina.value ?: 0f
        val newStamina = current - RunManager.staminaDrainRate
        _currentStamina.value = if (newStamina < 0) 0f else newStamina

        if (newStamina <= 0) {
            _isGameOver.value = true
            return
        }

        // Check for Items
        val pCol = playerX.toInt()
        val pRow = playerY.toInt()
        val m = maze ?: return

        // android.util.Log.d("MazeViewModel", "Step: ($pCol, $pRow) Stamina: $newStamina")

        val iterator = m.items.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            if (item.x == pCol && item.y == pRow) {
                collectItem(item)
                iterator.remove()
            }
        }

        // Check Level Complete (Exit is at bottom-right)
        if (pCol == m.width - 1 && pRow == m.height - 1) {
            android.util.Log.d("MazeViewModel", "Level Complete Condition Met! ($pCol, $pRow)")
            _isLevelComplete.value = true
        } else {
            // android.util.Log.d("MazeViewModel", "Not Exit: ($pCol, $pRow) vs (${m.width - 1}, ${m.height - 1})")
        }
    }

    private fun collectItem(item: com.appsters.unlimitedgames.games.maze.model.MazeItem) {
        when (item) {
            is com.appsters.unlimitedgames.games.maze.model.MazeItem.Artifact -> {
                RunManager.totalMoney += item.value
                RunManager.totalXP += 10 // Arbitrary XP
                updateRunState()
            }
            is com.appsters.unlimitedgames.games.maze.model.MazeItem.PowerUp -> {
                when (item.type) {
                    com.appsters.unlimitedgames.games.maze.model.PowerUpType.STAMINA_REFILL -> {
                        _currentStamina.value = maxStamina
                    }
                    else -> {
                        // Implement other powerups later
                    }
                }
            }
        }
    }
}
