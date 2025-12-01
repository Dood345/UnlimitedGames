package com.appsters.unlimitedgames.games.maze.controller

import android.content.Context
import android.util.Log
import com.appsters.unlimitedgames.games.maze.model.Player
import com.appsters.unlimitedgames.games.maze.model.PowerUpType
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object RunManager {
    // Run State
    var totalMoney: Int = 0
    var totalXP: Int = 0
    var currentLevel: Int = 1
    var roundNumber: Int = 1
    var isRunInProgress: Boolean = false

    // Player Instance
    val player = Player()

    fun startNewRun() {
        totalMoney = 0
        totalXP = 0
        currentLevel = 1
        roundNumber = 1
        isRunInProgress = true
        Log.d("RunManager", "New Run Started. Round: $roundNumber")

        // Reset player stats
        player.reset()

        currentLevelXP = 0
        xpToNextLevel = GameConfig.XP_PER_LEVEL_BASE
    }

    fun nextRound() {
        roundNumber++
        Log.d("RunManager", "Round incremented to: $roundNumber")
        // Potential difficulty scaling here
    }

    fun addXP(amount: Int): Boolean {
        totalXP += amount
        currentLevelXP += amount
        return checkLevelUp()
    }

    // Internal tracker for bar progress
    var currentLevelXP: Int = 0
    var xpToNextLevel: Int = GameConfig.XP_PER_LEVEL_BASE

    private const val SAVE_FILE_NAME = "maze_save.json"

    fun saveGame(context: Context, mazeState: JSONObject?) {
        try {
            val root = JSONObject()

            // 1. Save Run Stats
            val runStats = JSONObject()
            runStats.put("totalMoney", totalMoney)
            runStats.put("totalXP", totalXP)
            runStats.put("currentLevel", currentLevel)
            runStats.put("roundNumber", roundNumber)
            runStats.put("currentLevelXP", currentLevelXP)
            runStats.put("xpToNextLevel", xpToNextLevel)
            root.put("run_stats", runStats)

            // 2. Save Player Stats
            val playerStats = JSONObject()
            playerStats.put("maxStamina", player.maxStamina)
            playerStats.put("currentStamina", player.currentStamina)
            playerStats.put("skillPoints", player.skillPoints)
            playerStats.put("isWallSmashUnlocked", player.isWallSmashUnlocked)

            // Save Active Effects
            val effectsArray = JSONArray()
            for (effect in player.activeEffects) {
                val effectObj = JSONObject()
                effectObj.put("type", effect.type.name)
                effectObj.put("duration", effect.remainingDuration)
                effectsArray.put(effectObj)
            }
            playerStats.put("active_effects", effectsArray)

            root.put("player_stats", playerStats)

            // 3. Save Maze State (passed from ViewModel)
            if (mazeState != null) {
                root.put("maze_state", mazeState)
            }

            // Write to file
            val file = File(context.filesDir, SAVE_FILE_NAME)
            file.writeText(root.toString())
            Log.d("RunManager", "Game saved to ${file.absolutePath}")

        } catch (e: Exception) {
            Log.e("RunManager", "Error saving game", e)
        }
    }

    fun loadGame(context: Context): JSONObject? {
        try {
            val file = File(context.filesDir, SAVE_FILE_NAME)
            if (!file.exists()) return null

            val jsonString = file.readText()
            val root = JSONObject(jsonString)

            // 1. Restore Run Stats
            if (root.has("run_stats")) {
                val runStats = root.getJSONObject("run_stats")
                totalMoney = runStats.optInt("totalMoney", 0)
                totalXP = runStats.optInt("totalXP", 0)
                currentLevel = runStats.optInt("currentLevel", 1)
                roundNumber = runStats.optInt("roundNumber", 1)
                currentLevelXP = runStats.optInt("currentLevelXP", 0)
                xpToNextLevel = runStats.optInt("xpToNextLevel", GameConfig.XP_PER_LEVEL_BASE)
                isRunInProgress = true
            }

            // 2. Restore Player Stats
            if (root.has("player_stats")) {
                val playerStats = root.getJSONObject("player_stats")
                player.maxStamina = playerStats.optDouble("maxStamina", 100.0).toFloat()
                player.currentStamina = playerStats.optDouble("currentStamina", 100.0).toFloat()
                player.skillPoints = playerStats.optInt("skillPoints", 0)
                player.isWallSmashUnlocked = playerStats.optBoolean("isWallSmashUnlocked", false)

                // Restore Active Effects
                player.activeEffects.clear()
                val effectsArray = playerStats.optJSONArray("active_effects")
                if (effectsArray != null) {
                    for (i in 0 until effectsArray.length()) {
                        val effectObj = effectsArray.optJSONObject(i) ?: continue
                        val typeName = effectObj.optString("type")
                        val duration = effectObj.optLong("duration")
                        try {
                            val type = PowerUpType.valueOf(typeName)
                            player.activeEffects.add(Player.ActiveEffect(type, duration))
                        } catch (e: Exception) {
                            // Ignore invalid enum
                        }
                    }
                }
            }

            // Return the maze part for the ViewModel to handle
            return root.optJSONObject("maze_state")

        } catch (e: Exception) {
            Log.e("RunManager", "Error loading game", e)
            return null
        }
    }

    fun hasSavedGame(context: Context): Boolean {
        val file = File(context.filesDir, SAVE_FILE_NAME)
        return file.exists()
    }

    fun clearSavedGame(context: Context) {
        try {
            val file = File(context.filesDir, SAVE_FILE_NAME)
            if (file.exists()) {
                file.delete()
            }
            isRunInProgress = false
        } catch (e: Exception) {
            Log.e("RunManager", "Error clearing save", e)
        }
    }

    private fun checkLevelUp(): Boolean {
        if (currentLevelXP >= xpToNextLevel) {
            currentLevelXP -= xpToNextLevel
            currentLevel++

            // Apply Bonus
            player.maxStamina += GameConfig.LEVEL_UP_STAMINA_BONUS
            player.skillPoints++ // Award Skill Point

            // Calc next threshold
            xpToNextLevel = (xpToNextLevel * GameConfig.XP_SCALING_FACTOR).toInt()

            Log.d(
                "RunManager",
                "Level Up! New Level: $currentLevel, Max Stamina: ${player.maxStamina}, SP: ${player.skillPoints}"
            )
            return true
        }
        return false
    }
}