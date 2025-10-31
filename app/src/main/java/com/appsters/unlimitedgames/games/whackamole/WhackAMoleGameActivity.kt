package com.appsters.unlimitedgames.games.whackamole

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.appsters.unlimitedgames.R
import com.appsters.unlimitedgames.games.whackamole.model.GameConfig
import com.appsters.unlimitedgames.games.whackamole.model.Mole
import com.appsters.unlimitedgames.games.whackamole.model.MoleColor
import com.appsters.unlimitedgames.games.whackamole.repository.SharedPrefGameRepository
import com.appsters.unlimitedgames.games.whackamole.util.AndroidScheduler
import com.appsters.unlimitedgames.games.whackamole.WhackAMoleGameViewModel
import java.util.concurrent.TimeUnit

class WhackAMoleGameActivity : AppCompatActivity() {

    private lateinit var scoreTextView: TextView
    private lateinit var livesTextView: TextView
    private lateinit var timerTextView: TextView
    private val moleImageViews = mutableListOf<ImageView>()

    private val viewModel: WhackAMoleGameViewModel by lazy {
        val prefs = getSharedPreferences("WhackAMolePrefs", MODE_PRIVATE)
        val repository = SharedPrefGameRepository(prefs)
        val scheduler = AndroidScheduler(mainLooper)
        WhackAMoleGameViewModel(repository, scheduler)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.whack_a_mole_game)

        scoreTextView = findViewById(R.id.score)
        livesTextView = findViewById(R.id.lives)
        timerTextView = findViewById(R.id.timer)

        setupMoleViews()

        viewModel.score.observe(this, Observer { score ->
            scoreTextView.text = getString(R.string.score_format, score)
        })

        viewModel.misses.observe(this, Observer { misses ->
            val livesRemaining = GameConfig.DEFAULT.maxMisses - misses
            livesTextView.text = getString(R.string.lives_format, livesRemaining)
        })

        viewModel.moles.observe(this, Observer { moleContainer ->
            moleContainer.moles.forEach { mole ->
                updateMoleView(mole)
            }
        })

        viewModel.gameOver.observe(this, Observer { isGameOver ->
            if (isGameOver) {
                endGame()
            }
        })

        viewModel.moleTimeRemaining.observe(this, Observer { timeInMillis ->
            timerTextView.text = getString(R.string.timer_format, timeInMillis / 1000.0)
        })
    }

    private fun setupMoleViews() {
        val moleIds = listOf(
            R.id.mole_0, R.id.mole_1, R.id.mole_2,
            R.id.mole_3, R.id.mole_4, R.id.mole_5,
            R.id.mole_6, R.id.mole_7, R.id.mole_8
        )

        moleIds.forEachIndexed { index, moleId ->
            val moleView = findViewById<ImageView>(moleId)
            moleView.setOnClickListener {
                onMoleWhacked(index)
            }
            moleView.visibility = View.INVISIBLE
            moleImageViews.add(moleView)
        }
    }

    private fun onMoleWhacked(moleId: Int) {
        viewModel.hitMole(moleId)
    }

    private fun updateMoleView(mole: Mole) {
        if (moleImageViews.isEmpty()) {
            return
        }

        val moleView = moleImageViews[mole.id]

        if (mole.isVisible) {
            moleView.visibility = View.VISIBLE
            val colorResId = when (mole.color) {
                MoleColor.RED -> R.color.mole_red
                MoleColor.BLUE -> R.color.mole_blue
                MoleColor.GREEN -> R.color.mole_green
                MoleColor.YELLOW -> R.color.mole_yellow
                MoleColor.PURPLE -> R.color.mole_purple
            }
            moleView.backgroundTintList = ContextCompat.getColorStateList(this, colorResId)
        } else {
            moleView.visibility = View.INVISIBLE
        }
    }

    private fun endGame() {
        val finalScore = viewModel.score.value ?: 0
        val highScore = viewModel.highScore.value ?: 0

        val message = if (finalScore > highScore) {
            "New High Score: $finalScore!"
        } else {
            "Game Over! Score: $finalScore\nHigh Score: $highScore"
        }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Game Over")
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("Restart") { dialog, _ ->
                dialog.dismiss()
                restartGame()
            }
            .setNegativeButton("Main Menu") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .show()
    }

    fun restartGame() {
        viewModel.resetGame()
    }
}
