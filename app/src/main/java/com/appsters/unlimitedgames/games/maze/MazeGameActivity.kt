package com.appsters.unlimitedgames.games.maze

import android.os.Bundle
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.appsters.unlimitedgames.R

class MazeGameActivity : AppCompatActivity() {

    private lateinit var mazeView: MazeView
    private lateinit var dPad: DirectionalPadView
    private lateinit var viewModel: MazeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maze_game)

        mazeView = findViewById(R.id.maze_view)
        dPad = findViewById(R.id.d_pad)

        viewModel = androidx.lifecycle.ViewModelProvider(this)[MazeViewModel::class.java]
        viewModel.generateMaze(15, 15)

        // Sync physics
        mazeView.maxSpeed = viewModel.currentMaxSpeed
        mazeView.acceleration = viewModel.currentAcceleration

        viewModel.maze?.let {
            mazeView.setMaze(it)
            mazeView.setPlayerPosition(viewModel.playerX, viewModel.playerY)
        }

        // Observe Game Over
        viewModel.isGameOver.observe(this) { isGameOver ->
            if (isGameOver) {
                mazeView.stopGame()
                showGameOverDialog()
            }
        }

        // Observe Level Complete
        viewModel.isLevelComplete.observe(this) { isComplete ->
            if (isComplete) {
                mazeView.stopGame()
                showLevelCompleteDialog()
            }
        }

        // Observe Stamina (for debugging/future UI)
        viewModel.currentStamina.observe(this) { stamina ->
            // android.util.Log.d("MazeGame", "Stamina: $stamina")
        }

        mazeView.onTileChangedListener = {
            viewModel.playerX = mazeView.playerX
            viewModel.playerY = mazeView.playerY
            viewModel.onStepTaken()
        }

        dPad.listener = object : DirectionalPadView.OnDirectionalPadListener {
            override fun onDirectionChanged(dx: Int, dy: Int) {
                mazeView.isPressingUp = dy == -1
                mazeView.isPressingDown = dy == 1
                mazeView.isPressingLeft = dx == -1
                mazeView.isPressingRight = dx == 1
            }
        }
    }

    private fun showGameOverDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Game Over")
            .setMessage("You ran out of stamina!")
            .setPositiveButton("Main Menu") { _, _ ->
                finish()
            }
            .setNegativeButton("Try Again") { _, _ ->
                // Reset run?
                com.appsters.unlimitedgames.games.maze.RunManager.startNewRun()
                viewModel.resetGame(15, 15)
                // Sync physics again just in case
                mazeView.maxSpeed = viewModel.currentMaxSpeed
                mazeView.acceleration = viewModel.currentAcceleration
                
                // Re-apply maze to view
                viewModel.maze?.let {
                    mazeView.setMaze(it)
                    mazeView.setPlayerPosition(viewModel.playerX, viewModel.playerY)
                }
                // We don't strictly need recreate() if we reset the VM and View state correctly.
                // But recreate() ensures a clean UI state (dialogs gone, etc).
                // However, recreate() re-uses the VM.
                // So we reset VM *then* recreate? Or just update UI?
                // Updating UI is faster.
            }
            .setCancelable(false)
            .show()
    }

    private fun showLevelCompleteDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Level Complete!")
            .setMessage("Money: ${viewModel.currentRunMoney}\nXP: ${viewModel.currentRunXP}")
            .setPositiveButton("Next Level") { _, _ ->
                // Go to next level (Generate new maze)
                com.appsters.unlimitedgames.games.maze.RunManager.nextRound()
                viewModel.resetGame(15, 15)
                
                // Re-apply maze to view
                viewModel.maze?.let {
                    mazeView.setMaze(it)
                    mazeView.setPlayerPosition(viewModel.playerX, viewModel.playerY)
                }
            }
            .setNegativeButton("Main Menu") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }

    override fun onPause() {
        super.onPause()
        viewModel.playerX = mazeView.playerX
        viewModel.playerY = mazeView.playerY
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> mazeView.isPressingUp = true
            KeyEvent.KEYCODE_DPAD_DOWN -> mazeView.isPressingDown = true
            KeyEvent.KEYCODE_DPAD_LEFT -> mazeView.isPressingLeft = true
            KeyEvent.KEYCODE_DPAD_RIGHT -> mazeView.isPressingRight = true
            else -> return super.onKeyDown(keyCode, event)
        }
        return true
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> mazeView.isPressingUp = false
            KeyEvent.KEYCODE_DPAD_DOWN -> mazeView.isPressingDown = false
            KeyEvent.KEYCODE_DPAD_LEFT -> mazeView.isPressingLeft = false
            KeyEvent.KEYCODE_DPAD_RIGHT -> mazeView.isPressingRight = false
            else -> return super.onKeyUp(keyCode, event)
        }
        return true
    }
}
