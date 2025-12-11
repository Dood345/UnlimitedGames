package com.appsters.unlimitedgames.games.soccerseparationgame;

import android.content.Context;

import com.appsters.unlimitedgames.games.interfaces.IGame;
import com.appsters.unlimitedgames.games.soccerseparationgame.repository.SoccerSeparationGameRepository;

public class SoccerSeparationGame implements IGame {
    private final Context context;

    public SoccerSeparationGame(Context context) {
        this.context = context;
    }

    @Override
    public void clearUserData() {
        SoccerSeparationGameRepository repository = new SoccerSeparationGameRepository(context);
        repository.clearUserData();
    }
}
