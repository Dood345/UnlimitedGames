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

        String userId = FirebaseAuth.getInstance().getUid();
        adapter = new FriendAdapter(userId);

        binding.rvFriends.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvFriends.setAdapter(adapter);

        setupObservers();
        setupClickListeners();

        // ✅ Swipe to Delete
        new androidx.recyclerview.widget.ItemTouchHelper(
                new androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback(0,
                        androidx.recyclerview.widget.ItemTouchHelper.LEFT) {
                    @Override
                    public boolean onMove(@NonNull androidx.recyclerview.widget.RecyclerView recyclerView,
                            @NonNull androidx.recyclerview.widget.RecyclerView.ViewHolder viewHolder,
                            @NonNull androidx.recyclerview.widget.RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(@NonNull androidx.recyclerview.widget.RecyclerView.ViewHolder viewHolder,
                            int direction) {
                        int position = viewHolder.getAdapterPosition();
                        com.appsters.unlimitedgames.app.data.model.Friend friend = adapter.getItems().get(position);

                        String userId = FirebaseAuth.getInstance().getUid();
                        if (userId != null) {
                            // Identify other user
                            String otherUserId = friend.getFromUserId().equals(userId) ? friend.getToUserId()
                                    : friend.getFromUserId();

                            viewModel.removeFriend(userId, otherUserId);
                            Toast.makeText(requireContext(), "Friend removed", Toast.LENGTH_SHORT).show();

                            // Reload list (Ideally we would just remove from list but we rely on simple
                            // reload for now)
                            viewModel.loadFriends(userId);
                        }
                    }
                }).attachToRecyclerView(binding.rvFriends);

        if (userId != null) {
            viewModel.loadFriends(userId);
            viewModel.listenToRequestCount(userId); // Ensure listener is active here too if needed
        }
    }

    private void setupObservers() {
        viewModel.getFriends().observe(getViewLifecycleOwner(), friends -> {
            adapter.submitList(friends);
            binding.emptyText.setVisibility(friends.isEmpty() ? View.VISIBLE : View.GONE);
        });

        // ✅ Update Requests Button Text
        viewModel.getOngoingRequestCount().observe(getViewLifecycleOwner(), count -> {
            if (count > 0) {
                binding.btnFriendRequests.setText("Requests (" + count + ")");
            } else {
                binding.btnFriendRequests.setText("Requests");
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(),
                loading -> binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE));

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupClickListeners() {
        binding.btnFindFriends
                .setOnClickListener(v -> navController.navigate(R.id.action_friendsFragment_to_findFriendsFragment));

        binding.btnFriendRequests
                .setOnClickListener(v -> navController.navigate(R.id.action_friendsFragment_to_friendRequestsFragment));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
