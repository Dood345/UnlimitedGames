package com.appsters.unlimitedgames.games.whackamole;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.appsters.unlimitedgames.R;
import com.appsters.unlimitedgames.databinding.FragmentWhackAMoleTitleBinding;
import com.appsters.unlimitedgames.games.whackamole.repository.SharedPrefGameRepository;

public class WhackAMoleTitleFragment extends Fragment {

    private FragmentWhackAMoleTitleBinding binding;
    private WhackAMoleTitleViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentWhackAMoleTitleBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences prefs = requireActivity().getSharedPreferences("WhackAMolePrefs", Context.MODE_PRIVATE);
        SharedPrefGameRepository repository = new SharedPrefGameRepository(prefs);
        viewModel = new ViewModelProvider(this, new WhackAMoleTitleViewModel.Factory(repository))
                .get(WhackAMoleTitleViewModel.class);

        binding.startButton.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_whackAMoleTitleFragment_to_whackAMoleGameFragment));

        binding.clearScoreButton.setOnClickListener(v -> viewModel.clearHighScore());

        viewModel.getHighScore().observe(getViewLifecycleOwner(), highScore -> 
            binding.highScore.setText("High Score: " + highScore));
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.getHighScore().observe(getViewLifecycleOwner(), highScore ->
                binding.highScore.setText("High Score: " + highScore));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}