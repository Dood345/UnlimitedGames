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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import com.appsters.unlimitedgames.R
import com.appsters.unlimitedgames.app.util.TypewriterView
import com.appsters.unlimitedgames.games.sudoku.model.Score
import com.appsters.unlimitedgames.games.sudoku.repository.SudokuRepository
import com.appsters.unlimitedgames.games.sudoku.view.SudokuBoardView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.xml.KonfettiView
import java.util.concurrent.TimeUnit

class SudokuGameFragment : Fragment(), SudokuBoardView.OnCellSelectedListener {

    private lateinit var viewModel: SudokuViewModel
    private lateinit var difficulty: SudokuMenuFragment.Difficulty
    private var selectedColor: Int = Color.BLACK
    private var isRanked: Boolean = true
    private lateinit var sudokuBoardView: SudokuBoardView
    private lateinit var timerTextView: TextView
    private lateinit var numberButtons: List<Button>
    private lateinit var konfettiView: KonfettiView

    companion object {
        private const val ARG_DIFFICULTY = "difficulty"
        private const val ARG_COLOR = "color"
        private const val ARG_IS_RANKED = "is_ranked"
        private const val ARG_SHOULD_RESUME = "should_resume"

        fun newInstance(
            difficulty: SudokuMenuFragment.Difficulty,
            colorRes: Int,
            isRanked: Boolean = true,
            shouldResume: Boolean = false
        ): SudokuGameFragment {
            return SudokuGameFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_DIFFICULTY, difficulty.name)
                    putInt(ARG_COLOR, colorRes)
                    putBoolean(ARG_IS_RANKED, isRanked)
                    putBoolean(ARG_SHOULD_RESUME, shouldResume)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            difficulty = SudokuMenuFragment.Difficulty.valueOf(it.getString(ARG_DIFFICULTY) ?: "EASY")
            val colorRes = it.getInt(ARG_COLOR, R.color.sudoku_board_text_color)
            selectedColor = ContextCompat.getColor(requireContext(), colorRes)
            isRanked = it.getBoolean(ARG_IS_RANKED, true)
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

        sudokuBoardView.setPlayerColor(selectedColor)

        setupButtonClickListeners(view)
        observeViewModel()

        val shouldResume = arguments?.getBoolean(ARG_SHOULD_RESUME, false) ?: false
        if (shouldResume) {
            val savedState = SudokuRepository(requireContext()).getSavedGameState(difficulty)
            if (savedState != null && !savedState.isCompleted) {
                viewModel.resumeGame(savedState)
            } else {
                viewModel.startNewGame(difficulty, isRanked)
            }
        } else {
            viewModel.startNewGame(difficulty, isRanked)
        }
    }

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
            button.setOnLongClickListener {
                viewModel.toggleImpossibleNumber(index + 1)
                true
            }
        }

        view.findViewById<Button>(R.id.btn_clear)?.setOnClickListener { viewModel.clearCell() }
    }

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

        viewModel.impossibleNumbers.observe(viewLifecycleOwner) { impossibleNumbers ->
            updateNumberButtonColors(impossibleNumbers)
        }

        viewModel.invalidMoveEvent.observe(viewLifecycleOwner) { number ->
            flashButton(numberButtons[number - 1])
        }

        viewModel.gameCompletedEvent.observe(viewLifecycleOwner) { score ->
            showConfetti()
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                showAnimatedCompletionDialog(score)
            }, 1000)
        }
    }

    private fun updateNumberButtonColors(impossibleNumbers: Set<Int>) {
        numberButtons.forEachIndexed { index, button ->
            val number = index + 1
            if (impossibleNumbers.contains(number)) {
                button.backgroundTintList = ColorStateList.valueOf(Color.RED)
            } else {
                button.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.purple_500))
            }
        }
    }

    private fun flashButton(button: Button) {
        val originalTint = button.backgroundTintList
        button.backgroundTintList = ColorStateList.valueOf(Color.RED)
        button.postDelayed({
            button.backgroundTintList = originalTint
        }, 300)
    }

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

    private fun showAnimatedCompletionDialog(score: Score) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_animated_text, null)
        val typewriterView = dialogView.findViewById<TypewriterView>(R.id.typewriter_text)

        val dialog = AlertDialog.Builder(requireContext(), R.style.SudokuDialogTheme)
            .setTitle("Puzzle Solved!")
            .setView(dialogView)
            .setPositiveButton("Awesome!") { _, _ ->
                viewModel.deleteSavedGame()
                parentFragmentManager.popBackStack()
            }
            .setCancelable(false)
            .create()

        typewriterView.setCharacterDelay(50)
        typewriterView.animateText(score.getScoreBreakdown())

        dialog.show()
    }

    override fun onCellSelected(row: Int, col: Int) {
        viewModel.onCellSelected(row, col)
    }

    override fun onResume() {
        super.onResume()
        viewModel.resumeTimer()
    }

    override fun onPause() {
        super.onPause()
        viewModel.saveGame()
    }
}
