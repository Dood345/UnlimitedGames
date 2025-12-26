package com.appsters.simpleGames.games.sudoku

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.appsters.simpleGames.R

/**
 * The main activity for the Sudoku game.
 * This activity hosts the different fragments of the game, such as the menu and the game board.
 * It also handles window insets to ensure the UI doesn't overlap with system elements.
 */
class SudokuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_sudoku)

        val fragmentContainer = findViewById<android.view.View>(R.id.fragment_container)

        ViewCompat.setOnApplyWindowInsetsListener(fragmentContainer) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            windowInsets
        }

        // Load the menu fragment on first launch
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SudokuMenuFragment())
                .commit()
        }
    }
}
