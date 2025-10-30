package com.appsters.unlimitedgames.games.game2048;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.appsters.unlimitedgames.databinding.FragmentGame2048Binding;

public class Game2048Fragment extends Fragment {

    private static final String TAG = "Game2048Fragment";
    private FragmentGame2048Binding binding;
    private Game2048ViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(Game2048ViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentGame2048Binding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // TODO: Implement 2048 game logic
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
