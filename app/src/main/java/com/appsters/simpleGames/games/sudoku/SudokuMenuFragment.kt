package com.appsters.simpleGames.games.sudoku

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
import com.appsters.simpleGames.R
import com.appsters.simpleGames.games.sudoku.repository.SudokuRepository

/**
 * The main menu fragment for the Sudoku game.
 * Allows the user to select difficulty, game mode (Ranked/Free Play), and player color.
 * It also displays high scores and handles resuming saved games.
 */
class SudokuMenuFragment : Fragment() {

    private var selectedColor: Int = Color.BLACK
    private lateinit var colorPickers: List<View>
    private lateinit var repository: SudokuRepository
    private lateinit var loadingIndicator: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        repository = SudokuRepository(requireContext())
        selectedColor = repository.getLastColor()
        return inflater.inflate(R.layout.fragment_sudoku_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingIndicator = view.findViewById(R.id.loading_indicator)
        setupMenuUI(view)
        setupColorPicker(view)
    }

    override fun onResume() {
        super.onResume()
        val rgDifficulty = view?.findViewById<RadioGroup>(R.id.rg_difficulty)
        val selectedDifficulty = when (rgDifficulty?.checkedRadioButtonId) {
            R.id.rb_medium -> Difficulty.MEDIUM
            R.id.rb_hard -> Difficulty.HARD
            R.id.rb_expert -> Difficulty.EXPERT
            else -> Difficulty.EASY
        }
        checkSavedGame(selectedDifficulty)
    }

    private fun checkSavedGame(difficulty: Difficulty) {
        val resumeButton = view?.findViewById<Button>(R.id.btn_resume_game)
        val savedState = repository.getSavedGameState(difficulty)

        if (savedState != null && !savedState.isCompleted) {
            val mode = if (savedState.isRanked) "Ranked" else "Free Play"
            resumeButton?.text = "Resume Game ($mode)"
            resumeButton?.isEnabled = true
            resumeButton?.alpha = 1.0f
            resumeButton?.setOnClickListener { resumeGame(difficulty) }
        } else {
            resumeButton?.text = "Resume Game"
            resumeButton?.isEnabled = false
            resumeButton?.alpha = 0.5f
            resumeButton?.setOnClickListener(null)
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

        rgDifficulty.setOnCheckedChangeListener { _, checkedId ->
            val difficulty = when (checkedId) {
                R.id.rb_medium -> Difficulty.MEDIUM
                R.id.rb_hard -> Difficulty.HARD
                R.id.rb_expert -> Difficulty.EXPERT
                else -> Difficulty.EASY
            }
            checkSavedGame(difficulty)
        }

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

        com.appsters.simpleGames.app.util.SoundManager.init(requireContext())
        val muteButton = view.findViewById<android.widget.ImageButton>(R.id.btn_mute)
        val prefs = requireContext().getSharedPreferences("sudoku_prefs", android.content.Context.MODE_PRIVATE)
        updateMuteButtonIcon(muteButton, prefs)
        muteButton.setOnClickListener {
            com.appsters.simpleGames.app.util.SoundManager.toggleMute(prefs)
            updateMuteButtonIcon(muteButton, prefs)
        }

        view.findViewById<android.widget.ImageButton>(R.id.btn_help).setOnClickListener {
            com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext(), R.style.HelpDialogTheme)
                .setTitle("How to Play")
                .setMessage("Instructions: Fill grid with 1-9. No repeats in rows or columns or boxes.\n\nTip: Tap and hold on numbers to mark impossibilities!")
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

    private fun setupColorPicker(view: View) {
        colorPickers = listOf(
            view.findViewById(R.id.color_black),
            view.findViewById(R.id.color_blue),
            view.findViewById(R.id.color_red),
            view.findViewById(R.id.color_green)
        )

        val colorToSelectId = when (selectedColor) {
            Color.BLUE -> R.id.color_blue
            Color.RED -> R.id.color_red
            Color.GREEN -> R.id.color_green
            else -> R.id.color_black
        }

        colorPickers.forEach { picker ->
            val scale = if (picker.id == colorToSelectId) 1f else 0.54f
            picker.scaleX = scale
            picker.scaleY = scale
            picker.setOnClickListener { onColorSelected(it) }
        }
    }

    private fun onColorSelected(view: View) {
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

    private fun resumeGame(difficulty: Difficulty) {
        val savedState = repository.getSavedGameState(difficulty) ?: return
        loadingIndicator.visibility = View.VISIBLE
        
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
        repository.clearSavedGame(difficulty)
        
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
     * Represents the difficulty levels of the Sudoku game.
     *
     * @property multiplier The score multiplier for this difficulty.
     * @property givens The number of pre-filled cells (clues) provided at the start.
     */
    enum class Difficulty(val multiplier: Double, val givens: Int) {
        EASY(1.0, 45),
        MEDIUM(1.5, 35),
        HARD(2.0, 28),
        EXPERT(3.0, 22),
    }
}
