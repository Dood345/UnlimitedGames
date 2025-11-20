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

    private lateinit var pbStamina: android.widget.ProgressBar
    private lateinit var pbXP: android.widget.ProgressBar
    private lateinit var tvMoney: android.widget.TextView
    private lateinit var tvRound: android.widget.TextView

    private lateinit var tvStaminaValue: android.widget.TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maze_game)

        mazeView = findViewById(R.id.maze_view)
        dPad = findViewById(R.id.d_pad)
        pbStamina = findViewById(R.id.pb_stamina)
        pbXP = findViewById(R.id.pb_xp)
        tvMoney = findViewById(R.id.tv_money)
        tvRound = findViewById(R.id.tv_round)
        tvStaminaValue = findViewById(R.id.tv_stamina_value)

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
                showUpgradeScreen(true)
            }
        }

        // Observe Level Complete
        viewModel.isLevelComplete.observe(this) { isComplete ->
            if (isComplete) {
                mazeView.stopGame()
                showUpgradeScreen(false)
            }
        }

        // Observe Stamina
        viewModel.currentStamina.observe(this) { stamina ->
            pbStamina.max = viewModel.maxStamina.toInt()
            pbStamina.progress = stamina.toInt()
            tvStaminaValue.text = "${stamina.toInt()}/${viewModel.maxStamina.toInt()}"
        }

        // Observe Run State
        viewModel.currentRunMoney.observe(this) { money ->
            android.util.Log.d("MazeGame", "Money Updated: $money")
            tvMoney.text = "Money: $$money"
        }
        viewModel.currentRunXP.observe(this) { xp ->
            android.util.Log.d("MazeGame", "XP Updated: $xp")
            pbXP.progress = xp % 100 // Simple level up logic for now
        }
        viewModel.currentRound.observe(this) { round ->
            tvRound.text = "Round: $round"
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

    private fun showUpgradeScreen(isGameOver: Boolean = false) {
        val upgradeFragment = UpgradeFragment()
        upgradeFragment.setGameOverState(isGameOver)
        
        upgradeFragment.onUpgradeListener = {
            viewModel.updateRunState()
        }
        upgradeFragment.onNextLevelListener = {
            if (isGameOver) {
                // New Run
                com.appsters.unlimitedgames.games.maze.RunManager.startNewRun()
            } else {
                // Next Level
                com.appsters.unlimitedgames.games.maze.RunManager.nextRound()
            }
            
            viewModel.resetGame(15, 15)
            mazeView.maxSpeed = viewModel.currentMaxSpeed
            mazeView.acceleration = viewModel.currentAcceleration
            
            // Re-apply maze to view
            viewModel.maze?.let {
                mazeView.setMaze(it)
                mazeView.setPlayerPosition(viewModel.playerX, viewModel.playerY)
            }
        }
        upgradeFragment.onMainMenuListener = {
            finish()
        }
        upgradeFragment.show(supportFragmentManager, "UpgradeFragment")
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
