package com.appsters.unlimitedgames.games.maze

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment

class UpgradeFragment : DialogFragment() {

    private lateinit var tvCurrency: TextView
    private lateinit var btnStamina: Button
    private lateinit var btnSpeed: Button
    private lateinit var btnEfficiency: Button
    private lateinit var btnMainMenu: Button
    private lateinit var btnNextLevel: Button

    var onNextLevelListener: (() -> Unit)? = null
    var onMainMenuListener: (() -> Unit)? = null
    var onUpgradeListener: (() -> Unit)? = null

    private var isGameOver = false

    fun setGameOverState(isGameOver: Boolean) {
        this.isGameOver = isGameOver
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(com.appsters.unlimitedgames.R.layout.fragment_upgrade, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvCurrency = view.findViewById(com.appsters.unlimitedgames.R.id.tv_available_currency)
        btnStamina = view.findViewById(com.appsters.unlimitedgames.R.id.btn_upgrade_stamina)
        btnSpeed = view.findViewById(com.appsters.unlimitedgames.R.id.btn_upgrade_speed)
        btnEfficiency = view.findViewById(com.appsters.unlimitedgames.R.id.btn_upgrade_efficiency)
        btnMainMenu = view.findViewById(com.appsters.unlimitedgames.R.id.btn_main_menu)
        btnNextLevel = view.findViewById(com.appsters.unlimitedgames.R.id.btn_next_level)

        if (isGameOver) {
            btnNextLevel.text = "New Run"
            btnNextLevel.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FF9800")) // Orange for retry
        } else {
            btnNextLevel.text = "Next Level"
            btnNextLevel.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#4CAF50")) // Green for next
        }

        updateUI()

        btnStamina.setOnClickListener {
            if (RunManager.totalMoney >= GameConfig.COST_UPGRADE_STAMINA) {
                RunManager.totalMoney -= GameConfig.COST_UPGRADE_STAMINA
                RunManager.player.maxStamina += GameConfig.UPGRADE_STAMINA_AMOUNT
                updateUI()
                onUpgradeListener?.invoke()
            } else {
                Toast.makeText(context, "Not enough money!", Toast.LENGTH_SHORT).show()
            }
        }

        btnSpeed.setOnClickListener {
            if (RunManager.totalMoney >= GameConfig.COST_UPGRADE_SPEED) {
                RunManager.totalMoney -= GameConfig.COST_UPGRADE_SPEED
                RunManager.player.baseSpeed += GameConfig.UPGRADE_SPEED_AMOUNT
                updateUI()
                onUpgradeListener?.invoke()
            } else {
                Toast.makeText(context, "Not enough money!", Toast.LENGTH_SHORT).show()
            }
        }

        btnEfficiency.setOnClickListener {
            if (RunManager.totalMoney >= GameConfig.COST_UPGRADE_EFFICIENCY) {
                RunManager.totalMoney -= GameConfig.COST_UPGRADE_EFFICIENCY
                RunManager.player.staminaDrainRate *= GameConfig.UPGRADE_EFFICIENCY_MULTIPLIER
                updateUI()
                onUpgradeListener?.invoke()
            } else {
                Toast.makeText(context, "Not enough money!", Toast.LENGTH_SHORT).show()
            }
        }

        btnMainMenu.setOnClickListener {
            dismiss()
            onMainMenuListener?.invoke()
        }

        btnNextLevel.setOnClickListener {
            dismiss()
            onNextLevelListener?.invoke()
        }
    }

    private fun updateUI() {
        tvCurrency.text = "Money: $${RunManager.totalMoney}  |  XP: ${RunManager.totalXP}"
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }
}
