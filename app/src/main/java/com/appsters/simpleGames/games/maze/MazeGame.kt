package com.appsters.simpleGames.games.maze

import android.content.Context
import com.appsters.simpleGames.games.interfaces.IGame
import com.appsters.simpleGames.games.maze.controller.RunManager

class MazeGame(private val context: Context) : IGame {
    override fun clearUserData() {
        RunManager.clearAllData(context)
    }
}
