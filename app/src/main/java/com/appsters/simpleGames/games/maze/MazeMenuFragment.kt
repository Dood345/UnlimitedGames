package com.appsters.simpleGames.games.maze

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.appsters.simpleGames.R
import com.appsters.simpleGames.games.maze.controller.RunManager

class MazeMenuFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_maze_menu, container, false)
    }

    override fun onResume() {
        super.onResume()
        updateMenuState()
    }

    private fun updateMenuState() {
        val view = view ?: return
        val tvStats = view.findViewById<android.widget.TextView>(R.id.tv_last_run_stats)
        val btnContinue = view.findViewById<Button>(R.id.btn_start_maze)

        if (RunManager.isRunInProgress || RunManager.hasSavedGame(requireContext())) {
            if (RunManager.isRunInProgress) {
                 tvStats.text = "Current Run: Round ${RunManager.roundNumber} - $${RunManager.totalMoney}"
            } else {
                 tvStats.text = "Saved Run Available"
            }
            btnContinue.isEnabled = true
            btnContinue.alpha = 1.0f
        } else {
            tvStats.text = "No current run"
            btnContinue.isEnabled = false
            btnContinue.alpha = 0.5f
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        updateMenuState()

        com.appsters.simpleGames.app.util.SoundManager.init(requireContext())
        val muteButton = view.findViewById<android.widget.ImageButton>(R.id.btn_mute)
        val prefs = requireContext().getSharedPreferences("maze_prefs", android.content.Context.MODE_PRIVATE)
        updateMuteButtonIcon(muteButton, prefs)
        muteButton.setOnClickListener {
            com.appsters.simpleGames.app.util.SoundManager.toggleMute(prefs)
            updateMuteButtonIcon(muteButton, prefs)
        }

        view.findViewById<android.widget.ImageButton>(R.id.btn_help).setOnClickListener {
            com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext(), R.style.HelpDialogTheme)
                .setTitle("How to Play")
                .setMessage("Instructions: Navigate to the end to begin again. Try to avoid the walls\n\nTip: Cyan = Exp, Gold = Money, Green = Stamina, Purple = Vision.")
                .setPositiveButton("Got it", null)
                .show()
        }

        view.findViewById<Button>(R.id.btn_start_maze).setOnClickListener {
            val intent = Intent(requireContext(), MazeGameActivity::class.java)
            intent.putExtra("EXTRA_CONTINUE_RUN", true)
            startActivity(intent)
        }

        view.findViewById<Button>(R.id.btn_new_run).setOnClickListener {
            RunManager.clearSavedGame(requireContext())
            RunManager.startNewRun()
            val intent = Intent(requireContext(), MazeGameActivity::class.java)
            startActivity(intent)
        }

    }

    private fun updateMuteButtonIcon(button: android.widget.ImageButton, prefs: android.content.SharedPreferences) {
        if (com.appsters.simpleGames.app.util.SoundManager.isMuted(prefs)) {
            button.setImageResource(R.drawable.ic_volume_off)
        } else {
            button.setImageResource(R.drawable.ic_volume_up)
        }
    }
}
