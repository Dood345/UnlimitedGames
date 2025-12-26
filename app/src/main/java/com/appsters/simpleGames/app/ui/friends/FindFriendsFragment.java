package com.appsters.simpleGames.app.ui.friends;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.appsters.simpleGames.app.data.model.User;
import com.appsters.simpleGames.databinding.FragmentFindFriendsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class FindFriendsFragment extends Fragment {

    private FragmentFindFriendsBinding binding;
    private FriendViewModel viewModel;
    private FindFriendsAdapter adapter;

    private String currentUserId;

    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentFindFriendsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(FriendViewModel.class);
        viewModel.resetFlags();

        setupCurrentUser();
        setupRecyclerView();
        setupObservers();
        setupSearchInput();
    }

    // ✅ CURRENT USER FROM FIRESTORE (NOT AUTH DISPLAY NAME)
    private void setupCurrentUser() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser == null) {
            Toast.makeText(requireContext(), "User session expired.", Toast.LENGTH_SHORT).show();
            requireActivity().finish();
            return;
        }

        currentUserId = firebaseUser.getUid();

        // ✅ Load Firestore user
        viewModel.loadCurrentUser(currentUserId);

        // ✅ Only load find-friends AFTER current user loads
        viewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user == null) {
                Toast.makeText(requireContext(),
                        "Failed to load your profile.",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.loadAllNewFriends(currentUserId);
        });
    }

    private void setupRecyclerView() {
        adapter = new FindFriendsAdapter(user -> {

            User me = viewModel.getCurrentUser().getValue();

            if (me == null || me.getUsername() == null) {
                Toast.makeText(requireContext(),
                        "Your profile is still loading.",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (user == null || user.getUserId() == null || user.getUsername() == null) {
                Toast.makeText(requireContext(),
                        "Invalid user data. Try again.",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // ✅ REAL USERNAMES FROM FIRESTORE (NO MORE "UNKNOWN")
            viewModel.sendFriendRequest(
                    me.getUserId(),
                    user.getUserId(),
                    me.getUsername(),
                    user.getUsername());
        });

        binding.rvSearchResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvSearchResults.setAdapter(adapter);
        binding.rvSearchResults.setHasFixedSize(true);
    }

    private void setupObservers() {

        viewModel.getNewFriends().observe(getViewLifecycleOwner(), users -> {
            if (users == null || users.isEmpty()) {
                adapter.submitList(new ArrayList<>());
                binding.emptyText.setVisibility(View.VISIBLE);
            } else {
                adapter.submitList(users);
                binding.emptyText.setVisibility(View.GONE);
            }
        });

        viewModel.getActionSuccess().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(requireContext(),
                        "Friend request sent!",
                        Toast.LENGTH_SHORT).show();
                viewModel.resetFlags();
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ✅ DEBOUNCED SEARCH (UNCHANGED)
    private void setupSearchInput() {

        binding.etSearchFriends.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }

                searchRunnable = () -> applyFilter(s.toString());
                searchHandler.postDelayed(searchRunnable, 250);
            }
        });
    }

    private void applyFilter(String query) {
        // Delegate search to ViewModel for server-side privacy filtering
        viewModel.searchNewFriends(query);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
