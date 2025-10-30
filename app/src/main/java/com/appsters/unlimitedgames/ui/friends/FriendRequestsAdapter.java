package com.appsters.unlimitedgames.ui.friends;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.appsters.unlimitedgames.databinding.ItemFriendRequestBinding;
import com.appsters.unlimitedgames.model.User;

public class FriendRequestsAdapter extends ListAdapter<User, FriendRequestsAdapter.FriendRequestViewHolder> {

    private final OnFriendRequestInteractionListener listener;

    public FriendRequestsAdapter(OnFriendRequestInteractionListener listener) {
        super(new DiffUtil.ItemCallback<User>() {
            @Override
            public boolean areItemsTheSame(@NonNull User oldItem, @NonNull User newItem) {
                return oldItem.getUid().equals(newItem.getUid());
            }

            @Override
            public boolean areContentsTheSame(@NonNull User oldItem, @NonNull User newItem) {
                return oldItem.equals(newItem);
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public FriendRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFriendRequestBinding binding = ItemFriendRequestBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new FriendRequestViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendRequestViewHolder holder, int position) {
        User user = getItem(position);
        holder.bind(user, listener);
    }

    public interface OnFriendRequestInteractionListener {
        void onAcceptFriendRequest(User user);

        void onDeclineFriendRequest(User user);
    }

    static class FriendRequestViewHolder extends RecyclerView.ViewHolder {

        private final ItemFriendRequestBinding binding;

        public FriendRequestViewHolder(ItemFriendRequestBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(User user, OnFriendRequestInteractionListener listener) {
            binding.textViewEmail.setText(user.getEmail());
            binding.buttonAccept.setOnClickListener(v -> listener.onAcceptFriendRequest(user));
            binding.buttonDecline.setOnClickListener(v -> listener.onDeclineFriendRequest(user));
        }
    }
}
