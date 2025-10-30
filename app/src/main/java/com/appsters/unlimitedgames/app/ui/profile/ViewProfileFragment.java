package com.appsters.unlimitedgames.app.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.appsters.unlimitedgames.databinding.FragmentViewProfileBinding;

public class ViewProfileFragment extends Fragment {

    private FragmentViewProfileBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentViewProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // TODO: Implement view profile screen
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
