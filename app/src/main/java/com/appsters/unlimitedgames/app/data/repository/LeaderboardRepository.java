package com.appsters.unlimitedgames.app.data.repository;

import com.appsters.unlimitedgames.app.data.model.Friend;
import com.appsters.unlimitedgames.app.data.model.Score;
import com.appsters.unlimitedgames.app.data.model.User;
import com.appsters.unlimitedgames.app.util.GameType;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LeaderboardRepository {
    private FirebaseFirestore db;
    private FriendRepository friendRepository;
    private final android.content.Context context;
    private final com.google.gson.Gson gson;
    private static final String PENDING_SCORES_PREF = "PendingScores";

    public LeaderboardRepository(android.content.Context context) {
        this.context = context.getApplicationContext();
        this.db = FirebaseFirestore.getInstance();
        this.friendRepository = new FriendRepository();
        this.gson = new com.google.gson.Gson();

        retryPendingScores();
    }

    public void getGlobalLeaderboard(String currentUserId, GameType gameType, int limit,
            OnCompleteListener<List<Score>> listener) {
        // 1. Fetch scores
        OnCompleteListener<List<Score>> scoresListener = (isSuccess, scores, error) -> {
            if (!isSuccess || scores == null) {
                listener.onComplete(false, null, error);
                return;
            }

            // 2. Fetch current user's friends to check for FRIENDS_ONLY access
            friendRepository.getFriends(currentUserId, friendsTask -> {
                List<String> friendIds = new ArrayList<>();
                if (friendsTask.isSuccessful() && friendsTask.getResult() != null) {
                    for (Friend f : friendsTask.getResult()) {
                        friendIds.add(f.getFromUserId().equals(currentUserId) ? f.getToUserId() : f.getFromUserId());
                    }
                }
                friendIds.add(currentUserId); // I am my own friend for visibility

                // 3. Identify scores with missing privacy (Legacy Data)
                final List<Score> scoresWithPrivacy = new ArrayList<>();
                List<String> userIdsToFetch = new ArrayList<>();

                for (Score s : scores) {
                    if (s.getPrivacy() == null) {
                        userIdsToFetch.add(s.getUserId());
                    }
                }

                // Helper to finalize filtering
                OnCompleteListener<Map<String, User>> filterStep = (uSuccess, userMap, uError) -> {
                    // 4. Filter scores using denormalized privacy data (or fetched user data)
                    List<Score> filteredScores = new ArrayList<>();

                    for (Score s : scores) {
                        com.appsters.unlimitedgames.app.util.Privacy privacy = s.getPrivacy();

                        // If null, look up in fetched map
                        if (privacy == null && userMap != null) {
                            User u = userMap.get(s.getUserId());
                            if (u != null) {
                                privacy = u.getPrivacy();
                            }
                        }

                        // Default to PUBLIC only if we absolutely have no info (shouldn't happen if
                        // user exists)
                        // But if privacy is PRIVATE, it's not PUBLIC.
                        if (privacy == null) {
                            privacy = com.appsters.unlimitedgames.app.util.Privacy.PUBLIC;
                        }

                        boolean isPublic = privacy == com.appsters.unlimitedgames.app.util.Privacy.PUBLIC;
                        boolean isFriendsOnly = privacy == com.appsters.unlimitedgames.app.util.Privacy.FRIENDS_ONLY;
                        boolean isFriend = friendIds.contains(s.getUserId());

                        // Show if Public OR (FriendsOnly AND I am a friend/self)
                        if (isPublic || (isFriendsOnly && isFriend)) {
                            filteredScores.add(s);
                        }
                    }

                    // Re-rank
                    for (int i = 0; i < filteredScores.size(); i++) {
                        filteredScores.get(i).setRank(i + 1);
                    }

                    listener.onComplete(true, filteredScores, null);
                };

                // Execute fetch if needed
                if (!userIdsToFetch.isEmpty()) {
                    List<String> distinctIds = userIdsToFetch.stream().distinct().collect(Collectors.toList());
                    fetchUsers(distinctIds, (uSuccess, uMap, uError) -> {
                        // Even if fetch fails, we proceed with what we have (nulls becoming public per
                        // default safety fallback, or empty)
                        // Ideally we log error.
                        filterStep.onComplete(uSuccess, uMap, uError);
                    });
                } else {
                    filterStep.onComplete(true, new HashMap<>(), null);
                }
            });
        };

        if (gameType == GameType.ALL) {
            getAggregateLeaderboard(limit, scoresListener);
        } else {
            getSingleGameLeaderboard(gameType, limit, scoresListener);
        }
    }

    // Helper to fetch multiple users. No longer used for leaderboard filtering but
    // kept for other uses if needed.
    private void fetchUsers(List<String> userIds, OnCompleteListener<Map<String, User>> listener) {
        if (userIds.isEmpty()) {
            listener.onComplete(true, new HashMap<>(), null);
            return;
        }

        // Chunking
        List<com.google.android.gms.tasks.Task<QuerySnapshot>> tasks = new ArrayList<>();
        int batchSize = 10;
        for (int i = 0; i < userIds.size(); i += batchSize) {
            List<String> batch = userIds.subList(i, Math.min(i + batchSize, userIds.size()));
            tasks.add(db.collection("users").whereIn("uid", batch).get());
        }

        com.google.android.gms.tasks.Tasks.whenAllSuccess(tasks).addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Map<String, User> userMap = new HashMap<>();
                for (Object obj : task.getResult()) {
                    QuerySnapshot snap = (QuerySnapshot) obj;
                    for (DocumentSnapshot doc : snap) {
                        User u = doc.toObject(User.class);
                        if (u != null) {
                            userMap.put(u.getUserId(), u);
                        }
                    }
                }
                listener.onComplete(true, userMap, null);
            } else {
                listener.onComplete(false, null, task.getException());
            }
        });
    }

    public void updateUserPrivacy(String userId, com.appsters.unlimitedgames.app.util.Privacy newPrivacy,
            OnCompleteListener<Void> listener) {
        db.collection("scores")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        com.google.firebase.firestore.WriteBatch batch = db.batch();
                        for (DocumentSnapshot doc : task.getResult()) {
                            batch.update(doc.getReference(), "privacy", newPrivacy);
                        }
                        batch.commit().addOnCompleteListener(batchTask -> {
                            listener.onComplete(batchTask.isSuccessful(), null, batchTask.getException());
                        });
                    } else {
                        listener.onComplete(false, null, task.getException());
                    }
                });
    }

    public void deleteAllUserScores(String userId, OnCompleteListener<Void> listener) {
        db.collection("scores")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        com.google.firebase.firestore.WriteBatch batch = db.batch();
                        for (DocumentSnapshot doc : task.getResult()) {
                            batch.delete(doc.getReference());
                        }
                        batch.commit().addOnCompleteListener(batchTask -> {
                            listener.onComplete(batchTask.isSuccessful(), null, batchTask.getException());
                        });
                    } else {
                        listener.onComplete(false, null, task.getException());
                    }
                });
    }

    private void getSingleGameLeaderboard(GameType gameType, int limit, OnCompleteListener<List<Score>> listener) {
        Query query = db.collection("scores")
                .whereEqualTo("gameType", gameType.name())
                .orderBy("score", Query.Direction.DESCENDING)
                .limit(limit);

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<Score> scores = task.getResult().toObjects(Score.class);
                listener.onComplete(true, scores, null);
            } else {
                listener.onComplete(false, null, task.getException());
            }
        });
    }

    public void getUserScores(String userId, OnCompleteListener<List<Score>> listener) {
        db.collection("scores")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        listener.onComplete(true, task.getResult().toObjects(Score.class), null);
                    } else {
                        listener.onComplete(false, null, task.getException());
                    }
                });
    }

    private void getAggregateLeaderboard(int limit, OnCompleteListener<List<Score>> listener) {
        db.collection("scores").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<Score> allScores = task.getResult().toObjects(Score.class);

                Map<GameType, List<Score>> scoresByGame = allScores.stream()
                        .filter(s -> s.getGameType() != GameType.ALL)
                        .collect(Collectors.groupingBy(Score::getGameType));

                scoresByGame
                        .forEach((game, scores) -> scores.sort(Comparator.comparingInt(Score::getScore).reversed()));

                Map<String, int[]> userRankStats = new HashMap<>();
                Map<String, String> userIdToUsername = new HashMap<>();

                for (Map.Entry<GameType, List<Score>> entry : scoresByGame.entrySet()) {
                    List<Score> rankedScores = entry.getValue();
                    for (int i = 0; i < rankedScores.size(); i++) {
                        Score score = rankedScores.get(i);
                        String userId = score.getUserId();
                        int rank = i + 1;

                        userIdToUsername.putIfAbsent(userId, score.getUsername());
                        int[] stats = userRankStats.computeIfAbsent(userId, k -> new int[2]);
                        stats[0] += rank;
                        stats[1]++;
                    }
                }

                List<Score> aggregateLeaderboard = new ArrayList<>();
                for (Map.Entry<String, int[]> entry : userRankStats.entrySet()) {
                    String userId = entry.getKey();
                    int totalRank = entry.getValue()[0];
                    int gamesPlayed = entry.getValue()[1];

                    if (gamesPlayed > 0) {
                        int averageRank = totalRank / gamesPlayed;

                        // Find privacy from one of the user's scores (assuming consistency)
                        com.appsters.unlimitedgames.app.util.Privacy userPrivacy = com.appsters.unlimitedgames.app.util.Privacy.PUBLIC;
                        // Try to find a non-null privacy
                        for (Map.Entry<GameType, List<Score>> gameEntry : scoresByGame.entrySet()) {
                            List<Score> sList = gameEntry.getValue();
                            for (Score s : sList) {
                                if (s.getUserId().equals(userId) && s.getPrivacy() != null) {
                                    userPrivacy = s.getPrivacy();
                                    break;
                                }
                            }
                            // Optimization: if we found it effectively, break outer?
                            // But strict inner loop check is fine.
                        }

                        Score aggregateScore = new Score(
                                "",
                                userId,
                                userIdToUsername.get(userId),
                                GameType.ALL,
                                averageRank,
                                userPrivacy);
                        aggregateLeaderboard.add(aggregateScore);
                    }
                }

                // For aggregate rank (average rank), lower is better?
                // Original code sorted by score ASC? "Comparator.comparingInt(Score::getScore)"
                // But Average Rank: 1 is better than 10.
                // Score object usually holds "score". Here it holds "averageRank".
                // So sorting by ASC is correct for rank.
                aggregateLeaderboard.sort(Comparator.comparingInt(Score::getScore));

                List<Score> finalList = aggregateLeaderboard.stream().limit(limit).collect(Collectors.toList());

                listener.onComplete(true, finalList, null);

            } else {
                listener.onComplete(false, null, task.getException());
            }
        });
    }

    public void getFriendsLeaderboard(String userId, GameType gameType, OnCompleteListener<List<Score>> listener) {
        // Reuse getGlobalLeaderboard logic which handles friends filtering now?
        // Actually, getFriendsLeaderboard wants ONLY friends.
        // getGlobalLeaderboard shows Public + Friends.
        // So we can use getGlobalLeaderboard and then filter further?
        // Or keep separate implementation?
        // FriendsLeaderboard should show ALL friends, regardless of their privacy
        // (usually friends see friends data).
        // Let's rely on getGlobalLeaderboard('userId', ...) but filter specifically for
        // friends list.

        getGlobalLeaderboard(userId, gameType, 1000, (success, scores, err) -> {
            if (!success) {
                listener.onComplete(false, null, err);
                return;
            }

            friendRepository.getFriends(userId, friendsTask -> {
                if (!friendsTask.isSuccessful()) {
                    listener.onComplete(false, null, friendsTask.getException());
                    return;
                }

                List<String> friendIds = new ArrayList<>();
                for (Friend f : friendsTask.getResult()) {
                    friendIds.add(f.getFromUserId().equals(userId) ? f.getToUserId() : f.getFromUserId());
                }
                friendIds.add(userId);

                List<Score> friendScores = scores.stream()
                        .filter(s -> friendIds.contains(s.getUserId()))
                        .collect(Collectors.toList());

                // Re-rank
                for (int i = 0; i < friendScores.size(); i++) {
                    friendScores.get(i).setRank(i + 1);
                }

                listener.onComplete(true, friendScores, null);
            });
        });
    }

    public void submitScore(Score newScore, OnCompleteListener<Void> listener) {
        Query query = db.collection("scores")
                .whereEqualTo("userId", newScore.getUserId())
                .whereEqualTo("gameType", newScore.getGameType());

        query.limit(1).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot snapshot = task.getResult();
                if (snapshot != null && !snapshot.isEmpty()) {
                    DocumentSnapshot existingDoc = snapshot.getDocuments().get(0);
                    Score existingScore = existingDoc.toObject(Score.class);

                    if (existingScore != null && newScore.getScore() > existingScore.getScore()) {
                        existingDoc.getReference().set(newScore).addOnCompleteListener(updateTask -> {
                            if (!updateTask.isSuccessful()) {
                                savePendingScore(newScore);
                            }
                            listener.onComplete(updateTask.isSuccessful(), null, updateTask.getException());
                        });
                    } else {
                        listener.onComplete(true, null, null);
                    }
                } else {
                    db.collection("scores").add(newScore).addOnCompleteListener(createTask -> {
                        if (!createTask.isSuccessful()) {
                            savePendingScore(newScore);
                        }
                        listener.onComplete(createTask.isSuccessful(), null, createTask.getException());
                    });
                }
            } else {
                savePendingScore(newScore);
                listener.onComplete(false, null, task.getException());
            }
        });
    }

    private void savePendingScore(Score score) {
        android.content.SharedPreferences prefs = context.getSharedPreferences(PENDING_SCORES_PREF,
                android.content.Context.MODE_PRIVATE);
        String key = score.getUserId() + "_" + score.getGameType();
        // Check if we have a better score pending
        String existingJson = prefs.getString(key, null);
        if (existingJson != null) {
            Score existing = gson.fromJson(existingJson, Score.class);
            if (existing.getScore() >= score.getScore()) {
                return; // Already have a better or equal pending score
            }
        }

        prefs.edit().putString(key, gson.toJson(score)).apply();
    }

    public void retryPendingScores() {
        android.content.SharedPreferences prefs = context.getSharedPreferences(PENDING_SCORES_PREF,
                android.content.Context.MODE_PRIVATE);
        Map<String, ?> allPending = prefs.getAll();

        for (Map.Entry<String, ?> entry : allPending.entrySet()) {
            if (entry.getValue() instanceof String) {
                String json = (String) entry.getValue();
                Score score = gson.fromJson(json, Score.class);

                // Try submitting again. If successful, remove from prefs.
                // We pass a listener that removes it on success.
                submitScore(score, (isSuccess, result, e) -> {
                    if (isSuccess) {
                        prefs.edit().remove(entry.getKey()).apply();
                    }
                });
            }
        }
    }

    public interface OnCompleteListener<T> {
        void onComplete(boolean isSuccessful, T result, Exception e);
    }
}
