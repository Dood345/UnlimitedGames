package com.appsters.unlimitedgames.ui.home;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.appsters.unlimitedgames.databinding.ItemGameBinding;
import com.appsters.unlimitedgames.util.GameType;

import java.util.List;

/**
 * A RecyclerView adapter for displaying a grid of games.
 */
public class GameAdapter extends RecyclerView.Adapter<GameAdapter.GameViewHolder> {

    private final List<GameType> games;

    /**
     * Constructs a new GameAdapter.
     *
     * @param games The list of games to display.
     */
    public GameAdapter(List<GameType> games) {
        this.games = games;
    }

    /**
     * Called when RecyclerView needs a new {@link GameViewHolder} of the given type to represent
     * an item.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return A new GameViewHolder that holds a View of the given view type.
     */
    @NonNull
    @Override
    public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemGameBinding binding = ItemGameBinding.inflate(inflater, parent, false);
        return new GameViewHolder(binding);
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the {@link GameViewHolder#itemView} to reflect the item at the given
     * position.
     *
     * @param holder   The GameViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull GameViewHolder holder, int position) {
        holder.bind(games.get(position));
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return games.size();
    }

    /**
     * A ViewHolder that describes an item view and metadata about its place within the RecyclerView.
     */
    public static class GameViewHolder extends RecyclerView.ViewHolder {

        private final ItemGameBinding binding;

        /**
         * Constructs a new GameViewHolder.
         *
         * @param binding The data binding for the item view.
         */
        public GameViewHolder(ItemGameBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        /**
         * Binds a GameType to the item view.
         *
         * @param gameType The GameType to bind.
         */
        public void bind(GameType gameType) {
            binding.setGameTitle(gameType.getDisplayName());
            // TODO: Set game image
            binding.executePendingBindings();
        }
    }
}
