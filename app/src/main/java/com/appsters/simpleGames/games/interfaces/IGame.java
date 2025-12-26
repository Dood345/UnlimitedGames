package com.appsters.simpleGames.games.interfaces;

public interface IGame {
    /**
     * Clears all user-specific data for the game.
     * This is called when the user logs out.
     */
    void clearUserData();
}
