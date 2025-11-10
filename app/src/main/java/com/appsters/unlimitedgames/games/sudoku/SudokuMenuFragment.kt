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
    }
    
    private fun startGame(difficulty: Difficulty) {
        val fragment = SudokuGameFragment.newInstance(difficulty)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
    
    enum class Difficulty {
        EASY, MEDIUM, HARD
    }
}
