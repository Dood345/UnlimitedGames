package com.appsters.unlimitedgames.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.appsters.unlimitedgames.databinding.FragmentHomeBinding;
import com.appsters.unlimitedgames.util.GameType;

import java.util.Arrays;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        GameAdapter adapter = new GameAdapter(Arrays.asList(GameType.values()));
        binding.gamesRecyclerView.setAdapter(adapter);
        binding.gamesRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
