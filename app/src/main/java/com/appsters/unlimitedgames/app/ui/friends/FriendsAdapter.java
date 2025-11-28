package com.appsters.unlimitedgames.app.ui.friends;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.appsters.unlimitedgames.R;
import com.appsters.unlimitedgames.app.data.model.User;
import com.appsters.unlimitedgames.databinding.ItemFriendBinding;

/**
 * Adapter for displaying the user's friends list in a Material Card style.
 */
public class FriendsAdapter extends ListAdapter<User, FriendsAdapter.FriendViewHolder> {

    /** Listener for handling actions (like removing friends). */
    public interface OnFriendActionListener {
        void onRemoveFriend(User friend);
    }

    private final OnFriendActionListener listener;

    public FriendsAdapter(OnFriendActionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<User> DIFF_CALLBACK = new DiffUtil.ItemCallback<User>() {
        @Override
        public boolean areItemsTheSame(@NonNull User oldItem, @NonNull User newItem) {
            return oldItem.getUserId().equals(newItem.getUserId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull User oldItem, @NonNull User newItem) {
            return oldItem.getUsername().equals(newItem.getUsername()) &&
                    oldItem.getEmail().equals(newItem.getEmail());
        }
    };

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemFriendBinding binding = ItemFriendBinding.inflate(inflater, parent, false);
        return new FriendViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class FriendViewHolder extends RecyclerView.ViewHolder {
        private final ItemFriendBinding binding;

        FriendViewHolder(ItemFriendBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(User friend) {
            binding.textFriendName.setText(friend.getUsername());
            binding.textFriendEmail.setText(friend.getEmail());

            // If profile pictures are available, load with Glide or default
            binding.friendProfileImage.setImageResource(R.drawable.ic_person);

            binding.buttonRemoveFriend.setOnClickListener(v -> {
                if (listener != null) listener.onRemoveFriend(friend);
            });
        }
    }
}
