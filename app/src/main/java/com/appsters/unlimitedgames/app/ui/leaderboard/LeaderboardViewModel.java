package com.appsters.unlimitedgames.app.ui.leaderboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.appsters.unlimitedgames.app.data.model.Score;
import com.appsters.unlimitedgames.app.data.repository.LeaderboardRepository;
import com.appsters.unlimitedgames.app.util.GameType;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class LeaderboardViewModel extends ViewModel {

    private final LeaderboardRepository leaderboardRepository;
    private final MutableLiveData<List<Score>> leaderboard = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private GameType selectedGame = null;
    private boolean showFriendsOnly = false;

    public LeaderboardViewModel() {
        leaderboardRepository = new LeaderboardRepository();
    }

    public LiveData<List<Score>> getLeaderboard() {
        return leaderboard;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadLeaderboard() {
        isLoading.setValue(true);
        String userId = FirebaseAuth.getInstance().getUid();

        GameType gameType = selectedGame == null ? GameType.ALL : selectedGame;

        if (showFriendsOnly) {
            leaderboardRepository.getFriendsLeaderboard(userId, gameType, (isSuccessful, result, e) -> {
                isLoading.setValue(false);
                if (isSuccessful) {
                    leaderboard.setValue(result);
                } else {
                    errorMessage.setValue(e.getMessage());
                }
            });
        } else {
            leaderboardRepository.getGlobalLeaderboard(gameType, 100, (isSuccessful, result, e) -> {
                isLoading.setValue(false);
                if (isSuccessful) {
                    leaderboard.setValue(result);
                } else {
                    errorMessage.setValue(e.getMessage());
                }
            });
        }
    }

    public void setGameType(GameType gameType) {
        selectedGame = gameType;
        loadLeaderboard();
    }

    public void setShowFriendsOnly(boolean friendsOnly) {
        showFriendsOnly = friendsOnly;
        loadLeaderboard();
    }
}
