package com.appsters.simpleGames.games.soccerseparationgame;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.appsters.simpleGames.R;
import com.appsters.simpleGames.games.soccerseparationgame.model.SeparationQuestion;
import com.appsters.simpleGames.games.soccerseparationgame.viewmodel.SoccerSeparationGameViewModel;

import java.util.List;

public class SoccerSeparationGameFragment extends Fragment {

    private SoccerSeparationGameViewModel vm;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable feedbackRunnable;

    private TextView tvPlayer1, tvPlayer2, tvResult, tvProgress;
    private Button[] choiceButtons = new Button[4];
    private Button btnRestart;
    private ProgressBar loadingIndicator;

    private boolean isShowingFeedback = false;
    private boolean isAnswered = false; // Prevent multiple answers
    private String selectedAnswerId = null;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_soccer_separation_game, container, false);

        tvProgress = v.findViewById(R.id.tv_question_progress);
        tvPlayer1 = v.findViewById(R.id.tv_player1);
        tvPlayer2 = v.findViewById(R.id.tv_player2);
        tvResult = v.findViewById(R.id.tv_result);

        loadingIndicator = v.findViewById(R.id.loading_indicator);
        btnRestart = v.findViewById(R.id.btn_restart);

        choiceButtons[0] = v.findViewById(R.id.btn_choice_1);
        choiceButtons[1] = v.findViewById(R.id.btn_choice_2);
        choiceButtons[2] = v.findViewById(R.id.btn_choice_3);
        choiceButtons[3] = v.findViewById(R.id.btn_choice_4);

        vm = new ViewModelProvider(this).get(SoccerSeparationGameViewModel.class);

        observeVM();
        vm.loadGame();

        return v;
    }

    private void observeVM() {
        vm.getLoading().observe(getViewLifecycleOwner(), loading -> updateUI());
        vm.getGameOver().observe(getViewLifecycleOwner(), gameOver -> {
            if (Boolean.TRUE.equals(gameOver) && feedbackRunnable != null) {
                handler.removeCallbacks(feedbackRunnable);
                feedbackRunnable = null;
            }
            updateUI();
        });
        vm.getIndex().observe(getViewLifecycleOwner(), index -> updateUI());
        vm.getQuestions().observe(getViewLifecycleOwner(), questions -> updateUI());
        vm.getScore().observe(getViewLifecycleOwner(), score -> updateUI());

        vm.getLastAnswerCorrect().observe(getViewLifecycleOwner(), isCorrect -> {
            if (isCorrect != null && !Boolean.TRUE.equals(vm.getGameOver().getValue())) {
                showAnswerFeedback(isCorrect);
            }
        });

        btnRestart.setOnClickListener(v -> vm.resetGame());
    }

    private void updateUI() {
        if (isShowingFeedback)
            return;

        Boolean isLoading = vm.getLoading().getValue();
        Boolean isGameOver = vm.getGameOver().getValue();
        List<SeparationQuestion> questionsList = vm.getQuestions().getValue();
        Integer currentIndex = vm.getIndex().getValue();

        if (Boolean.TRUE.equals(isLoading)) {
            showLoadingUI();
            return;
        }

        if (Boolean.TRUE.equals(isGameOver)) {
            showGameOverUI();
            return;
        }

        if (questionsList != null && !questionsList.isEmpty() && currentIndex != null
                && currentIndex < questionsList.size()) {
            showPlayingUI();
            return;
        }

        showLoadingUI();
    }

    private void showLoadingUI() {
        loadingIndicator.setVisibility(View.VISIBLE);

        tvProgress.setVisibility(View.GONE);
        tvPlayer1.setVisibility(View.GONE);
        tvPlayer2.setVisibility(View.GONE);
        tvResult.setVisibility(View.GONE);
        btnRestart.setVisibility(View.GONE);

        for (Button b : choiceButtons) {
            b.setVisibility(View.GONE);
            b.setEnabled(true);
        }
    }

    private void showPlayingUI() {
        loadingIndicator.setVisibility(View.GONE);

        tvProgress.setVisibility(View.VISIBLE);
        tvPlayer1.setVisibility(View.VISIBLE);
        tvPlayer2.setVisibility(View.VISIBLE);
        tvResult.setVisibility(View.GONE);
        btnRestart.setVisibility(View.GONE);

        isAnswered = false; // Reset for new question

        SeparationQuestion q = vm.getCurrentQuestion();
        List<SeparationQuestion> list = vm.getQuestions().getValue();
        Integer idx = vm.getIndex().getValue();

        if (list == null || list.isEmpty() || idx == null || q == null) {
            return;
        }

        tvProgress.setText("Question " + (idx + 1) + " of " + list.size());
        tvPlayer1.setText(q.player0.name);
        tvPlayer2.setText(q.player2.name);

        for (int i = 0; i < choiceButtons.length; i++) {
            SeparationQuestion.Player p = q.choices1.get(i);
            Button b = choiceButtons[i];

            b.setVisibility(View.VISIBLE);
            b.setEnabled(true);
            b.setText(p.name);
            b.setTextSize(18);
            b.setTextColor(requireContext().getColor(R.color.separation_game_text));
            b.setBackgroundTintList(requireContext().getColorStateList(R.color.separation_game_button));
            b.setOnClickListener(v -> {
                if (isAnswered)
                    return; // Prevent multiple clicks
                isAnswered = true;
                selectedAnswerId = p.id;
                vm.answer(p.id);
            });
        }
    }

    private void showGameOverUI() {
        loadingIndicator.setVisibility(View.GONE);

        tvProgress.setVisibility(View.GONE);
        tvPlayer1.setVisibility(View.GONE);
        tvPlayer2.setVisibility(View.GONE);

        for (Button b : choiceButtons) {
            b.setVisibility(View.GONE);
        }

        Integer finalScore = vm.getScore().getValue();
        List<SeparationQuestion> list = vm.getQuestions().getValue();
        int totalQuestions = (list != null) ? list.size() : 10;
        int score = (finalScore != null ? finalScore : 0);

        tvResult.setVisibility(View.VISIBLE);

        // Create a more appealing multi-line game over message
        String gameOverMessage = "🎉 Game Over! 🎉\n\n" +
                "Your Score\n" +
                score + " / " + totalQuestions + "\n\n";

        // Add encouraging message based on performance
        double percentage = (double) score / totalQuestions * 100;
        if (percentage == 100) {
            gameOverMessage += "Perfect! Outstanding! ⭐";
        } else if (percentage >= 80) {
            gameOverMessage += "Excellent work! 🏆";
        } else if (percentage >= 60) {
            gameOverMessage += "Good job! 👏";
        } else if (percentage >= 40) {
            gameOverMessage += "Keep practicing! 💪";
        } else {
            gameOverMessage += "Try again! 🔄";
        }

        tvResult.setText(gameOverMessage);
        tvResult.setTextSize(24);
        tvResult.setTextColor(requireContext().getColor(R.color.separation_game_text));
        tvResult.setGravity(android.view.Gravity.CENTER);
        tvResult.setPadding(32, 32, 32, 32);

        btnRestart.setVisibility(View.VISIBLE);

        vm.submitScore();
    }

    private void showAnswerFeedback(boolean isCorrect) {
        isShowingFeedback = true;
        SeparationQuestion q = vm.getCurrentQuestion();
        if (q == null)
            return;

        // Highlight the buttons to show correct/incorrect
        for (int i = 0; i < choiceButtons.length; i++) {
            Button b = choiceButtons[i];
            SeparationQuestion.Player p = q.choices1.get(i);

            if (p.id.equals(q.correct1.id)) {
                // This is the correct answer - always show in green
                b.setBackgroundTintList(requireContext().getColorStateList(R.color.separation_game_correct));
                b.setTextSize(22);
                b.setTextColor(requireContext().getColor(R.color.separation_game_text));
            } else if (p.id.equals(selectedAnswerId)) {
                // User selected this wrong answer - show in red
                b.setBackgroundTintList(requireContext().getColorStateList(R.color.separation_game_wrong));
                b.setTextColor(requireContext().getColor(R.color.separation_game_text));
            } else {
                // Other buttons - grey out
                b.setBackgroundTintList(requireContext().getColorStateList(R.color.separation_game_button_pressed));
            }
        }

        // Wait 1 seconds before moving to next question or ending game
        feedbackRunnable = () -> {
            isShowingFeedback = false;
            selectedAnswerId = null;
            if (tvResult != null && !Boolean.TRUE.equals(vm.getGameOver().getValue())) {
                tvResult.setVisibility(View.GONE);
                tvResult.setTextSize(20);
            }
            vm.clearLastAnswerFeedback();

            vm.moveToNextQuestion();

            feedbackRunnable = null;

            updateUI();
        };

        handler.postDelayed(feedbackRunnable, 1000);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null);
    }
}