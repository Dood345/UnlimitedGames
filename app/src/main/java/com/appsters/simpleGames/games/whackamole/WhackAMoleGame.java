package com.appsters.simpleGames.games.whackamole;

import android.content.Context;
import com.appsters.simpleGames.games.interfaces.IGame;
import com.appsters.simpleGames.games.whackamole.repository.SharedPrefGameRepository;

public class WhackAMoleGame implements IGame {
    private final Context context;

    public WhackAMoleGame(Context context) {
        this.context = context;
    }

    @Override
    public void clearUserData() {
        android.content.SharedPreferences prefs = context.getSharedPreferences("WhackAMolePrefs", Context.MODE_PRIVATE);
        SharedPrefGameRepository repository = new SharedPrefGameRepository(prefs);
        repository.clearData();
    }
}
