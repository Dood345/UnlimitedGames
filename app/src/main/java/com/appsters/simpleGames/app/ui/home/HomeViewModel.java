package com.appsters.simpleGames.app.ui.home;

import androidx.lifecycle.ViewModel;
import com.appsters.simpleGames.app.data.GameDataSource;
import com.appsters.simpleGames.app.data.model.Game;

public class HomeViewModel extends ViewModel {
    private final androidx.lifecycle.MutableLiveData<java.util.List<Game>> mGames;

    public HomeViewModel() {
        mGames = new androidx.lifecycle.MutableLiveData<>();
        loadGames();
    }

    public androidx.lifecycle.LiveData<java.util.List<Game>> getGames() {
        return mGames;
    }

    private void loadGames() {
        // In a real app, this might be an async call
        mGames.setValue(GameDataSource.getGames());
    }
}
