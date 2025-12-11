package com.appsters.unlimitedgames.app.data.model;

import com.appsters.unlimitedgames.app.util.GameType;

public class Score {
    private String scoreId;
    private String userId;
    private String username;
    private GameType gameType;
    private int score;
    private long timestamp;
    private int rank;
    private com.appsters.unlimitedgames.app.util.Privacy privacy; // Denormalized privacy

    public Score() {
        this.timestamp = System.currentTimeMillis();
        this.privacy = com.appsters.unlimitedgames.app.util.Privacy.PUBLIC; // Default
    }

    public Score(String scoreId, String userId, String username, GameType gameType, int score,
            com.appsters.unlimitedgames.app.util.Privacy privacy) {
        this.scoreId = scoreId;
        this.userId = userId;
        this.username = username;
        this.gameType = gameType;
        this.score = score;
        this.timestamp = System.currentTimeMillis();
        this.privacy = privacy;
    }

    // Keep existing constructor for backward compatibility if needed, or update
    // call sites.
    // Ideally we update call sites, but overloading is safer for now.
    public Score(String scoreId, String userId, String username, GameType gameType, int score) {
        this(scoreId, userId, username, gameType, score, com.appsters.unlimitedgames.app.util.Privacy.PUBLIC);
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

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public com.appsters.unlimitedgames.app.util.Privacy getPrivacy() {
        return privacy;
    }

    public void setPrivacy(com.appsters.unlimitedgames.app.util.Privacy privacy) {
        this.privacy = privacy;
    }
}
