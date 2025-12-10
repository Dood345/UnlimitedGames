package com.appsters.unlimitedgames.app.ui.friends;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.appsters.unlimitedgames.R;
import com.appsters.unlimitedgames.databinding.FragmentFriendsBinding;
import com.google.firebase.auth.FirebaseAuth;

public class FriendFragment extends Fragment {

    private FragmentFriendsBinding binding;
    private FriendViewModel viewModel;
    private FriendAdapter adapter;
    private NavController navController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentFriendsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);

        viewModel = new ViewModelProvider(requireActivity()).get(FriendViewModel.class);
        adapter = new FriendAdapter();

        binding.rvFriends.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvFriends.setAdapter(adapter);

        setupObservers();
        setupClickListeners();

        String userId = FirebaseAuth.getInstance().getUid();
        if (userId != null) {
            viewModel.loadFriends(userId);
        }
    }

    private void setupObservers() {
        viewModel.getFriends().observe(getViewLifecycleOwner(), friends -> {
            adapter.submitList(friends);
            binding.emptyText.setVisibility(friends.isEmpty() ? View.VISIBLE : View.GONE);
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading ->
                binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE)
        );

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupClickListeners() {
        binding.btnFindFriends.setOnClickListener(v ->
                navController.navigate(R.id.action_friendsFragment_to_findFriendsFragment)
        );

        binding.btnFriendRequests.setOnClickListener(v ->
                navController.navigate(R.id.action_friendsFragment_to_friendRequestsFragment)
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
