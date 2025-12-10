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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.appsters.unlimitedgames.app.data.model.Friend;
import com.appsters.unlimitedgames.databinding.FragmentFriendRequestsBinding;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class FriendRequestFragment extends Fragment {

    private FragmentFriendRequestsBinding binding;
    private FriendViewModel viewModel;

    private FriendRequestAdapter incomingAdapter;
    private FriendRequestAdapter outgoingAdapter;

    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentFriendRequestsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(FriendViewModel.class);
        viewModel.resetFlags();

        currentUserId = FirebaseAuth.getInstance().getUid();
        if (currentUserId == null) {
            Toast.makeText(requireContext(), "Not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        setupRecyclerViews();
        setupObservers();

        // Load data
        viewModel.loadIncomingRequests(currentUserId);
        viewModel.loadOutgoingRequests(currentUserId);
    }

    private void setupRecyclerViews() {
        incomingAdapter = new FriendRequestAdapter(
                false,
                requestId -> viewModel.acceptFriendRequest(requestId),
                requestId -> viewModel.declineFriendRequest(requestId)
        );

        outgoingAdapter = new FriendRequestAdapter(
                true,
                null, // no accept option for outgoing
                requestId -> viewModel.declineFriendRequest(requestId)
        );

        binding.rvIncomingRequests.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvIncomingRequests.setAdapter(incomingAdapter);

        binding.rvOutgoingRequests.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvOutgoingRequests.setAdapter(outgoingAdapter);
    }

    private void setupObservers() {

        viewModel.getIncomingRequests().observe(getViewLifecycleOwner(), list -> {
            incomingAdapter.submitList(list);
            updateSectionVisibility();
        });

        viewModel.getOutgoingRequests().observe(getViewLifecycleOwner(), list -> {
            outgoingAdapter.submitList(list);
            updateSectionVisibility();
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(),
                loading -> binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE)
        );

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getActionSuccess().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {

                Toast.makeText(requireContext(),
                        "Updated successfully",
                        Toast.LENGTH_SHORT).show();

                // ✅ RELOAD BOTH SECTIONS
                viewModel.loadIncomingRequests(currentUserId);
                viewModel.loadOutgoingRequests(currentUserId);

                viewModel.resetFlags();
            }
        });
    }

    private void updateSectionVisibility() {

        List<Friend> incoming = viewModel.getIncomingRequests().getValue();
        List<Friend> outgoing = viewModel.getOutgoingRequests().getValue();

        boolean incomingEmpty = incoming == null || incoming.isEmpty();
        boolean outgoingEmpty = outgoing == null || outgoing.isEmpty();

        binding.tvIncomingTitle.setVisibility(incomingEmpty ? View.GONE : View.VISIBLE);
        binding.rvIncomingRequests.setVisibility(incomingEmpty ? View.GONE : View.VISIBLE);

        binding.tvOutgoingTitle.setVisibility(outgoingEmpty ? View.GONE : View.VISIBLE);
        binding.rvOutgoingRequests.setVisibility(outgoingEmpty ? View.GONE : View.VISIBLE);

        binding.tvEmpty.setVisibility(
                incomingEmpty && outgoingEmpty ? View.VISIBLE : View.GONE
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
