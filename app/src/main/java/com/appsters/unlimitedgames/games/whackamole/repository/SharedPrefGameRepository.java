package com.appsters.unlimitedgames.games.whackamole.repository;

import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.appsters.unlimitedgames.games.whackamole.repository.GameRepository;

/**
 * SharedPrefGameRepository is the production implementation of GameRepository.
 * It stores and retrieves the high score using SharedPreferences.
 */
public class SharedPrefGameRepository implements GameRepository {

    private static final String KEY_HIGH_SCORE = "HIGH_SCORE";

    private final SharedPreferences prefs;
    private final MutableLiveData<Integer> highScore;

    /**
     * Constructor
     *
     * @param prefs SharedPreferences instance for persistent storage
     */
    public SharedPrefGameRepository(SharedPreferences prefs) {
        this.prefs = prefs;
        this.highScore = new MutableLiveData<>(prefs.getInt(KEY_HIGH_SCORE, 0));
    }

    /**
     * Get the stored high score as LiveData
     *
     * @return LiveData of the current high score
     */
    @Override
    public LiveData<Integer> getHighScore() {
        return highScore;
    }

    /**
     * Save a new high score and update LiveData
     *
     * @param score the new high score to save
     */
    @Override
    public void saveHighScore(int score) {
        prefs.edit()
                .putInt(KEY_HIGH_SCORE, score)
                .apply();
        highScore.setValue(score);
    }
}
