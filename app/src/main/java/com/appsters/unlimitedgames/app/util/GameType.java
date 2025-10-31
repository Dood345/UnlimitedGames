package com.appsters.unlimitedgames.app.util;

import androidx.annotation.IdRes;

import com.appsters.unlimitedgames.R;

/**
 * Represents the different types of games available in the application.
 */
public enum GameType {
    GAME_2048("2048", R.id.action_homeFragment_to_game2048Fragment),
    POKER("Poker", R.id.action_homeFragment_to_pokerFragment),
    SUDOKU("Sudoku", R.id.action_homeFragment_to_sudokuFragment),
    NFL_QUIZ("NFL Quiz", R.id.action_homeFragment_to_NFLQuizFragment),
    WHACK_A_MOLE("Whack-a-Mole", R.id.action_homeFragment_to_whackAMoleFragment);

    private final String displayName;
    private final int actionId;

    /**
     * Constructs a new GameType with the given display name and action ID.
     *
     * @param displayName The user-friendly name of the game.
     * @param actionId    The ID of the navigation action to launch the game.
     */
    GameType(String displayName, @IdRes int actionId) {
        this.displayName = displayName;
        this.actionId = actionId;
    }

    /**
     * Gets the user-friendly name of the game.
     *
     * @return The display name of the game.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the ID of the navigation action to launch the game.
     *
     * @return The action ID.
     */
    public int getActionId() {
        return actionId;
    }
}
