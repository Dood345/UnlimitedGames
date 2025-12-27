package com.appsters.simpleGames.app.managers;

import android.content.Context;

import com.appsters.simpleGames.games.game2048.Game2048Game;
import com.appsters.simpleGames.games.interfaces.IGame;
import com.appsters.simpleGames.games.maze.MazeGame;
import com.appsters.simpleGames.games.soccerseparationgame.SoccerSeparationGame;
import com.appsters.simpleGames.games.sudoku.SudokuGame;
import com.appsters.simpleGames.games.whackamole.WhackAMoleGame;

import java.util.ArrayList;
import java.util.List;

public class GameRegistry {
    public static List<IGame> getRegisteredGames(Context context) {
        List<IGame> games = new ArrayList<>();
        games.add(new Game2048Game(context));
        games.add(new WhackAMoleGame(context));
        games.add(new SudokuGame(context));
        games.add(new MazeGame(context));
        games.add(new SoccerSeparationGame(context));
        return games;
    }
}
