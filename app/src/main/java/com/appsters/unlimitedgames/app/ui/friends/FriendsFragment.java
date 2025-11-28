package com.appsters.unlimitedgames.app.ui.friends;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.appsters.unlimitedgames.app.data.model.FriendRequest;
import com.appsters.unlimitedgames.app.data.model.User;
import com.appsters.unlimitedgames.databinding.FragmentFriendsBinding;

import java.util.List;

/**
 * Friends screen — allows searching users, viewing pending requests, and managing friend list.
 */
public class FriendsFragment extends Fragment
        implements FriendRequestsAdapter.OnFriendRequestInteractionListener,
        FriendsAdapter.OnFriendActionListener {

    private FragmentFriendsBinding binding;
    private FriendsViewModel viewModel;
    private FriendRequestsAdapter requestsAdapter;
    private FriendsAdapter friendsAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentFriendsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(FriendsViewModel.class);

        // === Setup RecyclerViews ===
        requestsAdapter = new FriendRequestsAdapter(this);
        friendsAdapter = new FriendsAdapter(this);

        binding.recyclerPendingRequests.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerPendingRequests.setAdapter(requestsAdapter);

        binding.recyclerFriends.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerFriends.setAdapter(friendsAdapter);

        // === Observe LiveData ===
        viewModel.getFriendRequests().observe(getViewLifecycleOwner(), this::updateRequests);
        viewModel.getFriendsProfiles().observe(getViewLifecycleOwner(), this::updateFriends);
        viewModel.getIsLoading().observe(getViewLifecycleOwner(),
                isLoading -> binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE));
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(),
                msg -> { if (msg != null) Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show(); });

        // === Buttons ===
        binding.textInputSearchEmail.setEndIconOnClickListener(v -> onSearchClicked());

        binding.buttonShowQr.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Show your QR code (TODO: generate UID QR)", Toast.LENGTH_SHORT).show();
            // TODO: Generate and display QR code using user's Firebase UID
        });

        binding.buttonScanQr.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Scan a friend’s QR code (TODO: launch scanner)", Toast.LENGTH_SHORT).show();
            // TODO: Launch MLKit / ZXing QR scanner here
        });

        // Load data
        viewModel.loadFriendsAndRequests();
    }

    private void onSearchClicked() {
        String email = binding.editTextSearchEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(requireContext(), "Enter an email to search", Toast.LENGTH_SHORT).show();
            return;
        }
        viewModel.sendFriendRequest(email);
    }

    private void updateRequests(List<FriendRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            binding.textPendingHeader.setVisibility(View.GONE);
        } else {
            binding.textPendingHeader.setVisibility(View.VISIBLE);
            requestsAdapter.submitList(requests);
        }
    }

    private void updateFriends(List<User> friends) {
        friendsAdapter.submitList(friends);
    }

    @Override
    public void onAcceptFriendRequest(FriendRequest request) {
        viewModel.acceptFriendRequest(request);
    }

    @Override
    public void onDeclineFriendRequest(FriendRequest request) {
        viewModel.declineFriendRequest(request);
    }

    @Override
    public void onRemoveFriend(User friend) {
        viewModel.removeFriend(friend.getUserId());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
