package com.appsters.simpleGames.games.maze

import android.os.Bundle
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import com.appsters.simpleGames.R
import com.appsters.simpleGames.app.util.DirectionalPadView
import com.appsters.simpleGames.games.maze.controller.RunManager
import com.appsters.simpleGames.games.maze.view.MazeView

import nl.dionsegijn.konfetti.xml.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

class MazeGameActivity : AppCompatActivity() {

    private lateinit var mazeView: MazeView
    private lateinit var dPad: DirectionalPadView
    private lateinit var viewModel: MazeViewModel
    private lateinit var konfettiView: KonfettiView

    private lateinit var pbStamina: android.widget.ProgressBar
    private lateinit var pbXP: android.widget.ProgressBar
    private lateinit var tvMoney: android.widget.TextView
    private lateinit var tvRound: android.widget.TextView

    private lateinit var tvStaminaValue: android.widget.TextView
    private lateinit var tvLevel: android.widget.TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.appsters.simpleGames.app.util.SoundManager.init(this)
        setContentView(R.layout.activity_maze_game)

        mazeView = findViewById(R.id.maze_view)
        dPad = findViewById(R.id.d_pad)
        pbStamina = findViewById(R.id.pb_stamina)
        pbXP = findViewById(R.id.pb_xp)
        tvMoney = findViewById(R.id.tv_money)
        tvRound = findViewById(R.id.tv_round)
        tvStaminaValue = findViewById(R.id.tv_stamina_value)
        tvLevel = findViewById(R.id.tv_level)
        konfettiView = findViewById(R.id.konfettiView)

        viewModel = androidx.lifecycle.ViewModelProvider(this)[MazeViewModel::class.java]

        if (intent.getBooleanExtra("EXTRA_CONTINUE_RUN", false)) {
            val savedState = RunManager.loadGame(this)
            if (savedState != null) {
                viewModel.restoreMazeState(savedState)
            } else {
                viewModel.generateMaze(15, 15)
            }
        } else {
            viewModel.generateMaze(15, 15)
        }

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
                com.appsters.simpleGames.app.util.SoundManager.playSound(com.appsters.simpleGames.R.raw.win)
                triggerConfetti()
                mazeView.startRewind {
                    mazeView.stopGame()
                    showUpgradeScreen(false)
                }
            }
        }

        // Observe Stamina
        viewModel.currentStamina.observe(this) { stamina ->
            pbStamina.max = viewModel.maxStamina.toInt()
            pbStamina.progress = stamina.toInt()
            tvStaminaValue.text = "${stamina.toInt()}/${viewModel.maxStamina.toInt()}"
        }

        // Observe Visibility
        viewModel.currentVisibility.observe(this) { radius ->
            mazeView.visibilityRadius = radius
        }

        // Observe Run State
        viewModel.currentRunMoney.observe(this) { money ->
            android.util.Log.d("MazeGame", "Money Updated: $money")
            tvMoney.text = "Money: $$money"
        }
        
        viewModel.xpProgress.observe(this) { (current, max) ->
            pbXP.max = max
            pbXP.progress = current
        }
        
        viewModel.currentLevel.observe(this) { level ->
             tvLevel.text = "Level: $level"
        }

        viewModel.currentRound.observe(this) { round ->
             tvRound.text = "Round: $round"
        }

        mazeView.onTileChangedListener = {
            viewModel.playerX = mazeView.playerX
            viewModel.playerY = mazeView.playerY
            viewModel.onStepTaken()
        }
        
        mazeView.onUpdateListener = { dt ->
            viewModel.onGameUpdate(dt)
        }

        dPad.listener = object : DirectionalPadView.OnDirectionalPadListener {
            override fun onJoystickMoved(xPercent: Float, yPercent: Float) {
                mazeView.inputX = xPercent
                mazeView.inputY = yPercent
            }
        }
        
        val fabWallSmash: com.google.android.material.floatingactionbutton.FloatingActionButton = findViewById(R.id.fab_wall_smash)
        
        // Wall Smash Logic
        updateSkillUI()
        
        fabWallSmash.setOnClickListener {
            viewModel.activateWallSmash()
        }
        
        viewModel.isWallSmashActive.observe(this) { isActive ->
            if (isActive) {
                fabWallSmash.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.YELLOW)
                fabWallSmash.setImageResource(android.R.drawable.ic_menu_delete) // Placeholder for Hammer
            } else {
                fabWallSmash.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#F44336")) // Red
                fabWallSmash.setImageResource(android.R.drawable.ic_menu_close_clear_cancel) // Reset icon
                
                // Check if used
                if (viewModel.hasUsedWallSmash) {
                    fabWallSmash.isEnabled = false
                    fabWallSmash.alpha = 0.5f
                } else {
                    fabWallSmash.isEnabled = true
                    fabWallSmash.alpha = 1.0f
                }
            }
        }
        
        mazeView.onWallCollisionListener = { col: Int, row: Int, wallType: Int ->
            viewModel.onWallSmash(col, row, wallType)
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
                RunManager.startNewRun()
            } else {
                // Next Level
                RunManager.nextRound()
            }
            
            viewModel.resetGame(15, 15)
            mazeView.maxSpeed = viewModel.currentMaxSpeed
            mazeView.acceleration = viewModel.currentAcceleration
            
            // Re-apply maze to view
            viewModel.maze?.let {
                mazeView.setMaze(it)
                mazeView.setMaze(it)
                mazeView.setPlayerPosition(viewModel.playerX, viewModel.playerY)
            }
            
            // Update UI for new skills
            updateSkillUI()
        }
        upgradeFragment.onMainMenuListener = {
            if (!isGameOver) {
                RunManager.nextRound()
            }
            finish()
        }
        upgradeFragment.show(supportFragmentManager, "UpgradeFragment")
    }

    override fun onPause() {
        super.onPause()
        viewModel.playerX = mazeView.playerX
        viewModel.playerY = mazeView.playerY
        
        // Save Game
        // We save even if finishing (Back button), unless it's a game over which handles its own state.
        // RunManager.isRunInProgress should be the source of truth.
        if (RunManager.isRunInProgress) {
             val mazeState = if (viewModel.isLevelComplete.value == true) null else viewModel.serializeMazeState()
             RunManager.saveGame(this, mazeState)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> mazeView.inputY = -1f
            KeyEvent.KEYCODE_DPAD_DOWN -> mazeView.inputY = 1f
            KeyEvent.KEYCODE_DPAD_LEFT -> mazeView.inputX = -1f
            KeyEvent.KEYCODE_DPAD_RIGHT -> mazeView.inputX = 1f
            else -> return super.onKeyDown(keyCode, event)
        }
        return true
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN -> mazeView.inputY = 0f
            KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_RIGHT -> mazeView.inputX = 0f
            else -> return super.onKeyUp(keyCode, event)
        }
        return true
    }

    private fun triggerConfetti() {
        val party = Party(
            speed = 0f,
            maxSpeed = 30f,
            damping = 0.9f,
            spread = 360,
            colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
            emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(100),
            position = Position.Relative(0.5, 0.3)
        )
        konfettiView.start(party)
    }

    private fun updateSkillUI() {
        val fabWallSmash: com.google.android.material.floatingactionbutton.FloatingActionButton = findViewById(R.id.fab_wall_smash)
        if (RunManager.player.isWallSmashUnlocked) {
            fabWallSmash.visibility = android.view.View.VISIBLE
        } else {
            fabWallSmash.visibility = android.view.View.GONE
        }
    }
}
