package com.appsters.unlimitedgames.games.soccerseparationgame.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.appsters.unlimitedgames.games.soccerseparationgame.model.SeparationQuestion;
import com.appsters.unlimitedgames.games.soccerseparationgame.repository.SoccerSeparationGameRepository;

import java.util.ArrayList;
import java.util.List;

public class SoccerSeparationGameViewModel extends ViewModel {

    private final SoccerSeparationGameRepository repository;

    private final MutableLiveData<List<SeparationQuestion>> questions = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Integer> index = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> score = new MutableLiveData<>(0);

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(true);
    private final MutableLiveData<Boolean> gameOver = new MutableLiveData<>(false);

    private final MutableLiveData<Boolean> lastAnswerCorrect = new MutableLiveData<>(null);

    public SoccerSeparationGameViewModel() {
        this(new SoccerSeparationGameRepository());
    }

    public SoccerSeparationGameViewModel(SoccerSeparationGameRepository repository) {
        this.repository = repository;
    }

    // Getters
    public LiveData<List<SeparationQuestion>> getQuestions() { return questions; }
    public LiveData<Integer> getIndex() { return index; }
    public LiveData<Integer> getScore() { return score; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<Boolean> getGameOver() { return gameOver; }
    public LiveData<Boolean> getLastAnswerCorrect() { return lastAnswerCorrect; }

    /**
     * Loads questions asynchronously from the repository.
     */
    public void loadGame() {
        loading.setValue(true);
        gameOver.setValue(false);
        lastAnswerCorrect.setValue(null);

        repository.loadQuestions((list, err) -> {
            if (err != null) {
                loading.postValue(false);
                gameOver.postValue(true);
                return;
            }

            questions.postValue(list);
            score.postValue(0);
            index.postValue(0);
            gameOver.postValue(false);
            loading.postValue(false);
        });
    }

    /**
     * Returns current question safely.
     */
    public SeparationQuestion getCurrentQuestion() {
        List<SeparationQuestion> qList = questions.getValue();
        Integer i = index.getValue();

        if (qList == null || qList.isEmpty()) return null;
        if (i == null || i >= qList.size()) return null;

        return qList.get(i);
    }

    /**
     * User answers a question.
     */
    public void answer(String pickedId) {
        if (Boolean.TRUE.equals(gameOver.getValue())) return;
        if (Boolean.TRUE.equals(loading.getValue())) return;

        SeparationQuestion q = getCurrentQuestion();
        if (q == null) return;

        // Check if answer is correct and update score
        boolean isCorrect = pickedId.equals(q.correct1.id);
        lastAnswerCorrect.setValue(isCorrect);

        if (isCorrect) {
            Integer currentScore = score.getValue();
            score.setValue(currentScore != null ? currentScore + 1 : 1);
        }

        // DON'T move to next question yet - let the Fragment handle this after showing feedback
    }

    /**
     * Moves to the next question after feedback is shown
     */
    public void moveToNextQuestion() {
        Integer currentIndex = index.getValue();
        int nextIndex = (currentIndex != null ? currentIndex : 0) + 1;
        index.setValue(nextIndex);

        // Check if game is over
        List<SeparationQuestion> qList = questions.getValue();
        if (qList != null && nextIndex >= qList.size()) {
            gameOver.setValue(true);
        }
    }

    /**
     * Resets game and loads a new set of questions from repository.
     */
    public void resetGame() {
        // Always load fresh questions from repository
        loadGame();
    }

    /**
     * Clear the last answer feedback (call after showing feedback briefly)
     */
    public void clearLastAnswerFeedback() {
        lastAnswerCorrect.setValue(null);
    }
}