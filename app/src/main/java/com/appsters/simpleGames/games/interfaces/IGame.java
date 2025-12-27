package com.appsters.simpleGames.games.interfaces;

public interface IGame {
    /**
     * Clears all user-specific data for the game.
     * This is called when the user logs out.
     */
    void clearUserData();

    /**
     * Syncs the high score from remote to local if applicable.
     * Default implementation does nothing.
     *
     * @param context      Context to access SharedPreferences
     * @param remoteScores List of all remote scores for the user
     */
    default void syncHighScore(android.content.Context context,
            java.util.List<com.appsters.simpleGames.app.data.model.Score> remoteScores) {
        // Default implementation: do nothing
    }
}
