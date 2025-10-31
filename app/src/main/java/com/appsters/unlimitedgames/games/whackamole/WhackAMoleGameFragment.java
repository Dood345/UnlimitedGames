package com.appsters.unlimitedgames.games.whackamole;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.appsters.unlimitedgames.R;
import com.appsters.unlimitedgames.databinding.FragmentWhackAMoleGameBinding;
import com.appsters.unlimitedgames.games.whackamole.model.Mole;
import com.appsters.unlimitedgames.games.whackamole.model.MoleColor;
import com.appsters.unlimitedgames.games.whackamole.repository.SharedPrefGameRepository;
import com.appsters.unlimitedgames.games.whackamole.util.AndroidScheduler;
import java.util.ArrayList;
import java.util.List;

public class WhackAMoleGameFragment extends Fragment {

    private FragmentWhackAMoleGameBinding binding;
    private WhackAMoleGameViewModel viewModel;
    private final List<ImageButton> moleImageViews = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentWhackAMoleGameBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences prefs = requireActivity().getSharedPreferences("WhackAMolePrefs", Context.MODE_PRIVATE);
        SharedPrefGameRepository repository = new SharedPrefGameRepository(prefs);
        AndroidScheduler scheduler = new AndroidScheduler();
        viewModel = new ViewModelProvider(this, new WhackAMoleGameViewModel.Factory(repository, scheduler))
                .get(WhackAMoleGameViewModel.class);

        setupMoleViews();
        observeViewModel();
    }

    private void setupMoleViews() {
        moleImageViews.add(binding.mole0);
        moleImageViews.add(binding.mole1);
        moleImageViews.add(binding.mole2);
        moleImageViews.add(binding.mole3);
        moleImageViews.add(binding.mole4);
        moleImageViews.add(binding.mole5);
        moleImageViews.add(binding.mole6);
        moleImageViews.add(binding.mole7);
        moleImageViews.add(binding.mole8);

        for (int i = 0; i < moleImageViews.size(); i++) {
            int moleId = i;
            moleImageViews.get(i).setOnClickListener(v -> onMoleWhacked(moleId));
            moleImageViews.get(i).setVisibility(View.INVISIBLE);
        }
    }

    private void onMoleWhacked(int moleId) {
        viewModel.hitMole(moleId);
    }

    private void observeViewModel() {
        viewModel.getScore().observe(getViewLifecycleOwner(), score ->
                binding.score.setText(getString(R.string.score_format, score)));

        viewModel.getMisses().observe(getViewLifecycleOwner(), misses -> {
            int livesRemaining = GameConfig.DEFAULT.getMaxMisses() - misses;
            binding.lives.setText(getString(R.string.lives_format, livesRemaining));
        });

        viewModel.getMoles().observe(getViewLifecycleOwner(), moleContainer -> {
            for (Mole mole : moleContainer.getMoles()) {
                updateMoleView(mole);
            }
        });

        viewModel.getGameOver().observe(getViewLifecycleOwner(), isGameOver -> {
            if (isGameOver) {
                endGame();
            }
        });
    }

    private void updateMoleView(Mole mole) {
        if (moleImageViews.isEmpty()) {
            return;
        }

        ImageButton moleView = moleImageViews.get(mole.getId());

        if (mole.isVisible()) {
            moleView.setVisibility(View.VISIBLE);
            int colorResId = getColorForMole(mole.getColor());
            moleView.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), colorResId));
        } else {
            moleView.setVisibility(View.INVISIBLE);
        }
    }

    private int getColorForMole(MoleColor color) {
        switch (color) {
            case RED:
                return R.color.mole_red;
            case BLUE:
                return R.color.mole_blue;
            case GREEN:
                return R.color.mole_green;
            case YELLOW:
                return R.color.mole_yellow;
            case PURPLE:
                return R.color.mole_purple;
        }
        return R.color.black;
    }

    private void endGame() {
        Integer finalScore = viewModel.getScore().getValue();
        if (finalScore == null) finalScore = 0;

        Integer highScore = viewModel.getHighScore().getValue();
        if (highScore == null) highScore = 0;

        String message = (finalScore > highScore)
                ? "New High Score: " + finalScore + "!"
                : "Game Over! Score: " + finalScore + "
High Score: " + highScore;

        new AlertDialog.Builder(requireContext())
                .setTitle("Game Over")
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Restart", (dialog, which) -> {
                    dialog.dismiss();
                    restartGame();
                })
                .setNegativeButton("Main Menu", (dialog, which) -> {
                    dialog.dismiss();
                    requireActivity().getSupportFragmentManager().popBackStack();
                })
                .show();
    }

    private void restartGame() {
        viewModel.resetGame();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}