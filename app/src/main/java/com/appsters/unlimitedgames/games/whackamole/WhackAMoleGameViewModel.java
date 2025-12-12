package com.appsters.unlimitedgames.games.whackamole;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.appsters.unlimitedgames.app.data.model.Score;
import com.appsters.unlimitedgames.app.data.repository.LeaderboardRepository;
import com.appsters.unlimitedgames.app.data.repository.UserRepository;
import com.appsters.unlimitedgames.app.util.GameType;
import com.appsters.unlimitedgames.games.whackamole.model.GameConfig;
import com.appsters.unlimitedgames.games.whackamole.model.MoleContainer;
import com.appsters.unlimitedgames.games.whackamole.repository.GameRepository;
import com.appsters.unlimitedgames.games.whackamole.util.Scheduler;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;
import java.util.Random;

/**
 * Manages the core logic, state, and data for the Whack-a-Mole game screen.
 * <p>
 * This ViewModel is responsible for:
 * <ul>
 * <li>Starting and stopping the game loop that spawns moles.</li>
 * <li>Tracking the player's score, misses, and the current high score.</li>
 * <li>Handling user interactions, such as hitting a mole.</li>
 * <li>Managing the game-over state.</li>
 * <li>Persisting the high score using a {@link GameRepository}.</li>
 * <li>Resetting the game to a fresh state.</li>
 * </ul>
 * It exposes game state to the UI (the Activity) via {@link LiveData} objects,
 * ensuring that the
 * UI is always in sync with the underlying game data and that the logic is
 * decoupled from the view.
 */
public class WhackAMoleGameViewModel extends androidx.lifecycle.AndroidViewModel {

    private final Random random = new Random();

    private final GameConfig gameConfig;
    private final GameRepository gameRepository;
    private final LeaderboardRepository leaderboardRepository;
    private final UserRepository userRepository;
    private final Scheduler scheduler;
    private final Runnable spawnRunnable = this::spawnMole;
    private final LiveData<Integer> highScore;
    private final MutableLiveData<Integer> score;
    private final MutableLiveData<Boolean> gameOver;
    private final MutableLiveData<MoleContainer> moles;
    private final MutableLiveData<Integer> misses;
    private final MutableLiveData<Long> moleTimeRemaining;
    private final Runnable moleTimerRunnable = this::onMoleTimerTick;
    private long currentInterval;
    private boolean scoreSubmitted;

    /**
     * Constructs a GameViewModel with a default game configuration.
     *
     * @param gameRepository The repository for handling high score persistence.
     * @param scheduler      The scheduler for managing timed events like mole
     *                       spawning.
     */
    public WhackAMoleGameViewModel(android.app.Application application, GameRepository gameRepository,
            Scheduler scheduler) {
        this(application, gameRepository, scheduler, GameConfig.DEFAULT);
    }

    /**
     * Constructs a GameViewModel with a custom game configuration.
     *
     * @param gameRepository The repository for handling high score persistence.
     * @param scheduler      The scheduler for managing timed events.
     * @param gameConfig     The configuration defining game rules (e.g., number of
     *                       moles, miss limit).
     */
    public WhackAMoleGameViewModel(android.app.Application application, GameRepository gameRepository,
            Scheduler scheduler,
            GameConfig gameConfig) {
        super(application);
        this.gameConfig = gameConfig;
        this.gameRepository = gameRepository;
        this.leaderboardRepository = new LeaderboardRepository(application);
        this.userRepository = new UserRepository();
        this.scheduler = scheduler;

        this.highScore = gameRepository.getHighScore();

        this.score = new MutableLiveData<>(0);
        this.gameOver = new MutableLiveData<>(false);
        this.moles = new MutableLiveData<>(
                new MoleContainer(gameConfig.getNumMoles(),
                        random.nextInt(gameConfig.getNumMoles())));
        this.misses = new MutableLiveData<>(0);
        this.moleTimeRemaining = new MutableLiveData<>(gameConfig.getInitialInterval());

        this.currentInterval = gameConfig.getInitialInterval();
        this.scoreSubmitted = false;

        scheduler.postDelayed(spawnRunnable, gameConfig.getInitialInterval());
        scheduler.postDelayed(moleTimerRunnable, 100);
    }

    /**
     * Core game loop action. This method is responsible for advancing the game
     * state when a mole is missed.
     * It increments the miss counter, checks for game-over conditions, and then
     * selects a new mole
     * to be visible. It also dynamically adjusts the spawn interval to increase
     * difficulty.
     * Finally, it schedules the next call to itself.
     *
     * @throws IllegalStateException if called after the game is already over.
     */
    private void spawnMole() {
        if (Boolean.TRUE.equals(gameOver.getValue())) {
            return; // Game is already over, do nothing.
        }

        MoleContainer currentMoles = Objects.requireNonNull(moles.getValue());

        // Increment miss because previous mole was not hit
        int currentMisses = Objects.requireNonNull(misses.getValue());
        misses.setValue(currentMisses + 1);

        if (misses.getValue() >= gameConfig.getMaxMisses()) {
            stopGameLoop();
            return;
        }

        // Pick new mole ID avoiding previous one
        int newVisibleId = random.nextInt(gameConfig.getNumMoles() - 1);
        newVisibleId = newVisibleId >= currentMoles.getVisibleId() ? newVisibleId + 1 : newVisibleId;

        moles.setValue(new MoleContainer(gameConfig.getNumMoles(), newVisibleId));

        // Speed up interval
        currentInterval = Math.max(gameConfig.getMinInterval(),
                currentInterval - gameConfig.getIntervalDecrement());

        // Schedule next spawn
        scheduler.removeCallbacks(spawnRunnable);
        scheduler.postDelayed(spawnRunnable, currentInterval);
        moleTimeRemaining.setValue(currentInterval);
        scheduler.removeCallbacks(moleTimerRunnable);
        scheduler.postDelayed(moleTimerRunnable, 100);
    }

