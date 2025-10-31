package com.appsters.unlimitedgames.games.whackamole

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.appsters.unlimitedgames.R
import com.appsters.unlimitedgames.games.whackamole.repository.SharedPrefGameRepository
import com.appsters.unlimitedgames.games.whackamole.WhackAMoleTitleViewModel

class WhackAMoleTitleActivity : AppCompatActivity() {

    private val mainViewModel: WhackAMoleTitleViewModel by lazy {
        val prefs = getSharedPreferences("WhackAMolePrefs", MODE_PRIVATE)
        val repository = SharedPrefGameRepository(prefs)
        WhackAMoleTitleViewModel(repository)
    }

    private lateinit var highScoreTextView: TextView;

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
    }

    override fun onResume() {
        super.onResume()
        mainViewModel.highScore.observe(this, Observer { highScore ->
            highScoreTextView.text = "High Score: $highScore"
        })
    }
}