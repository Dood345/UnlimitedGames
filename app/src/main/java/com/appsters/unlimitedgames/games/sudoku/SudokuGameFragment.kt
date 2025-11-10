package com.appsters.unlimitedgames.games.sudoku

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.appsters.unlimitedgames.R
import com.appsters.unlimitedgames.games.sudoku.view.SudokuBoardView

class SudokuGameFragment : Fragment(), SudokuBoardView.OnCellSelectedListener {

    private lateinit var viewModel: SudokuViewModel
    private lateinit var difficulty: SudokuMenuFragment.Difficulty
    private lateinit var sudokuBoardView: SudokuBoardView
    private lateinit var timerTextView: TextView

    companion object {
        private const val ARG_DIFFICULTY = "difficulty"

        fun newInstance(difficulty: SudokuMenuFragment.Difficulty): SudokuGameFragment {
            return SudokuGameFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_DIFFICULTY, difficulty.name)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        difficulty = SudokuMenuFragment.Difficulty.valueOf(
            arguments?.getString(ARG_DIFFICULTY) ?: "EASY"
        )
        viewModel = ViewModelProvider(this)[SudokuViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sudoku_game, container, false)
        sudokuBoardView = view.findViewById(R.id.sudoku_board_view)
        timerTextView = view.findViewById(R.id.tv_timer)
        sudokuBoardView.setOnCellSelectedListener(this)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupButtonClickListeners(view)
        observeViewModel()
        
        viewModel.createNewGame(difficulty)
    }

    private fun setupButtonClickListeners(view: View) {
        // Set up number button listeners (1-9)
        view.findViewById<Button>(R.id.btn_number_1)?.setOnClickListener { viewModel.enterValue(1) }
        view.findViewById<Button>(R.id.btn_number_2)?.setOnClickListener { viewModel.enterValue(2) }
        view.findViewById<Button>(R.id.btn_number_3)?.setOnClickListener { viewModel.enterValue(3) }
        view.findViewById<Button>(R.id.btn_number_4)?.setOnClickListener { viewModel.enterValue(4) }
        view.findViewById<Button>(R.id.btn_number_5)?.setOnClickListener { viewModel.enterValue(5) }
        view.findViewById<Button>(R.id.btn_number_6)?.setOnClickListener { viewModel.enterValue(6) }
        view.findViewById<Button>(R.id.btn_number_7)?.setOnClickListener { viewModel.enterValue(7) }
        view.findViewById<Button>(R.id.btn_number_8)?.setOnClickListener { viewModel.enterValue(8) }
        view.findViewById<Button>(R.id.btn_number_9)?.setOnClickListener { viewModel.enterValue(9) }
        
        // Add a clear/erase button
        view.findViewById<Button>(R.id.btn_clear)?.setOnClickListener { viewModel.clearCell() }
    }

    private fun observeViewModel() {
        viewModel.gameState.observe(viewLifecycleOwner) { gameState ->
            sudokuBoardView.setBoard(gameState.board)
            if (gameState.isCompleted) {
                // TODO: Show game over dialog with score
            }
        }

        viewModel.timer.observe(viewLifecycleOwner) { time ->
            timerTextView.text = time
        }

        viewModel.selectedCell.observe(viewLifecycleOwner) { cell ->
            sudokuBoardView.setSelectedCell(cell)
        }
    }

    override fun onCellSelected(row: Int, col: Int) {
        viewModel.onCellSelected(row, col)
    }
}
