package com.appsters.unlimitedgames.app.data.repository;

import com.appsters.unlimitedgames.app.data.model.Friend;
import com.appsters.unlimitedgames.app.data.model.Score;
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

    public LeaderboardRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.friendRepository = new FriendRepository();
    }

    public void getGlobalLeaderboard(GameType gameType, int limit, OnCompleteListener<List<Score>> listener) {
        if (gameType == GameType.ALL) {
            getAggregateLeaderboard(limit, listener);
        } else {
            getSingleGameLeaderboard(gameType, limit, listener);
        }
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

                scoresByGame.forEach((game, scores) -> scores.sort(Comparator.comparingInt(Score::getScore).reversed()));

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
                        Score aggregateScore = new Score(
                                "",
                                userId,
                                userIdToUsername.get(userId),
                                GameType.ALL,
                                averageRank
                        );
                        aggregateLeaderboard.add(aggregateScore);
                    }
                }

                aggregateLeaderboard.sort(Comparator.comparingInt(Score::getScore));

                List<Score> finalList = aggregateLeaderboard.stream().limit(limit).collect(Collectors.toList());

                listener.onComplete(true, finalList, null);

            } else {
                listener.onComplete(false, null, task.getException());
            }
        });
    }


    public void getFriendsLeaderboard(String userId, GameType gameType, OnCompleteListener<List<Score>> listener) {
        friendRepository.getFriends(userId, friendsTask -> {
            if (!friendsTask.isSuccessful() || friendsTask.getResult() == null) {
                listener.onComplete(false, null, friendsTask.getException());
                return;
            }

            List<Friend> friends = friendsTask.getResult();
            List<String> friendIds = friends.stream()
                    .map(friend -> friend.getFromUserId().equals(userId) ? friend.getToUserId() : friend.getFromUserId())
                    .collect(Collectors.toList());
            friendIds.add(userId);

            getGlobalLeaderboard(gameType, 1000, (isSuccessful, allScores, e) -> {
                if (isSuccessful) {
                    List<Score> friendScores = allScores.stream()
                            .filter(score -> friendIds.contains(score.getUserId()))
                            .collect(Collectors.toList());
                    listener.onComplete(true, friendScores, null);
                } else {
                    listener.onComplete(false, null, e);
                }
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
                            listener.onComplete(updateTask.isSuccessful(), null, updateTask.getException());
                        });
                    } else {
                        listener.onComplete(true, null, null);
                    }
                } else {
                    db.collection("scores").add(newScore).addOnCompleteListener(createTask -> {
                        listener.onComplete(createTask.isSuccessful(), null, createTask.getException());
                    });
                }
            } else {
                listener.onComplete(false, null, task.getException());
            }
        });
    }

    public interface OnCompleteListener<T> {
        void onComplete(boolean isSuccessful, T result, Exception e);
    }
}
