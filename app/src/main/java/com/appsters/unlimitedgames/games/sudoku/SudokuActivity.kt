package com.appsters.unlimitedgames.games.sudoku

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.appsters.unlimitedgames.R

class SudokuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sudoku)
        
        // Load the menu fragment on first launch
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SudokuMenuFragment())
                .commit()
        }
    }
}
