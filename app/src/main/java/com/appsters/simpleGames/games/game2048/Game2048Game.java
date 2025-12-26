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
}
