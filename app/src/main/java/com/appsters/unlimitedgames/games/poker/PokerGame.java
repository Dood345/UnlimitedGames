package com.appsters.unlimitedgames.games.poker;

import android.content.Context;
import com.appsters.unlimitedgames.games.interfaces.IGame;
import com.appsters.unlimitedgames.games.poker.repo.RandPokerRepository;

public class PokerGame implements IGame {
    private final Context context;

    public PokerGame(Context context) {
        this.context = context;
    }

    @Override
    public void clearUserData() {
        // Instantiate repository and clear state
        // Note: Repository needs Application context usually, but if it takes Context
        // it works.
        // Assuming RandPokerRepository takes Context or Application.
        RandPokerRepository repository = new RandPokerRepository(context);
        repository.clearAllState();
    }
}