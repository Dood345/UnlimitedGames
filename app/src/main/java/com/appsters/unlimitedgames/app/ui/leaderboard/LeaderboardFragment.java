package com.appsters.unlimitedgames.app.ui.leaderboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.appsters.unlimitedgames.R;
import com.appsters.unlimitedgames.app.data.GameDataSource;
import com.appsters.unlimitedgames.app.data.model.Game;
import com.appsters.unlimitedgames.app.util.GameType;
import com.appsters.unlimitedgames.databinding.FragmentLeaderboardBinding;

import java.util.List;
import java.util.stream.Collectors;

public class LeaderboardFragment extends Fragment {

    private FragmentLeaderboardBinding binding;
    private LeaderboardViewModel viewModel;
    private List<Game> games;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLeaderboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(LeaderboardViewModel.class);
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(getViewLifecycleOwner());

        setupSpinner();
        setupSwitch();
        setupRecyclerView();
        setupObservers();

        viewModel.loadLeaderboard();
    }

    private void setupSpinner() {
        games = GameDataSource.getGames();
        List<String> gameTitles = games.stream().map(Game::getTitle).collect(Collectors.toList());
        gameTitles.add(0, "All Games");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.spinner_item_white_text, gameTitles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.gameSpinner.setAdapter(adapter);
        binding.gameSpinner.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.spinner_background));

        binding.gameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    viewModel.setGameType(GameType.ALL);
                } else {
                    viewModel.setGameType(games.get(position - 1).getGameType());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupSwitch() {
        binding.friendsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.setShowFriendsOnly(isChecked);
        });
    }

    private void setupRecyclerView() {
        binding.leaderboardRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void setupObservers() {
        viewModel.getLeaderboard().observe(getViewLifecycleOwner(), scores -> {
            if (scores != null) {
                GameType selectedGameType = viewModel.getSelectedGame();
                binding.leaderboardRecyclerView.setAdapter(new LeaderboardAdapter(scores, selectedGameType));
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
