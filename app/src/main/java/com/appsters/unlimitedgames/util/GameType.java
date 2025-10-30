package com.appsters.unlimitedgames.util;

/**
 * Represents the different types of games available in the application.
 */
public enum GameType {
    GAME_2048("2048"),
    POKER("Poker"),
    SUDOKU("Sudoku"),
    NFL_QUIZ("NFL Quiz"),
    WHACK_A_MOLE("Whack-a-Mole");

    private final String displayName;

    /**
     * Constructs a new GameType with the given display name.
     *
     * @param displayName The user-friendly name of the game.
     */
    GameType(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Gets the user-friendly name of the game.
     *
     * @return The display name of the game.
     */
    public String getDisplayName() {
        return displayName;
    }
}
