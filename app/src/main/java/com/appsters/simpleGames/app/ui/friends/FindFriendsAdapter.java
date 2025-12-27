package com.appsters.simpleGames.app.ui.friends;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.appsters.simpleGames.R;
import com.appsters.simpleGames.app.data.model.User;

public class FindFriendsAdapter
        extends ListAdapter<User, FindFriendsAdapter.FindFriendViewHolder> {

    public interface OnSendRequestListener {
        void onSendRequest(User user);
    }

    private final OnSendRequestListener listener;

    public FindFriendsAdapter(OnSendRequestListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<User> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<User>() {
                @Override
                public boolean areItemsTheSame(@NonNull User oldItem, @NonNull User newItem) {
                    return oldItem.getUserId().equals(newItem.getUserId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull User oldItem, @NonNull User newItem) {
                    return oldItem.equals(newItem);
                }
            };

    @NonNull
    @Override
    public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_find_friend, parent, false);
        return new FindFriendViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FindFriendViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class FindFriendViewHolder extends RecyclerView.ViewHolder {

        TextView tvUsername;
        TextView tvEmail;
        Button btnAdd;

        public FindFriendViewHolder(@NonNull View itemView) {
            super(itemView);

            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            btnAdd = itemView.findViewById(R.id.btnAddFriend);
        }

        void bind(User user) {
            tvUsername.setText(user.getUsername());
            tvEmail.setText(user.getEmail());

            btnAdd.setOnClickListener(v -> {
                if (listener != null) listener.onSendRequest(user);
            });
        }
    }
}
