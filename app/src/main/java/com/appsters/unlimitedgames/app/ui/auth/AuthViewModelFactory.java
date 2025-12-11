package com.appsters.unlimitedgames.app.ui.auth;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.appsters.unlimitedgames.app.managers.GameCleanupManager;
import com.appsters.unlimitedgames.games.interfaces.IGame;
import com.appsters.unlimitedgames.games.game2048.Game2048Game;
import com.appsters.unlimitedgames.games.whackamole.WhackAMoleGame;
import com.appsters.unlimitedgames.games.sudoku.SudokuGame;
import com.appsters.unlimitedgames.games.maze.MazeGame;

import java.util.ArrayList;
import java.util.List;

public class AuthViewModelFactory implements ViewModelProvider.Factory {
    private final Application application;

    public AuthViewModelFactory(Application application) {
        this.application = application;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(AuthViewModel.class)) {
            List<IGame> games = new ArrayList<>();
            // Initialize game adapters (passing Application context)
            games.add(new Game2048Game(application));
            games.add(new WhackAMoleGame(application));
            games.add(new SudokuGame(application));
            games.add(new MazeGame(application));
            games.add(new com.appsters.unlimitedgames.games.soccerseparationgame.SoccerSeparationGame(application));

            GameCleanupManager manager = new GameCleanupManager(games);
            return (T) new AuthViewModel(manager);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
