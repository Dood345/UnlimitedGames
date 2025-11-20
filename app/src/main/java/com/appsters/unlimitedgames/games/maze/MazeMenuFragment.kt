package com.appsters.unlimitedgames.games.maze

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.appsters.unlimitedgames.R

class MazeMenuFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_maze_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvStats = view.findViewById<android.widget.TextView>(R.id.tv_last_run_stats)
        val btnContinue = view.findViewById<Button>(R.id.btn_start_maze)

        if (RunManager.isRunInProgress) {
            tvStats.text = "Current Run: Round ${RunManager.roundNumber} - $${RunManager.totalMoney}"
            btnContinue.isEnabled = true
            btnContinue.alpha = 1.0f
        } else {
            tvStats.text = "No current run"
            btnContinue.isEnabled = false
            btnContinue.alpha = 0.5f
        }

        btnContinue.setOnClickListener {
            val intent = Intent(requireContext(), MazeGameActivity::class.java)
            startActivity(intent)
        }

        view.findViewById<Button>(R.id.btn_new_run).setOnClickListener {
            RunManager.startNewRun()
            val intent = Intent(requireContext(), MazeGameActivity::class.java)
            startActivity(intent)
        }
    }
}
