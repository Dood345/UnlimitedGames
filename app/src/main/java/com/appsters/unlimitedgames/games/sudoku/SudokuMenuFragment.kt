package com.appsters.unlimitedgames.games.sudoku

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.appsters.unlimitedgames.R

/**
 * A [Fragment] that displays the Sudoku game menu.
 * This fragment allows the user to choose a difficulty level and start a new game.
 * It also provides access to ranked games and a free-play mode.
 */
class SudokuMenuFragment : Fragment() {

    private var selectedColor: Int = Color.BLACK
    private lateinit var colorPickers: List<View>

    /**
     * Inflates the layout for this fragment.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sudoku_menu, container, false)
    }

    /**
     * Called when the fragment's view has been created.
     * Sets up click listeners for the difficulty buttons.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDifficultyButtons(view)
        setupColorPicker(view)
    }

    private fun setupDifficultyButtons(view: View) {
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

    private fun setupColorPicker(view: View) {
        colorPickers = listOf(
            view.findViewById(R.id.color_black),
            view.findViewById(R.id.color_blue),
            view.findViewById(R.id.color_red),
            view.findViewById(R.id.color_green)
        )

        // Set black as the default selected color
        colorPickers[0].isSelected = true

        colorPickers.forEach { picker ->
            picker.setOnClickListener { onColorSelected(it) }
        }
    }

    private fun onColorSelected(view: View) {
        // Deselect all other pickers
        colorPickers.forEach { it.isSelected = false }
        // Select the clicked one
        view.isSelected = true

        selectedColor = when (view.id) {
            R.id.color_blue -> Color.BLUE
            R.id.color_red -> Color.RED
            R.id.color_green -> Color.GREEN
            else -> Color.BLACK
        }
    }

    /**
     * Starts a new game with the specified difficulty.
     * Replaces the current fragment with a [SudokuGameFragment].
     *
     * @param difficulty The selected difficulty for the new game.
     */
    private fun startGame(difficulty: Difficulty) {
        val fragment = SudokuGameFragment.newInstance(difficulty, selectedColor)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    /**
     * Represents the different difficulty levels for the Sudoku game.
     * Each difficulty has a multiplier for scoring and a number of given cells.
     */
    enum class Difficulty(val multiplier: Double, val givens: Int) {
        EASY(1.0, 45),
        MEDIUM(1.5, 35),
        HARD(2.0, 28),
        EXPERT(3.0, 22),
        FREE_PLAY(0.0, 0);

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
