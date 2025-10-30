package com.appsters.unlimitedgames.data.model;

import com.appsters.unlimitedgames.util.GameType;

import java.util.List;

public class Leaderboard {
    private GameType gameType;
    private List<Score> scores;

    public Leaderboard() {}

    public Leaderboard(GameType gameType, List<Score> scores) {
        this.gameType = gameType;
        this.scores = scores;
    }

    public GameType getGameType() {
        return gameType;
    }

    public void setGameType(GameType gameType) {
        this.gameType = gameType;
    }

    public List<Score> getScores() {
        return scores;
    }

    public void setScores(List<Score> scores) {
        this.scores = scores;
    }
}
