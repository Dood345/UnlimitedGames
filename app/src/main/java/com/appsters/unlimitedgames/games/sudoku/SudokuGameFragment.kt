package com.appsters.unlimitedgames.games.sudoku

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.appsters.unlimitedgames.R

class SudokuGameFragment : Fragment() {
    
    private lateinit var viewModel: SudokuViewModel
    private lateinit var difficulty: SudokuMenuFragment.Difficulty
    
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
        return inflater.inflate(R.layout.fragment_sudoku_game, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // TODO: Initialize game board based on difficulty
        // TODO: Set up timer
        // TODO: Set up number buttons
        // TODO: Observe ViewModel LiveData
    }
}
