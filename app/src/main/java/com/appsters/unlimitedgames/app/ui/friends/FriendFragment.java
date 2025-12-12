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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ColorDrawable;
import androidx.core.content.ContextCompat;

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

        // ✅ Swipe to Delete with Red Background & "Remove"
        new androidx.recyclerview.widget.ItemTouchHelper(
                new androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback(0,
                        androidx.recyclerview.widget.ItemTouchHelper.LEFT) {

                    private final ColorDrawable background = new ColorDrawable(Color.RED);
                    private final Paint textPaint = new Paint();

                    {
                        textPaint.setColor(Color.WHITE);
                        textPaint.setTextSize(60f); // adjusted size for visibility
                        textPaint.setAntiAlias(true);
                        textPaint.setTextAlign(Paint.Align.RIGHT);
                    }

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

                            // 1. Optimistic Update (Immediate Removal)
                            adapter.removeItem(position);

                            // 2. Database Update
                            viewModel.removeFriend(userId, otherUserId);
                            Toast.makeText(requireContext(), "Friend removed", Toast.LENGTH_SHORT).show();

                            // Note: We do NOT call loadFriends immediately to avoid race condition
                            // replacing list with old data.
                            // The cached/optimistic list is correct.
                        }
                    }

                    @Override
                    public void onChildDraw(@NonNull Canvas c,
                            @NonNull androidx.recyclerview.widget.RecyclerView recyclerView,
                            @NonNull androidx.recyclerview.widget.RecyclerView.ViewHolder viewHolder, float dX,
                            float dY, int actionState, boolean isCurrentlyActive) {
                        if (actionState == androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_SWIPE) {
                            View itemView = viewHolder.itemView;

                            if (dX < 0) { // Swiping Left
                                background.setBounds(itemView.getRight() + (int) dX, itemView.getTop(),
                                        itemView.getRight(), itemView.getBottom());
                                background.draw(c);

                                // Draw "Remove" text vertically centered, with 32dp margin from right
                                float margin = 32f * getResources().getDisplayMetrics().density;
                                float textY = itemView.getTop() + ((itemView.getBottom() - itemView.getTop()) / 2f)
                                        - ((textPaint.descent() + textPaint.ascent()) / 2f);
                                c.drawText("Remove", itemView.getRight() - margin, textY, textPaint);
                            }
                        }
                        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                    }
                }).attachToRecyclerView(binding.rvFriends);

        if (userId != null) {
            viewModel.loadFriends(userId);
            viewModel.listenToRequestCount(userId);
        }
    }

    private void setupObservers() {
        viewModel.getFriends().observe(getViewLifecycleOwner(), friends -> {
            adapter.submitList(friends);
            binding.emptyText.setVisibility(friends.isEmpty() ? View.VISIBLE : View.GONE);
        });

        // ✅ Update Requests Badge Bubble
        viewModel.getOngoingRequestCount().observe(getViewLifecycleOwner(), count -> {
            // Reset button text just in case
            binding.btnFriendRequests.setText("Requests");

            if (count > 0) {
                binding.tvRequestsBadge.setText(String.valueOf(count));
                binding.tvRequestsBadge.setVisibility(View.VISIBLE);
            } else {
                binding.tvRequestsBadge.setVisibility(View.GONE);
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
