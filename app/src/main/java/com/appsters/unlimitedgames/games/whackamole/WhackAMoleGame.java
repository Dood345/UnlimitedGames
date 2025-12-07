package com.appsters.unlimitedgames.games.whackamole;

import android.content.Context;
import com.appsters.unlimitedgames.games.interfaces.IGame;
import com.appsters.unlimitedgames.games.whackamole.repository.SharedPrefGameRepository;

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
