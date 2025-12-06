package com.appsters.unlimitedgames.app.ui.friends;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.appsters.unlimitedgames.R;
import com.appsters.unlimitedgames.app.data.model.Friend;

public class FriendRequestAdapter
        extends ListAdapter<Friend, FriendRequestAdapter.RequestViewHolder> {

    private final boolean isOutgoing;
    private final OnAcceptListener onAcceptListener;
    private final OnDeclineListener onDeclineListener;

    public interface OnAcceptListener {
        void onAccept(String requestId);
    }

    public interface OnDeclineListener {
        void onDecline(String requestId);
    }

    public FriendRequestAdapter(
            boolean isOutgoing,
            OnAcceptListener acceptListener,
            OnDeclineListener declineListener
    ) {
        super(DIFF_CALLBACK);
        this.isOutgoing = isOutgoing;
        this.onAcceptListener = acceptListener;
        this.onDeclineListener = declineListener;
    }

    // ✅ CRASH-PROOF DIFFUTIL
    private static final DiffUtil.ItemCallback<Friend> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Friend>() {

                @Override
                public boolean areItemsTheSame(@NonNull Friend oldItem, @NonNull Friend newItem) {
                    return oldItem.getId().equals(newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Friend oldItem, @NonNull Friend newItem) {

                    return oldItem.getId().equals(newItem.getId()) &&
                            oldItem.getFromUserId().equals(newItem.getFromUserId()) &&
                            oldItem.getToUserId().equals(newItem.getToUserId()) &&
                            oldItem.getStatus().equals(newItem.getStatus());
                }
            };


    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend_request, parent, false);
        return new RequestViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class RequestViewHolder extends RecyclerView.ViewHolder {

        TextView tvUsername;
        Button btnAccept, btnDecline;

        RequestViewHolder(@NonNull View itemView) {
            super(itemView);

            tvUsername = itemView.findViewById(R.id.tvUsername);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnDecline = itemView.findViewById(R.id.btnDecline);
        }

        void bind(Friend friend) {

            // ✅ Display correct username
            String displayName = isOutgoing
                    ? friend.getToUsername()      // You sent it
                    : friend.getFromUsername();  // They sent it

            tvUsername.setText(displayName != null ? displayName : "Unknown User");

            if (isOutgoing) {
                // ✅ OUTGOING: Only Cancel
                btnAccept.setVisibility(View.GONE);
                btnDecline.setText("Cancel");

                btnDecline.setOnClickListener(v -> {
                    if (onDeclineListener != null) {
                        onDeclineListener.onDecline(friend.getId());
                    }
                });

            } else {
                // ✅ INCOMING: Accept + Decline
                btnAccept.setVisibility(View.VISIBLE);
                btnAccept.setText("Accept");
                btnDecline.setText("Decline");

                btnAccept.setOnClickListener(v -> {
                    if (onAcceptListener != null) {
                        onAcceptListener.onAccept(friend.getId());
                    }
                });

                btnDecline.setOnClickListener(v -> {
                    if (onDeclineListener != null) {
                        onDeclineListener.onDecline(friend.getId());
                    }
                });
            }
        }
    }
}
