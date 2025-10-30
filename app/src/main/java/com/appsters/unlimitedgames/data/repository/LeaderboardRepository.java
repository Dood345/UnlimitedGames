package com.appsters.unlimitedgames.data.repository;

import com.appsters.unlimitedgames.data.model.Score;
import com.appsters.unlimitedgames.util.GameType;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

/**
 * A repository for managing leaderboard data in Firestore.
 * This class provides methods for retrieving global and friends leaderboards, as well as
 * submitting new scores.
 */
public class LeaderboardRepository {
    private FirebaseFirestore db;

    /**
     * Constructs a new LeaderboardRepository and initializes the Firestore instance.
     */
    public LeaderboardRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    // TODO: Implement these methods

    /**
     * Gets the global leaderboard for a given game.
     *
     * @param gameType The type of game for which to retrieve the leaderboard.
     * @param limit    The maximum number of scores to retrieve.
     * @param listener A listener that will be called with a list of {@link Score} objects
     *                 representing the global leaderboard, or an exception if the operation
     *                 fails.
     */
    public void getGlobalLeaderboard(GameType gameType, int limit, OnCompleteListener<List<Score>> listener) {}

    /**
     * Gets the leaderboard for a user's friends for a given game.
     *
     * @param userId   The ID of the user whose friends' leaderboard to retrieve.
     * @param gameType The type of game for which to retrieve the leaderboard.
     * @param listener A listener that will be called with a list of {@link Score} objects
     *                 representing the friends leaderboard, or an exception if the operation
     *                 fails.
     */
    public void getFriendsLeaderboard(String userId, GameType gameType, OnCompleteListener<List<Score>> listener) {}

    /**
     * Submits a new score to the leaderboard.
     *
     * @param score    The score to submit.
     * @param listener A listener that will be called when the operation is complete.
     */
    public void submitScore(Score score, OnCompleteListener<Void> listener) {}
}
