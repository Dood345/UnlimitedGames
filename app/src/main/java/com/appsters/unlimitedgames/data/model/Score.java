package com.appsters.unlimitedgames.data.model;

import com.appsters.unlimitedgames.util.GameType;

public class Score {
    private String scoreId;
    private String userId;
    private String username;
    private GameType gameType;
    private int score;
    private long timestamp;

    public Score() {
        this.timestamp = System.currentTimeMillis();
    }

    public Score(String scoreId, String userId, String username, GameType gameType, int score) {
        this.scoreId = scoreId;
        this.userId = userId;
        this.username = username;
        this.gameType = gameType;
        this.score = score;
        this.timestamp = System.currentTimeMillis();
    }

    public String getScoreId() {
        return scoreId;
    }

    public void setScoreId(String scoreId) {
        this.scoreId = scoreId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public GameType getGameType() {
        return gameType;
    }

    public void setGameType(GameType gameType) {
        this.gameType = gameType;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
