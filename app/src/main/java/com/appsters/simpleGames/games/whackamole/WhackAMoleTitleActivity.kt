package com.appsters.simpleGames.games.whackamole

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.appsters.simpleGames.R
import com.appsters.simpleGames.games.whackamole.repository.SharedPrefGameRepository

/**
 * Title activity that initializes and manages the Whack-a-Mole game.
 *
 * @author Jesutofunmi Obimakinde, Rand Roman, Daniel Ripley
 */
class WhackAMoleTitleActivity : AppCompatActivity() {

    private val mainViewModel: WhackAMoleTitleViewModel by lazy {
        val prefs = getSharedPreferences("WhackAMolePrefs", MODE_PRIVATE)
        val repository = SharedPrefGameRepository(prefs)
        WhackAMoleTitleViewModel(repository)
    }

    private lateinit var highScoreTextView: TextView;

    /**
     * Initializes the activity, setting up the user interface and view model.
     * This function is called when the activity is first created. It inflates the layout,
     * finds UI elements, and sets up listeners for the start and clear score buttons.
     * It also observes the high score from the view model to keep the display updated.
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     * this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     * Otherwise, it is null.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.whack_a_mole_title)

        highScoreTextView = findViewById<TextView>(R.id.high_score)
        val startButton = findViewById<Button>(R.id.start_button)
        val clearScoreButton = findViewById<Button>(R.id.clear_score_button)

        startButton.setOnClickListener {
            val intent = Intent(this, WhackAMoleGameActivity::class.java)
            startActivity(intent)
        }

        clearScoreButton.setOnClickListener {
            mainViewModel.clearHighScore()
        }

        mainViewModel.highScore.observe(this, Observer { highScore ->
            highScoreTextView.text = "High Score: $highScore"
        })

        com.appsters.simpleGames.app.util.SoundManager.init(this)
        val muteButton = findViewById<android.widget.ImageButton>(R.id.btn_mute)
        val prefs = getSharedPreferences("WhackAMolePrefs", MODE_PRIVATE)
        updateMuteButtonIcon(muteButton, prefs)
        muteButton.setOnClickListener {
            com.appsters.simpleGames.app.util.SoundManager.toggleMute(prefs)
            updateMuteButtonIcon(muteButton, prefs)
        }

        findViewById<android.widget.ImageButton>(R.id.btn_help).setOnClickListener {
            com.google.android.material.dialog.MaterialAlertDialogBuilder(this, R.style.HelpDialogTheme)
                .setTitle("How to Play")
                .setMessage("Instructions: Tap moles to score! Avoid empty holes.\n\nTip: Be quick, they disappear fast!")
                .setPositiveButton("Got it", null)
                .show()
        }
    }

    private fun updateMuteButtonIcon(button: android.widget.ImageButton, prefs: android.content.SharedPreferences) {
        if (com.appsters.simpleGames.app.util.SoundManager.isMuted(prefs)) {
            button.setImageResource(R.drawable.ic_volume_off)
        } else {
            button.setImageResource(R.drawable.ic_volume_up)
        }
    }

    /**
     * Called when returning to main window after finishing a round.
     * At this point, the activity is at the top of the activity stack, with user input going to it.
     * This implementation ensures the high score is fresh every time the user returns to the main screen.
     */
    override fun onResume() {
        super.onResume()
        mainViewModel.highScore.observe(this, Observer { highScore ->
            highScoreTextView.text = "High Score: $highScore"
        })
    }
}