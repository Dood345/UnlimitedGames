package com.appsters.unlimitedgames.app.ui.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.appsters.unlimitedgames.R;
import com.appsters.unlimitedgames.app.data.model.Score;
import com.appsters.unlimitedgames.app.util.GameType;

import java.util.List;

public class ProfileScoreAdapter extends RecyclerView.Adapter<ProfileScoreAdapter.ViewHolder> {

    private final List<Score> scores;

    public ProfileScoreAdapter(List<Score> scores) {
        this.scores = scores;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_profile_score, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Score score = scores.get(position);
        holder.gameTitle.setText(getGameName(score.getGameType())); // Use helper to get a clean name
        holder.highScore.setText(String.valueOf(score.getScore()));

        if (score.getRank() != 0) {
            String medal = "";
            switch (score.getRank()) {
                case 1:
                    medal = "🥇";
                    break;
                case 2:
                    medal = "🥈";
                    break;
                case 3:
                    medal = "🥉";
                    break;
            }
            if (!medal.isEmpty()) {
                holder.medal.setText(medal);
                holder.medal.setVisibility(View.VISIBLE);
            } else {
                holder.medal.setVisibility(View.GONE);
            }
        } else {
            holder.medal.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return scores.size();
    }

    /**
     * Converts a GameType enum into a user-friendly, readable string.
     * 
     * @param gameType The enum to convert.
     * @return A readable string for the UI.
     */
    private String getGameName(GameType gameType) {
        if (gameType == null)
            return "Game";
        switch (gameType) {
            case GAME2048:
                return "2048";
            case SUDOKU:
                return "Sudoku";
            case SOCCERSEPARATION:
                return "NFL Quiz";
            case POKER:
                return "Poker";
            case MAZE:
                return "Maze";
            case WHACK_A_MOLE:
                return "Whack-A-Mole";
            default:
                // Capitalize the first letter and make the rest lowercase, replacing
                // underscores
                String name = gameType.name().replace("_", " ").toLowerCase();
                return name.substring(0, 1).toUpperCase() + name.substring(1);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView gameTitle, highScore, medal;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            gameTitle = itemView.findViewById(R.id.game_title);
            highScore = itemView.findViewById(R.id.high_score);
            medal = itemView.findViewById(R.id.medal);
        }
    }
}
