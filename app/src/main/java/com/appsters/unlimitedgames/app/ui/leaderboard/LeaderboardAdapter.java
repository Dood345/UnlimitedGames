package com.appsters.unlimitedgames.app.ui.leaderboard;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.appsters.unlimitedgames.app.data.model.Score;
import com.appsters.unlimitedgames.databinding.ItemLeaderboardBinding;

import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder> {

    private final List<Score> scores;

    public LeaderboardAdapter(List<Score> scores) {
        this.scores = scores;
    }

    @NonNull
    @Override
    public LeaderboardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemLeaderboardBinding binding = ItemLeaderboardBinding.inflate(inflater, parent, false);
        return new LeaderboardViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull LeaderboardViewHolder holder, int position) {
        holder.bind(scores.get(position), position + 1);
    }

    @Override
    public int getItemCount() {
        return scores.size();
    }

    public static class LeaderboardViewHolder extends RecyclerView.ViewHolder {

        private final ItemLeaderboardBinding binding;

        public LeaderboardViewHolder(ItemLeaderboardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Score score, int rank) {
            binding.rank.setText(String.valueOf(rank));
            binding.username.setText(score.getUsername());
            binding.score.setText(String.valueOf(score.getScore()));
        }
    }
}
