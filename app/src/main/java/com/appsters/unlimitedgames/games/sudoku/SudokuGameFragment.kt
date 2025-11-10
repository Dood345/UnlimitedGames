package com.appsters.unlimitedgames.games.sudoku

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import com.appsters.unlimitedgames.R
import com.appsters.unlimitedgames.app.ui.custom.TypewriterView
import com.appsters.unlimitedgames.games.sudoku.model.Score
import com.appsters.unlimitedgames.games.sudoku.repository.SudokuRepository
import com.appsters.unlimitedgames.games.sudoku.view.SudokuBoardView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.xml.KonfettiView
import java.util.concurrent.TimeUnit

/**
 * A [Fragment] that displays the main Sudoku game screen.
 * This fragment contains the Sudoku board, timer, and number input controls.
 * It communicates with a [SudokuViewModel] to manage the game state.
 */
class SudokuGameFragment : Fragment(), SudokuBoardView.OnCellSelectedListener {

    private lateinit var viewModel: SudokuViewModel
    private lateinit var difficulty: SudokuMenuFragment.Difficulty
    private var playerColor: Int = Color.BLACK
    private lateinit var sudokuBoardView: SudokuBoardView
    private lateinit var timerTextView: TextView
    private lateinit var numberButtons: List<Button>
    private lateinit var konfettiView: KonfettiView

    companion object {
        private const val ARG_DIFFICULTY = "difficulty"
        private const val ARG_COLOR = "color"

        /**
         * Creates a new instance of the fragment with the specified difficulty and color.
         */
        fun newInstance(difficulty: SudokuMenuFragment.Difficulty, color: Int): SudokuGameFragment {
            return SudokuGameFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_DIFFICULTY, difficulty.name)
                    putInt(ARG_COLOR, color)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            difficulty = SudokuMenuFragment.Difficulty.valueOf(it.getString(ARG_DIFFICULTY) ?: "EASY")
            playerColor = it.getInt(ARG_COLOR, Color.BLACK)
        }
        val repository = SudokuRepository(requireContext())
        val factory = SudokuViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[SudokuViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sudoku_game, container, false)
        sudokuBoardView = view.findViewById(R.id.sudoku_board_view)
        timerTextView = view.findViewById(R.id.tv_timer)
        konfettiView = view.findViewById(R.id.konfetti_view)
        sudokuBoardView.setOnCellSelectedListener(this)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        sudokuBoardView.setPlayerColor(playerColor)
        
        setupButtonClickListeners(view)
        observeViewModel()
        
        viewModel.createNewGame(difficulty)
    }

    /**
     * Sets up click listeners for all the number and control buttons.
     */
    private fun setupButtonClickListeners(view: View) {
        numberButtons = listOf(
            view.findViewById(R.id.btn_number_1),
            view.findViewById(R.id.btn_number_2),
            view.findViewById(R.id.btn_number_3),
            view.findViewById(R.id.btn_number_4),
            view.findViewById(R.id.btn_number_5),
            view.findViewById(R.id.btn_number_6),
            view.findViewById(R.id.btn_number_7),
            view.findViewById(R.id.btn_number_8),
            view.findViewById(R.id.btn_number_9)
        )

        numberButtons.forEachIndexed { index, button ->
            button.setOnClickListener { viewModel.enterValue(index + 1) }
        }
        
        view.findViewById<Button>(R.id.btn_clear)?.setOnClickListener { viewModel.clearCell() }
    }

    /**
     * Observes [LiveData] from the [SudokuViewModel] to update the UI.
     */
    private fun observeViewModel() {
        viewModel.gameState.observe(viewLifecycleOwner) { gameState ->
            sudokuBoardView.setBoard(gameState.board)
        }

        viewModel.timer.observe(viewLifecycleOwner) { time ->
            timerTextView.text = time
        }

        viewModel.selectedCell.observe(viewLifecycleOwner) { cell ->
            sudokuBoardView.setSelectedCell(cell)
        }

        viewModel.invalidMoveEvent.observe(viewLifecycleOwner) { number ->
            flashButton(numberButtons[number - 1])
        }

        viewModel.gameCompletedEvent.observe(viewLifecycleOwner) { score ->
            showConfetti()
            showAnimatedCompletionDialog(score)
        }
    }

    /**
     * Animates a button with a red flash to indicate an invalid move.
     */
    private fun flashButton(button: Button) {
        val originalTint = button.backgroundTintList
        button.backgroundTintList = ColorStateList.valueOf(Color.RED)
        button.postDelayed({
            button.backgroundTintList = originalTint
        }, 300) // Restore after 0.3 seconds
    }

    /**
     * Triggers a burst of confetti to celebrate solving the puzzle.
     */
    private fun showConfetti() {
        val party = Party(
            speed = 0f,
            maxSpeed = 30f,
            damping = 0.9f,
            spread = 360,
            colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
            emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(100),
            position = nl.dionsegijn.konfetti.core.Position.Relative(0.5, 0.3)
        )
        konfettiView.start(party)
    }

    /**
     * Displays a dialog with the final score, animated with a typewriter effect.
     */
    private fun showAnimatedCompletionDialog(score: Score) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_animated_text, null)
        val typewriterView = dialogView.findViewById<TypewriterView>(R.id.typewriter_text)
        
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Puzzle Solved!")
            .setView(dialogView)
            .setPositiveButton("Awesome!") { _, _ ->
                parentFragmentManager.popBackStack()
            }
            .setCancelable(false)
            .create()

        typewriterView.setCharacterDelay(50)
        typewriterView.animateText(score.getScoreBreakdown())

        dialog.show()
    }

    /**
     * Called when a cell on the board is selected by the user.
     */
    override fun onCellSelected(row: Int, col: Int) {
        viewModel.onCellSelected(row, col)
    }
}
