package com.appsters.unlimitedgames.app.ui.friends;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.appsters.unlimitedgames.R;
import com.appsters.unlimitedgames.app.data.model.Friend;

import java.util.ArrayList;
import java.util.List;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendVH> {

    private List<Friend> items = new ArrayList<>();

    private final String currentUserId;

    public FriendAdapter(String currentUserId) {
        this.currentUserId = currentUserId;
    }

    public void submitList(List<Friend> list) {
        if (list == null)
            list = new ArrayList<>();
        items = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FriendVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FriendVH(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_friend, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FriendVH holder, int position) {
        Friend f = items.get(position);

        // ✅ Display the OTHER user's username
        String displayName;
        if (f.getFromUserId() != null && f.getFromUserId().equals(currentUserId)) {
            displayName = f.getToUsername();
        } else {
            displayName = f.getFromUsername();
        }

        holder.name.setText(displayName);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class FriendVH extends RecyclerView.ViewHolder {

        TextView name;

        FriendVH(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvFriendName);
        }
    }
}
