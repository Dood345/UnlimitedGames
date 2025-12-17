package com.appsters.unlimitedgames.games.poker;

import android.os.Bundle;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.appsters.unlimitedgames.databinding.FragmentPokerBinding;

public class PokerFragment extends Fragment {

    private FragmentPokerBinding binding;
    private PokerViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPokerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(PokerViewModel.class);
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(getViewLifecycleOwner());

        binding.buyIn5Button.setOnClickListener(v -> viewModel.selectBuyIn(5));
        binding.buyIn50Button.setOnClickListener(v -> viewModel.selectBuyIn(50));
        binding.buyIn500Button.setOnClickListener(v -> viewModel.selectBuyIn(500));

        binding.claimFreeCoinsButton.setOnClickListener(v -> viewModel.claimFreeCoins());
        binding.startHandButton.setOnClickListener(v -> viewModel.startHand());
        binding.checkButton.setOnClickListener(v -> viewModel.playerCheck());
        binding.raiseButton.setOnClickListener(v -> viewModel.playerRaise());
        binding.revealNextButton.setOnClickListener(v -> viewModel.revealNext());

        //Backs out of game
        binding.backToGamesButton.setOnClickListener(v -> {
            NavController navController =
                    NavHostFragment.findNavController(PokerFragment.this);
            navController.navigateUp();
        });

        // Raise slider: progress 0..(raiseMax-1) maps to raise amount 1..raiseMax
        binding.raiseAmountSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) viewModel.setRaiseSliderProgress(progress);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        // Keep slider max/value in sync with ViewModel
        viewModel.raiseMax.observe(getViewLifecycleOwner(), max -> {
            int safeMax = (max == null ? 1 : Math.max(1, max));
            binding.raiseAmountSeekBar.setMax(Math.max(0, safeMax - 1));
            Integer current = viewModel.raiseAmount.getValue();
            int amount = (current == null ? 1 : current);
            amount = Math.max(1, Math.min(safeMax, amount));
            binding.raiseAmountSeekBar.setProgress(amount - 1);
        });

        viewModel.raiseAmount.observe(getViewLifecycleOwner(), amount -> {
            Integer max = viewModel.raiseMax.getValue();
            int safeMax = (max == null ? 1 : Math.max(1, max));
            int a = (amount == null ? 1 : amount);
            a = Math.max(1, Math.min(safeMax, a));
            if (binding.raiseAmountSeekBar.getProgress() != a - 1) {
                binding.raiseAmountSeekBar.setProgress(a - 1);
            }


        });

// Highlight selected buy-in button
viewModel.selectedBuyIn.observe(getViewLifecycleOwner(), sel -> {
    int selectedColor = ContextCompat.getColor(requireContext(), android.R.color.holo_blue_light);
    int normalColor = ContextCompat.getColor(requireContext(), android.R.color.darker_gray);

    binding.buyIn5Button.setBackgroundTintList(ColorStateList.valueOf((sel != null && sel == 5) ? selectedColor : normalColor));
    binding.buyIn50Button.setBackgroundTintList(ColorStateList.valueOf((sel != null && sel == 50) ? selectedColor : normalColor));
    binding.buyIn500Button.setBackgroundTintList(ColorStateList.valueOf((sel != null && sel == 500) ? selectedColor : normalColor));
});


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
