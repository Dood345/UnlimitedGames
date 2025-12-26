package com.appsters.simpleGames.app.ui.auth;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.appsters.simpleGames.app.managers.GameCleanupManager;
import com.appsters.simpleGames.games.interfaces.IGame;
import com.appsters.simpleGames.games.game2048.Game2048Game;
import com.appsters.simpleGames.games.whackamole.WhackAMoleGame;
import com.appsters.simpleGames.games.sudoku.SudokuGame;
import com.appsters.simpleGames.games.maze.MazeGame;

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
            List<IGame> games = com.appsters.simpleGames.app.managers.GameRegistry.getRegisteredGames(application);

            GameCleanupManager manager = new GameCleanupManager(games);
            return (T) new AuthViewModel(manager);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
