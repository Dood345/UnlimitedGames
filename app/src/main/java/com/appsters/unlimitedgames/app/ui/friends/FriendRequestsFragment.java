package com.appsters.unlimitedgames.app.ui.friends;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.appsters.unlimitedgames.databinding.FragmentFriendRequestsBinding;
import com.appsters.unlimitedgames.app.data.model.User;

public class FriendRequestsFragment extends Fragment implements FriendRequestsAdapter.OnFriendRequestInteractionListener {

    private FragmentFriendRequestsBinding binding;
    private FriendsViewModel viewModel;
    private FriendRequestsAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(FriendsViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFriendRequestsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new FriendRequestsAdapter(this);
        binding.recyclerViewFriendRequests.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewFriendRequests.setAdapter(adapter);

        viewModel.getFriendRequests().observe(getViewLifecycleOwner(), friendRequests -> {
            adapter.submitList(friendRequests);
        });

        viewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onAcceptFriendRequest(User user) {
        viewModel.acceptFriendRequest(user);
    }

    @Override
    public void onDeclineFriendRequest(User user) {
        viewModel.declineFriendRequest(user);
    }
}
