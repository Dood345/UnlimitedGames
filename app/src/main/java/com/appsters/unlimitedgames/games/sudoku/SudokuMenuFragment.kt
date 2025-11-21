package com.appsters.unlimitedgames.games.sudoku

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import com.appsters.unlimitedgames.R
import com.appsters.unlimitedgames.games.sudoku.repository.SudokuRepository

/**
 * A [Fragment] that displays the Sudoku game menu.
 * This fragment allows the user to choose a difficulty level and start a new game.
 * It also provides access to ranked games and a free-play mode.
 */
class SudokuMenuFragment : Fragment() {

    private var selectedColor: Int = Color.BLACK
    private lateinit var colorPickers: List<View>
    private lateinit var repository: SudokuRepository
    private lateinit var loadingIndicator: ProgressBar

    /**
     * Inflates the layout for this fragment.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        repository = SudokuRepository(requireContext())
        selectedColor = repository.getLastColor()
        return inflater.inflate(R.layout.fragment_sudoku_menu, container, false)
    }

    /**
     * Called when the fragment's view has been created.
     * Sets up click listeners for the difficulty buttons.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingIndicator = view.findViewById(R.id.loading_indicator)
        setupMenuUI(view)
        setupColorPicker(view)
    }

    override fun onResume() {
        super.onResume()
        checkSavedGame()
    }

    private fun checkSavedGame() {
        val resumeButton = view?.findViewById<Button>(R.id.btn_resume_game)
        if (repository.hasSavedGame()) {
            resumeButton?.visibility = View.VISIBLE
            resumeButton?.setOnClickListener { resumeGame() }
        } else {
            resumeButton?.visibility = View.GONE
        }
    }

    private fun setupMenuUI(view: View) {
        val rgDifficulty = view.findViewById<RadioGroup>(R.id.rg_difficulty)
        val rgMode = view.findViewById<RadioGroup>(R.id.rg_mode)
        val btnStart = view.findViewById<Button>(R.id.btn_start_game)

        // Update difficulty texts with high scores
        view.findViewById<RadioButton>(R.id.rb_easy).text = "Easy (HS: ${repository.getHighScore(Difficulty.EASY)})"
        view.findViewById<RadioButton>(R.id.rb_medium).text = "Medium (HS: ${repository.getHighScore(Difficulty.MEDIUM)})"
        view.findViewById<RadioButton>(R.id.rb_hard).text = "Hard (HS: ${repository.getHighScore(Difficulty.HARD)})"
        view.findViewById<RadioButton>(R.id.rb_expert).text = "Expert (HS: ${repository.getHighScore(Difficulty.EXPERT)})"

        btnStart.setOnClickListener {
            val difficulty = when (rgDifficulty.checkedRadioButtonId) {
                R.id.rb_medium -> Difficulty.MEDIUM
                R.id.rb_hard -> Difficulty.HARD
                R.id.rb_expert -> Difficulty.EXPERT
                else -> Difficulty.EASY
            }
            val isRanked = rgMode.checkedRadioButtonId == R.id.rb_ranked
            startGame(difficulty, isRanked)
        }
    }

    private fun setupColorPicker(view: View) {
        colorPickers = listOf(
            view.findViewById(R.id.color_black),
            view.findViewById(R.id.color_blue),
            view.findViewById(R.id.color_red),
            view.findViewById(R.id.color_green)
        )

        // Set the initially selected color based on repository
        val colorToSelectId = when (selectedColor) {
            Color.BLUE -> R.id.color_blue
            Color.RED -> R.id.color_red
            Color.GREEN -> R.id.color_green
            else -> R.id.color_black
        }

        // could be handled with states in resource (state_selected) but would not be animated
        colorPickers.forEach { picker ->
            val scale = if (picker.id == colorToSelectId) 1f else 0.54f
            picker.scaleX = scale
            picker.scaleY = scale
            picker.setOnClickListener { onColorSelected(it) }
        }
    }

    private fun onColorSelected(view: View) {
        // could be handled with states in resource (state_selected) but would not be animated
        colorPickers.forEach {
            val scale = if (it == view) 1f else 0.54f
            it.animate().scaleX(scale).scaleY(scale).setDuration(150).start()
        }

        selectedColor = when (view.id) {
            R.id.color_blue -> Color.BLUE
            R.id.color_red -> Color.RED
            R.id.color_green -> Color.GREEN
            else -> Color.BLACK
        }
        repository.saveLastColor(selectedColor)
    }

    /**
     * Starts a new game with the specified difficulty.
     * Replaces the current fragment with a [SudokuGameFragment].
     *
     * @param difficulty The selected difficulty for the new game.
     * @param isRanked Whether the game should be ranked or not.
     */
    private fun resumeGame() {
        val savedState = repository.getSavedGameState() ?: return
        loadingIndicator.visibility = View.VISIBLE
        
        // We need to pass the saved state to the fragment. 
        // Since SudokuGameFragment.newInstance only takes difficulty/color, 
        // we might need to modify it or handle it differently.
        // For now, let's use the existing factory/ViewModel logic in the fragment to load it?
        // Actually, better to pass a flag or just let the fragment check for saved game?
        // No, the user explicitly clicked "Resume".
        
        // Let's modify start game to accept an optional state or just launch the fragment 
        // and let the fragment ask the repository if it should load a saved game?
        // But we want to be explicit.
        
        // Let's just launch the fragment with the saved difficulty/color
        // and then tell the ViewModel to load the saved state.
        // But we can't easily tell the ViewModel from here without a shared ViewModel or passing arguments.
        
        // Simplest approach: Pass a "resume" flag to the fragment.
        
        val colorRes = when (selectedColor) {
            Color.BLUE -> R.color.blue
            Color.RED -> R.color.red
            Color.GREEN -> R.color.green
            else -> R.color.sudoku_board_text_color
        }
        
        val fragment = SudokuGameFragment.newInstance(savedState.difficulty, colorRes, savedState.isRanked, true)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun startGame(difficulty: Difficulty, isRanked: Boolean) {
        loadingIndicator.visibility = View.VISIBLE
        // Clear any previous saved game when starting a new one
        repository.clearSavedGame()
        
        val colorRes = when (selectedColor) {
            Color.BLUE -> R.color.blue
            Color.RED -> R.color.red
            Color.GREEN -> R.color.green
            else -> R.color.sudoku_board_text_color
        }
        val fragment = SudokuGameFragment.newInstance(difficulty, colorRes, isRanked, false)
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
    }
}
