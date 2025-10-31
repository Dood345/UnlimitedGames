package com.appsters.unlimitedgames.games.whackamole;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

public class WhackAMoleTitleViewModel extends ViewModel {
    private final GameRepository gameRepository;
    private final LiveData<Integer> highScore;

    /**
     * Constructor
     *
     * @param gameRepository repository used to persist/load high score
     */
    public WhackAMoleTitleViewModel(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
        this.highScore = gameRepository.getHighScore(); // directly use repo LiveData
    }

    /**
     * Clears the high score by setting it to 0 in the repository.
     */
    public void clearHighScore() {
        gameRepository.saveHighScore(0);
    }

    /**
     * Returns the current high score as LiveData.
     *
     * @return LiveData<Integer> representing high score
     */
    public LiveData<Integer> getHighScore() {
        return highScore;
    }
}