    private void onMoleTimerTick() {
        long currentTime = Objects.requireNonNull(moleTimeRemaining.getValue());
        if (currentTime > 0) {
            moleTimeRemaining.setValue(currentTime - 100);
            scheduler.postDelayed(moleTimerRunnable, 100);
        }
    }

    private void stopGameLoop() {
        gameOver.setValue(true);
        scheduler.removeCallbacks(spawnRunnable);
        scheduler.removeCallbacks(moleTimerRunnable);

        Integer finalScore = score.getValue();
        if (finalScore != null) {
            submitScoreToLeaderboard(finalScore);
        }
    }

    private void submitScoreToLeaderboard(int scoreValue) {
        if (scoreSubmitted)
            return; // Prevent multiple submissions
        scoreSubmitted = true;

        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null)
            return;

        userRepository.getUser(userId, task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                String username = task.getResult().getUsername();
                Score scoreObject = new Score(null, userId, username, GameType.WHACK_A_MOLE, scoreValue,
                        task.getResult().getPrivacy());
                leaderboardRepository.submitScore(scoreObject, (isSuccessful, result, e) -> {
                    // Optionally handle success or failure, e.g., log an error
                });
            }
        });
    }

    /**
     * Processes a user's tap on a mole.
     * If the correct mole is hit, the score is incremented, the high score is
     * updated if necessary,
     * and the game loop is reset for the next mole. If the wrong mole is hit, the
     * action is ignored.
     *
     * @param moleId The ID of the mole that was tapped.
     * @throws IllegalStateException if called after the game is already over.
     */
    public void hitMole(int moleId) {
        if (Boolean.TRUE.equals(gameOver.getValue())) {
            return;
        }

        MoleContainer currentMoles = Objects.requireNonNull(moles.getValue());
        int currentScore = Objects.requireNonNull(score.getValue());
        Integer currentHighScore = highScore.getValue(); // Can be null initially

        if (currentMoles.getVisibleId() != moleId) {
            return;
        }

        // Increment score
        int newScore = currentScore + currentMoles.getMoles().get(moleId).getColor().getPoints();
        score.setValue(newScore);

        // Update high score if needed
        if (currentHighScore == null || newScore > currentHighScore) {
            gameRepository.saveHighScore(newScore);
        }

        // Pick new mole ID
        int newVisibleId = random.nextInt(gameConfig.getNumMoles() - 1);
        newVisibleId = newVisibleId >= currentMoles.getVisibleId() ? newVisibleId + 1 : newVisibleId;
        moles.setValue(new MoleContainer(gameConfig.getNumMoles(), newVisibleId));

        // Speed up interval
        currentInterval = Math.max(gameConfig.getMinInterval(),
                currentInterval - gameConfig.getIntervalDecrement());

        // Reset spawn timer
        scheduler.removeCallbacks(spawnRunnable);
        scheduler.postDelayed(spawnRunnable, currentInterval);
        moleTimeRemaining.setValue(currentInterval);
        scheduler.removeCallbacks(moleTimerRunnable);
        scheduler.postDelayed(moleTimerRunnable, 100);
    }

    /**
     * Resets the game to its initial state, allowing the player to start a new
     * session.
     * This method should only be called after the game is over.
     * It resets the score, misses, and mole positions, and restarts the spawn
     * scheduler.
     *
     * @throws IllegalStateException if called while the game is still active.
     */
    public void resetGame() {
        boolean isGameOver = Objects.requireNonNull(gameOver.getValue());
        if (!isGameOver) {
            throw new IllegalStateException("resetGame should only be called after game over.");
        }

        misses.setValue(0);
        currentInterval = gameConfig.getInitialInterval();
        scoreSubmitted = false; // Reset the flag for the new game

        score.setValue(0);
        gameOver.setValue(false);
        moles.setValue(new MoleContainer(gameConfig.getNumMoles(), random.nextInt(
                gameConfig.getNumMoles())));
        moleTimeRemaining.setValue(gameConfig.getInitialInterval());

        scheduler.postDelayed(spawnRunnable, currentInterval);
        scheduler.postDelayed(moleTimerRunnable, 100);
    }

    /**
     * @return A LiveData stream of the {@link MoleContainer}, which holds the state
     *         of all moles.
     *         The UI observes this to draw the moles on the screen.
     */
    public LiveData<MoleContainer> getMoles() {
        return moles;
    }

    /**
     * @return A LiveData stream of the current score.
     *         The UI observes this to display the player's score in real-time.
     */
    public LiveData<Integer> getScore() {
        return score;
    }

    /**
     * @return A LiveData stream of the persisted high score.
     *         The UI observes this to display the all-time high score.
     */
    public LiveData<Integer> getHighScore() {
        return highScore;
    }

    /**
     * @return A LiveData stream indicating whether the game is over.
     *         The UI observes this to show or hide game-over screens or dialogs.
     */
    public LiveData<Boolean> getGameOver() {
        return gameOver;
    }

    /**
     * @return A LiveData stream of the current number of misses.
     *         The UI observes this to show the player how many misses they have
     *         left.
     */
    public LiveData<Integer> getMisses() {
        return misses;
    }

    public LiveData<Long> getMoleTimeRemaining() {
        return moleTimeRemaining;
    }

    /**
     * This method is called when the ViewModel is about to be destroyed.
     * It cleans up resources by removing any pending callbacks from the scheduler,
     * preventing memory leaks and stopping the game loop after the ViewModel is no
     * longer in use.
     */
    @Override
    protected void onCleared() {
        scheduler.removeCallbacksAndMessages(null);
    }
}
