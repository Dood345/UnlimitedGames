package com.appsters.simpleGames.app.ui.friends;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.appsters.simpleGames.R;
import com.appsters.simpleGames.app.data.model.Friend;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.widget.ImageView;
import com.appsters.simpleGames.app.util.ImageHelper;
import android.content.res.Configuration;
import android.graphics.Color;

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

    public List<Friend> getItems() {
        return items;
    }

    public void removeItem(int position) {
        if (position >= 0 && position < items.size()) {
            items.remove(position);
            notifyItemRemoved(position);
        }
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

        // ✅ Profile Picture Logic
        if (f.getProfileBase64() != null && !f.getProfileBase64().isEmpty()) {
            Bitmap bitmap = ImageHelper.decodeBase64ToBitmap(f.getProfileBase64());
            if (bitmap != null) {
                holder.avatar.setImageBitmap(bitmap);
                holder.avatar.clearColorFilter(); // Clear any previous filter
            } else {
                setSilhouette(holder);
            }
        } else {
            setSilhouette(holder);
        }
    }

    private void setSilhouette(FriendVH holder) {
        int nightModeFlags = holder.itemView.getContext().getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        int color;
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            color = Color.WHITE;
        } else {
            color = Color.BLACK;
        }

        holder.avatar.setImageResource(R.drawable.ic_profile);
        holder.avatar.setColorFilter(color);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class FriendVH extends RecyclerView.ViewHolder {

        TextView name;
        ImageView avatar;

        FriendVH(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvFriendName);
            avatar = itemView.findViewById(R.id.friend_avatar);
        }
    }
}
