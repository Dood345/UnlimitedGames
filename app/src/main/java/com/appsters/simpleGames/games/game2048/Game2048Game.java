package com.appsters.simpleGames.games.game2048;

import android.content.Context;
import com.appsters.simpleGames.games.interfaces.IGame;
import com.appsters.simpleGames.games.game2048.repository.Cam2048Repository;

public class Game2048Game implements IGame {
    private final Context context;

    public Game2048Game(Context context) {
        this.context = context;
    }

    @Override
    public void clearUserData() {
        // Instantiate repository and clear state
        // Note: Repository needs Application context usually, but if it takes Context
        // it works.
        // Assuming Cam2048Repository takes Context or Application.
        Cam2048Repository repository = new Cam2048Repository(context);
        repository.clearSavedState();
        repository.clearHighScore();
    }

    @Override
    public void syncHighScore(Context context,
            java.util.List<com.appsters.simpleGames.app.data.model.Score> remoteScores) {
        try {
            Cam2048Repository activeRepo2048 = new Cam2048Repository(context);

            int local2048 = activeRepo2048.getHighScore();
            for (com.appsters.simpleGames.app.data.model.Score s : remoteScores) {
                if (s.getGameType() == com.appsters.simpleGames.app.util.GameType.GAME2048) {
                    if (s.getScore() > local2048) {
                        android.util.Log.d("Sync",
                                "Updating 2048 local score from " + local2048 + " to " + s.getScore());
                        activeRepo2048.saveHighScore(s.getScore());
                        local2048 = s.getScore();
                    }
                }
            }
        } catch (Exception e) {
            android.util.Log.e("Sync", "Error syncing 2048", e);
        }
    }
}
