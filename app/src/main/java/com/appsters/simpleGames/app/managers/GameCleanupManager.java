package com.appsters.simpleGames.app.managers;

import com.appsters.simpleGames.games.interfaces.IGame;
import java.util.List;

public class GameCleanupManager {
    private final List<IGame> games;

    public GameCleanupManager(List<IGame> games) {
        this.games = games;
    }

    public void clearAllGameData() {
        if (games != null) {
            for (IGame game : games) {
                game.clearUserData();
            }
        }
    }
}
