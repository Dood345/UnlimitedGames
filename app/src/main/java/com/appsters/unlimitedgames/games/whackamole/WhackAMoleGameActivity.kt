package com.appsters.unlimitedgames.games.whackamole

import android.content.Intent
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

/**
 * The `GameActivity` class serves as the controller for the Whack-a-Mole game UI.
 * It observes the `GameViewModel` and updates the view in response to data changes.
 * This class manages the game's lifecycle, including initializing UI components,
 * handling user interactions, and displaying game state information such as score,
 * lives, and mole visibility.
 *
 * @author Jesutofunmi Obimakinde, Rand Roman, Daniel Ripley
 */
class WhackAMoleGameActivity : AppCompatActivity() {

    private lateinit var scoreTextView: TextView
    private lateinit var livesTextView: TextView
    private lateinit var timerTextView: TextView

    /**
     * A list of `ImageView` objects representing the moles. The visibility of these
     * views changes as moles appear and disappear. The mole views are identified by
     * IDs such as `mole_0`, `mole_1`, and so on, up to `mole_8`. They can be layered
     * on top of hill views using a `FrameLayout` or `RelativeLayout`.
     */
    private val moleImageViews = mutableListOf<ImageView>()

    /**
     * The `viewModel` provides the data for the game and handles the game logic.
     * It is lazily initialized to ensure that it is created only when needed.
     */
    private val viewModel: WhackAMoleGameViewModel by lazy {
        val prefs = getSharedPreferences("WhackAMolePrefs", MODE_PRIVATE)
        val repository = SharedPrefGameRepository(prefs)
        val scheduler = AndroidScheduler(mainLooper)
        WhackAMoleGameViewModel(repository, scheduler)
    }

    /**
     * Called when the activity is first created. This is where you should do all of your normal
     * static set up: create views, bind data to lists, etc.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being
     * shut down then this Bundle contains the data it most recently supplied in
     * onSaveInstanceState(Bundle). Note: Otherwise it is null.
     */
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

    /**
     * Initializes the list of mole `ImageView`s and sets their click listeners.
     * This function assumes that the XML layout contains `ImageView`s with IDs
     * `mole_0` through `mole_8`.
     */
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

    /**
     * Called when a mole `ImageView` is clicked. This function delegates the hit logic
     * to the `GameViewModel`.
     *
     * @param moleId The ID of the mole that was clicked (0-8).
     */
    private fun onMoleWhacked(moleId: Int) {
        viewModel.hitMole(moleId)
    }

    /**
     * Updates a single mole's `ImageView` based on its state, including its visibility
     * and color. This function is called for all nine moles each time the `MoleContainer`
     * changes.
     *
     * @param mole The `Mole` object containing the ID, visibility, and color information.
     */
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

    /**
     * Called when the game is over. This function displays the final score and provides
     * options to restart the game or return to the main menu.
     */
    private fun endGame() {
        val finalScore = viewModel.score.value ?: 0
        val highScore = viewModel.highScore.value ?: 0

        val message = if (finalScore > highScore) {
            "New High Score: $finalScore!"
        } else {
            "Game Over! Score: $finalScore\nHigh Score: $highScore"
        }

        androidx.appcompat.app.AlertDialog.Builder(this, R.style.DialogueTheme)
            .setTitle("Game Over")
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("Restart") { dialog, _ ->
                dialog.dismiss()
                restartGame()
            }
            .setNegativeButton("Main Menu") { dialog, _ ->
                dialog.dismiss()
                val intent = Intent(this, WhackAMoleTitleActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finish()
            }
            .show()
    }

    /**
     * Restarts the game by calling the `resetGame` function on the `GameViewModel`.
     * This function can be connected to a "Restart" button in the UI.
     */
    fun restartGame() {
        viewModel.resetGame()
    }
}
