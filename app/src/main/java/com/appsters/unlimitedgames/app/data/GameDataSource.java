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
                        GameType.PUZZLE,
                        "2048",
                        "",
                        R.id.action_homeFragment_to_game2048Fragment,
                        R.drawable.ic_launcher_background));
        games.add(
                new Game(
                        "2",
                        GameType.CARD,
                        "Poker",
                        "",
                        R.id.action_homeFragment_to_pokerFragment,
                        R.drawable.ic_launcher_background));
        games.add(
                new Game(
                        "3",
                        GameType.PUZZLE,
                        "Sudoku",
                        "",
                        R.id.action_homeFragment_to_sudokuFragment,
                        R.drawable.ic_sudoku));
        games.add(
                new Game(
                        "4",
                        GameType.QUIZ,
                        "NFL Quiz",
                        "",
                        R.id.action_homeFragment_to_NFLQuizFragment,
                        R.drawable.ic_launcher_background));
        games.add(
                new Game(
                        "5",
                        GameType.ACTION,
                        "Whack-a-Mole",
                        "",
                        0,
                        R.drawable.mole_transparent));
        return games;
    }
}
