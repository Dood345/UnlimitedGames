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
        get() = RunManager.player.maxStamina
    
    // Game State
    private val _isGameOver = androidx.lifecycle.MutableLiveData<Boolean>(false)
    val isGameOver: androidx.lifecycle.LiveData<Boolean> = _isGameOver

    private val _isLevelComplete = androidx.lifecycle.MutableLiveData<Boolean>(false)
    val isLevelComplete: androidx.lifecycle.LiveData<Boolean> = _isLevelComplete

    // Physics (Dynamic)
    val currentMaxSpeed: Float
        get() = RunManager.player.effectiveSpeed
    
    val currentAcceleration: Float
        get() = RunManager.player.baseAcceleration
        
    val visibilityRadius: Int
        get() = RunManager.player.effectiveVisibility

    // Run State
    private val _currentRunMoney = androidx.lifecycle.MutableLiveData<Int>(0)
    val currentRunMoney: androidx.lifecycle.LiveData<Int> = _currentRunMoney
    
    private val _currentRunXP = androidx.lifecycle.MutableLiveData<Int>(0)
    val currentRunXP: androidx.lifecycle.LiveData<Int> = _currentRunXP

    private val _currentLevel = androidx.lifecycle.MutableLiveData<Int>(1)
    val currentLevel: androidx.lifecycle.LiveData<Int> = _currentLevel

    private val _xpProgress = androidx.lifecycle.MutableLiveData<Pair<Int, Int>>() // Current, Max
    val xpProgress: androidx.lifecycle.LiveData<Pair<Int, Int>> = _xpProgress

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
        _currentLevel.value = RunManager.currentLevel
        _xpProgress.value = Pair(RunManager.currentLevelXP, RunManager.xpToNextLevel)
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
        if (RunManager.player.currentStamina <= 0f) {
            RunManager.player.currentStamina = RunManager.player.maxStamina
        }
        _currentStamina.value = RunManager.player.currentStamina
        _isGameOver.value = false
        _isLevelComplete.value = false

        spawnItems(width, height)
    }

    private fun spawnItems(width: Int, height: Int) {
        val m = maze ?: return
        
        // BFS to find distances from (0,0)
        val distances = Array(height) { IntArray(width) { -1 } }
        val queue = java.util.LinkedList<Pair<Int, Int>>()
        
        distances[0][0] = 0
        queue.add(Pair(0, 0))
        
        val validSpawnPoints = java.util.ArrayList<Pair<Int, Int>>()
        
        while (queue.isNotEmpty()) {
            val (c, r) = queue.poll() ?: break
            val dist = distances[r][c]
            
            // Collect valid points (10-20 steps away)
            if (dist in 10..20) {
                validSpawnPoints.add(Pair(c, r))
            }
            
            // Neighbors
            // Up
            if (r > 0 && !m.cells[r][c].topWall && distances[r-1][c] == -1) {
                distances[r-1][c] = dist + 1
                queue.add(Pair(c, r-1))
            }
            // Down
            if (r < height - 1 && !m.cells[r][c].bottomWall && distances[r+1][c] == -1) {
                distances[r+1][c] = dist + 1
                queue.add(Pair(c, r+1))
            }
            // Left
            if (c > 0 && !m.cells[r][c].leftWall && distances[r][c-1] == -1) {
                distances[r][c-1] = dist + 1
                queue.add(Pair(c-1, r))
            }
            // Right
            if (c < width - 1 && !m.cells[r][c].rightWall && distances[r][c+1] == -1) {
                distances[r][c+1] = dist + 1
                queue.add(Pair(c+1, r))
            }
        }
        
        // Spawn Items
        val numberOfItems = 10 // Increased for testing
        
        if (validSpawnPoints.isNotEmpty()) {
            validSpawnPoints.shuffle()
            
            for (i in 0 until kotlin.math.min(numberOfItems, validSpawnPoints.size)) {
                val (c, r) = validSpawnPoints[i]
                
                // 50/50 Artifact or PowerUp
                if (kotlin.random.Random.nextBoolean()) {
                    m.items.add(com.appsters.unlimitedgames.games.maze.model.MazeItem.Artifact(c, r, 10))
                } else {
                    m.items.add(com.appsters.unlimitedgames.games.maze.model.MazeItem.PowerUp(c, r, com.appsters.unlimitedgames.games.maze.model.PowerUpType.STAMINA_REFILL, 0))
                }
            }
        } else {
            android.util.Log.w("MazeViewModel", "No valid spawn points found within 10-20 steps!")
            // Fallback?
        }
    }

    fun onStepTaken() {
        if (_isGameOver.value == true) return

        // Tick Player Effects
        RunManager.player.tickEffects()

        // Drain Stamina
        val drain = RunManager.player.staminaDrainRate
        RunManager.player.currentStamina -= drain
        
        val newStamina = RunManager.player.currentStamina
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
                val leveledUp = RunManager.addXP(10) // 10 XP per artifact
                
                if (leveledUp) {
                    // Refill Stamina
                    RunManager.player.currentStamina = RunManager.player.maxStamina
                    _currentStamina.value = RunManager.player.currentStamina
                    // Show Toast or some indicator? ViewModel shouldn't show Toast directly.
                    // We can use a SingleLiveEvent or just let the UI observe the level change.
                }
                
                updateRunState()
            }
            is com.appsters.unlimitedgames.games.maze.model.MazeItem.PowerUp -> {
                when (item.type) {
                    com.appsters.unlimitedgames.games.maze.model.PowerUpType.STAMINA_REFILL -> {
                        RunManager.player.currentStamina = RunManager.player.maxStamina
                        _currentStamina.value = RunManager.player.currentStamina
                    }
                    else -> {
                        // Implement other powerups later
                    }
                }
            }
        }
    }
}
