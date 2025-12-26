package com.appsters.simpleGames.games.whackamole.repository;

import androidx.lifecycle.LiveData;

/**
 * GameRepository defines the contract for storing and retrieving
 * the high score in the Whack-a-Mole game.
 * <p>
 * Implementations can use different storage mechanisms, e.g.,
 * SharedPreferences, database, or in-memory for testing.
 */
public interface GameRepository {

    /**
     * Returns the current high score.
     *
     * @return the high score as an int. Defaults to 0 if no score is saved.
     */
    LiveData<Integer> getHighScore();

    /**
     * Updates the high score with the provided value.
     *
     * @param score the new high score to save
     */
    void saveHighScore(int score);

    /**
     * Clears all game data.
     */
    void clearData();
}
