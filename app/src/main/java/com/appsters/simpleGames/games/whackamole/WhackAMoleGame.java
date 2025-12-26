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

    @Override
    public void syncHighScore(Context context,
            java.util.List<com.appsters.simpleGames.app.data.model.Score> remoteScores) {
        try {
            android.content.SharedPreferences prefs = context.getSharedPreferences("WhackAMolePrefs",
                    Context.MODE_PRIVATE);
            SharedPrefGameRepository whamRepo = new SharedPrefGameRepository(prefs);
            Integer currentObj = whamRepo.getHighScore().getValue();
            int localWham = (currentObj != null) ? currentObj : 0;

            for (com.appsters.simpleGames.app.data.model.Score s : remoteScores) {
                if (s.getGameType() == com.appsters.simpleGames.app.util.GameType.WHACK_A_MOLE) {
                    if (s.getScore() > localWham) {
                        android.util.Log.d("Sync",
                                "Updating Whack-A-Mole local score from " + localWham + " to " + s.getScore());
                        whamRepo.saveHighScore(s.getScore());
                        localWham = s.getScore();
                    }
                }
            }
        } catch (Exception e) {
            android.util.Log.e("Sync", "Error syncing Whack-a-Mole", e);
        }
    }
}
