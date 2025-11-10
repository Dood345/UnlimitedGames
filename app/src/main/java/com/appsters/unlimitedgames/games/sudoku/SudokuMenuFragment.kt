package com.appsters.unlimitedgames.games.sudoku

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.appsters.unlimitedgames.R

class SudokuMenuFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sudoku_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.btn_easy).setOnClickListener {
            startGame(Difficulty.EASY)
        }

        view.findViewById<Button>(R.id.btn_medium).setOnClickListener {
            startGame(Difficulty.MEDIUM)
        }

        view.findViewById<Button>(R.id.btn_hard).setOnClickListener {
            startGame(Difficulty.HARD)
        }

        view.findViewById<Button>(R.id.btn_expert).setOnClickListener {
            startGame(Difficulty.EXPERT)
        }

        view.findViewById<Button>(R.id.btn_free_play).setOnClickListener {
            startGame(Difficulty.FREE_PLAY)
        }
    }

    private fun startGame(difficulty: Difficulty) {
        val fragment = SudokuGameFragment.newInstance(difficulty)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    enum class Difficulty(val multiplier: Double, val givens: Int) {
        EASY(1.0, 45),      // More given numbers, easier puzzle
        MEDIUM(1.5, 35),    // Moderate difficulty
        HARD(2.0, 28),      // Fewer givens, harder
        EXPERT(3.0, 22),    // Very few givens, expert level
        FREE_PLAY(0.0, 0);  // Empty board, no scoring

        fun isRanked(): Boolean = this != FREE_PLAY

        fun getDisplayName(): String = when(this) {
            EASY -> "Easy"
            MEDIUM -> "Medium"
            HARD -> "Hard"
            EXPERT -> "Expert"
            FREE_PLAY -> "Free Play"
        }
    }
}
