package com.appsters.simpleGames.games.game2048;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.appsters.simpleGames.app.data.model.Score;
import com.appsters.simpleGames.app.data.repository.LeaderboardRepository;
import com.appsters.simpleGames.app.data.repository.UserRepository;
import com.appsters.simpleGames.app.util.GameType;
import com.appsters.simpleGames.games.game2048.repository.Cam2048Repository;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Game2048ViewModel extends AndroidViewModel {

    private static final int SIZE = 4;
    private final MutableLiveData<int[][]> _board = new MutableLiveData<>();
    public final LiveData<int[][]> board = _board;
    private final MutableLiveData<Integer> _score = new MutableLiveData<>(0);
    public final LiveData<Integer> score = _score;
    private final MutableLiveData<Boolean> _gameOver = new MutableLiveData<>(false);
    public final LiveData<Boolean> gameOver = _gameOver;
    private final MutableLiveData<Integer> _highScore = new MutableLiveData<>(0);
    public final LiveData<Integer> highScore = _highScore;

    private int[][] boardArray = new int[SIZE][SIZE];
    private final Random random = new Random();
    private int moveScore = 0;
    private final Cam2048Repository repository;
    private final LeaderboardRepository leaderboardRepository;
    private final UserRepository userRepository;

    public Game2048ViewModel(Application application) {
        super(application);
        repository = new Cam2048Repository(application);
        leaderboardRepository = new LeaderboardRepository(application);
        userRepository = new UserRepository();
        _highScore.setValue(repository.getHighScore());
        loadGame();
    }

    private void loadGame() {
        int[][] savedBoard = repository.getSavedBoard();
        int savedScore = repository.getSavedScore();

        if (savedBoard != null && savedScore != -1) {
            boardArray = savedBoard;
            _score.setValue(savedScore);
            _board.setValue(boardArray);
        } else {
            newGame();
        }
    }

    public void saveGameState() {
        Integer currentScore = _score.getValue();
        if (currentScore != null) {
            // Save high score if needed
            if (currentScore > _highScore.getValue()) {
                _highScore.postValue(currentScore);
                repository.saveHighScore(currentScore);
                submitScoreToLeaderboard(currentScore);
            }

            // Save game state if not over
            if (!Boolean.TRUE.equals(_gameOver.getValue())) {
                repository.saveGameState(boardArray, currentScore);
            }
        }
    }

    public void newGame() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                boardArray[i][j] = 0;
            }
        }
        _score.setValue(0);
        _gameOver.setValue(false);
        repository.clearSavedState(); // Clear any saved state
        addRandomTile();
        addRandomTile();
        _board.setValue(boardArray);
    }

    public void move(int direction) { // 0:left, 1:up, 2:right, 3:down
        boolean moved = false;
        moveScore = 0; // Reset score for the current move

        int rotations = getRotationsForDirection(direction);
        rotate(rotations);

        for (int i = 0; i < SIZE; i++) {
            int[] row = boardArray[i];
            int[] newRow = processRow(row);
            boardArray[i] = newRow;
            if (!moved && !Arrays.equals(row, newRow)) {
                moved = true;
            }
        }

        rotate((4 - rotations) % 4); // Rotate back

        if (moved) {
            Integer currentScore = _score.getValue();
            if (currentScore == null)
                currentScore = 0;
            _score.setValue(currentScore + moveScore);
            addRandomTile();
            checkGameOver();
        }
        _board.setValue(boardArray);
    }

    private int[] processRow(int[] row) {
        List<Integer> nonZero = new ArrayList<>();
        for (int val : row) {
            if (val != 0) {
                nonZero.add(val);
            }
        }

        List<Integer> merged = new ArrayList<>();
        for (int i = 0; i < nonZero.size(); i++) {
            if (i < nonZero.size() - 1 && nonZero.get(i).equals(nonZero.get(i + 1))) {
                int mergedValue = nonZero.get(i) * 2;
                merged.add(mergedValue);
                moveScore += mergedValue; // Accumulate score for the move
                i++; // Skip the next element
            } else {
                merged.add(nonZero.get(i));
            }
        }

        int[] newRow = new int[SIZE];
        for (int i = 0; i < merged.size(); i++) {
            newRow[i] = merged.get(i);
        }
        return newRow;
    }

    private void rotate(int count) {
        for (int i = 0; i < count; i++) {
            int[][] rotated = new int[SIZE][SIZE];
            for (int r = 0; r < SIZE; r++) {
                for (int c = 0; c < SIZE; c++) {
                    rotated[c][SIZE - 1 - r] = boardArray[r][c];
                }
            }
            boardArray = rotated;
        }
    }

    private int getRotationsForDirection(int direction) {
        switch (direction) {
            case 0:
                return 0; // Left
            case 1:
                return 3; // Up
            case 2:
                return 2; // Right
            case 3:
                return 1; // Down
            default:
                return 0;
        }
    }

    private void addRandomTile() {
        List<int[]> emptyTiles = new ArrayList<>();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (boardArray[i][j] == 0) {
                    emptyTiles.add(new int[] { i, j });
                }
            }
        }
        if (!emptyTiles.isEmpty()) {
            int[] pos = emptyTiles.get(random.nextInt(emptyTiles.size()));
            boardArray[pos[0]][pos[1]] = random.nextDouble() < 0.9 ? 2 : 4;
        }
    }

    private void checkGameOver() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (boardArray[i][j] == 0 ||
                        (i < SIZE - 1 && boardArray[i][j] == boardArray[i + 1][j]) ||
                        (j < SIZE - 1 && boardArray[i][j] == boardArray[i][j + 1])) {
                    _gameOver.postValue(false);
                    return;
                }
            }
        }
        Integer currentScore = _score.getValue();
        if (currentScore != null) {
            repository.saveHighScore(currentScore);
            if (currentScore > _highScore.getValue()) {
                _highScore.postValue(currentScore);
            }
            submitScoreToLeaderboard(currentScore);
        }
        _gameOver.postValue(true);
        repository.clearSavedState(); // Clear saved state on game over
    }

    private void submitScoreToLeaderboard(int score) {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null)
            return;

        userRepository.getUser(userId, task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                String username = task.getResult().getUsername();
                Score scoreObject = new Score(null, userId, username, GameType.GAME2048, score);
                leaderboardRepository.submitScore(scoreObject, (isSuccessful, result, e) -> {
                    // Optionally handle success or failure
                });
            }
        });
    }
}
