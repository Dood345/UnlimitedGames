package com.appsters.unlimitedgames.app.data.repository;

import com.appsters.unlimitedgames.app.data.model.Score;
import com.appsters.unlimitedgames.app.util.GameType;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardRepository {
    private FirebaseFirestore db;

    public LeaderboardRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    public void getGlobalLeaderboard(GameType gameType, int limit, OnCompleteListener<List<Score>> listener) {
        Query query = db.collection("scores").orderBy("score", Query.Direction.DESCENDING).limit(limit);

        if (gameType != GameType.ALL) {
            query = query.whereEqualTo("gameType", gameType.name());
        }

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Score> scores = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    scores.add(document.toObject(Score.class));
                }
                listener.onComplete(task.isSuccessful(), scores, task.getException());
            } else {
                listener.onComplete(task.isSuccessful(), null, task.getException());
            }
        });
    }

    public void getFriendsLeaderboard(String userId, GameType gameType, OnCompleteListener<List<Score>> listener) {
        db.collection("users").document(userId).collection("friends").get().addOnCompleteListener(friendsTask -> {
            if (friendsTask.isSuccessful()) {
                List<String> friendIds = new ArrayList<>();
                for (QueryDocumentSnapshot document : friendsTask.getResult()) {
                    friendIds.add(document.getId());
                }
                friendIds.add(userId);

                if (friendIds.isEmpty()) {
                    listener.onComplete(true, new ArrayList<>(), null);
                    return;
                }

                Query query = db.collection("scores").whereIn("userId", friendIds).orderBy("score", Query.Direction.DESCENDING);

                if (gameType != GameType.ALL) {
                    query = query.whereEqualTo("gameType", gameType.name());
                }

                query.get().addOnCompleteListener(scoresTask -> {
                    if (scoresTask.isSuccessful()) {
                        List<Score> scores = new ArrayList<>();
                        for (QueryDocumentSnapshot document : scoresTask.getResult()) {
                            scores.add(document.toObject(Score.class));
                        }
                        listener.onComplete(scoresTask.isSuccessful(), scores, scoresTask.getException());
                    } else {
                        listener.onComplete(scoresTask.isSuccessful(), null, scoresTask.getException());
                    }
                });
            } else {
                listener.onComplete(friendsTask.isSuccessful(), null, friendsTask.getException());
            }
        });
    }

    public void submitScore(Score score, OnCompleteListener<Void> listener) {
        db.collection("scores").add(score).addOnCompleteListener(task -> {
            listener.onComplete(task.isSuccessful(), null, task.getException());
        });
    }

    public interface OnCompleteListener<T> {
        void onComplete(boolean isSuccessful, T result, Exception e);
    }
}
