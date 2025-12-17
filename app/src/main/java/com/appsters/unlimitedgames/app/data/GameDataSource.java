package com.appsters.unlimitedgames.app.data;

import com.appsters.unlimitedgames.R;
import com.appsters.unlimitedgames.app.data.model.Game;
import com.appsters.unlimitedgames.app.util.GameType;

import java.util.ArrayList;
import java.util.List;

/**
 * A data source for providing a list of games.
 */
public class GameDataSource {

        /**
         * Gets a list of available games.
         *
         * @return A list of {@link Game} objects.
         */
        public static List<Game> getGames() {
                List<Game> games = new ArrayList<>();
                games.add(
                                new Game(
                                                "1",
                                                GameType.GAME2048,
                                                "2048",
                                                "",
                                                R.id.action_homeFragment_to_game2048Fragment,
                                                R.drawable.game2048_logo));

                games.add(
                                new Game(
                                                "3",
                                                GameType.SUDOKU,
                                                "Sudoku",
                                                "",
                                                0,
                                                R.drawable.ic_sudoku));
                games.add(
                                new Game(
                                                "4",
                                                GameType.SOCCERSEPARATION,
                                                "Soccer Separation Game",
                                                "",
                                                R.id.action_homeFragment_to_SoccerSeparationGameFragment,
                                                R.drawable.ic_soccer_separation));
                games.add(
                                new Game(
                                                "5",
                                                GameType.WHACK_A_MOLE,
                                                "Whack-a-Mole",
                                                "",
                                                0,
                                                R.drawable.mole_transparent));
                games.add(
                                new Game(
                                                "6",
                                                GameType.MAZE,
                                                "Maze",
                                                "",
                                                R.id.action_homeFragment_to_mazeMenuFragment,
                                                R.drawable.ic_maze));
                games.add(
                                new Game(
                                                "7",
                                                GameType.POKER,
                                                "Poker",
                                                "",
                                                R.id.action_homeFragment_to_pokerFragment,
                                                R.drawable.ic_poker));

                return games;
        }

        /**
         * Clears all game data for all games.
         *
         * @param context The application context.
         */
        public static void clearAllGameData(android.content.Context context) {
                // Use IGame implementations to ensure consistent cleanup across the app
                List<com.appsters.unlimitedgames.games.interfaces.IGame> games = new ArrayList<>();
                games.add(new com.appsters.unlimitedgames.games.game2048.Game2048Game(context));
                games.add(new com.appsters.unlimitedgames.games.sudoku.SudokuGame(context));
                games.add(new com.appsters.unlimitedgames.games.maze.MazeGame(context));
                games.add(new com.appsters.unlimitedgames.games.whackamole.WhackAMoleGame(context));
                games.add(new com.appsters.unlimitedgames.games.soccerseparationgame.SoccerSeparationGame(context));

                for (com.appsters.unlimitedgames.games.interfaces.IGame game : games) {
                        game.clearUserData();
                }
        }
}
