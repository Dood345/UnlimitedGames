package com.appsters.unlimitedgames.games.maze

import android.content.Context
import com.appsters.unlimitedgames.games.interfaces.IGame
import com.appsters.unlimitedgames.games.maze.controller.RunManager

class MazeGame(private val context: Context) : IGame {
    override fun clearUserData() {
        RunManager.clearAllData(context)
    }
}
