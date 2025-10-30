package com.appsters.unlimitedgames.app.data.model;

import com.appsters.unlimitedgames.app.util.GameType;

public class Game {
    private String gameId;
    private GameType gameType;
    private String title;
    private String description;

    public Game() {}

    public Game(String gameId, GameType gameType, String title, String description) {
        this.gameId = gameId;
        this.gameType = gameType;
        this.title = title;
        this.description = description;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public GameType getGameType() {
        return gameType;
    }

    public void setGameType(GameType gameType) {
        this.gameType = gameType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
