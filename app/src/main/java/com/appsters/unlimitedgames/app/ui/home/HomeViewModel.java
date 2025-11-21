package com.appsters.unlimitedgames.app.ui.home;

import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {
    private final androidx.lifecycle.MutableLiveData<java.util.List<com.appsters.unlimitedgames.app.data.model.Game>> mGames;

    public HomeViewModel() {
        mGames = new androidx.lifecycle.MutableLiveData<>();
        loadGames();
    }

    public androidx.lifecycle.LiveData<java.util.List<com.appsters.unlimitedgames.app.data.model.Game>> getGames() {
        return mGames;
    }

    private void loadGames() {
        // In a real app, this might be an async call
        mGames.setValue(com.appsters.unlimitedgames.app.data.GameDataSource.getGames());
    }
}
