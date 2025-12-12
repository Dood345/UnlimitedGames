package com.appsters.unlimitedgames.app.ui.home;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.appsters.unlimitedgames.app.data.model.Game;
import com.appsters.unlimitedgames.databinding.ItemGameBinding;

import java.util.List;

/**
 * A RecyclerView adapter for displaying a grid of games.
 */
public class GameAdapter extends RecyclerView.Adapter<GameAdapter.GameViewHolder> {

    private final List<Game> games;
    private OnItemClickListener listener;

    /**
     * Constructs a new GameAdapter.
     *
     * @param games The list of games to display.
     */
    public GameAdapter(List<Game> games) {
        this.games = games;
    }

    /**
     * Called when RecyclerView needs a new {@link GameViewHolder} of the given type
     * to represent
     * an item.
     *
     * @param parent   The ViewGroup into which the new View will be added after it
     *                 is bound to
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
     * Called by RecyclerView to display the data at the specified position. This
     * method should
     * update the contents of the {@link GameViewHolder#itemView} to reflect the
     * item at the given
     * position.
     *
     * @param holder   The GameViewHolder which should be updated to represent the
     *                 contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull GameViewHolder holder, int position) {
        holder.bind(games.get(position));
    }

    /**
     * Returns the total number of games in the data set held by the adapter.
     *
     * @return The total number of games in this adapter.
     */
    @Override
    public int getItemCount() {
        return games.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(Game game);
    }

    /**
     * A ViewHolder that describes an item view and metadata about its place within
     * the RecyclerView.
     */
    public class GameViewHolder extends RecyclerView.ViewHolder {

        private final ItemGameBinding binding;

        /**
         * Constructs a new GameViewHolder.
         *
         * @param binding The data binding for the item view.
         */
        public GameViewHolder(ItemGameBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(games.get(position));
                }
            });
        }

        /**
         * Binds a Game to the item view. Loads the title, and image for the game from
         * the
         * Game object.
         *
         * @param game The Game to bind.
         */
        public void bind(Game game) {
            binding.setGameTitle(game.getTitle());
            binding.gameImage.setImageResource(game.getImageResId());

            // Programmatic background for icons
            android.content.Context context = binding.getRoot().getContext();
            android.graphics.drawable.GradientDrawable background = new android.graphics.drawable.GradientDrawable();
            background.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
            background.setCornerRadius(16 * context.getResources().getDisplayMetrics().density); // 16dp radius

            int padding = (int) (16 * context.getResources().getDisplayMetrics().density); // 16dp padding

            int color = 0;
            boolean applyStyle = true;

            switch (game.getGameType()) {
                case SUDOKU:
                    color = androidx.core.content.ContextCompat.getColor(context,
                            com.appsters.unlimitedgames.R.color.teal_700);
                    break;
                case SOCCERSEPARATION:
                    color = androidx.core.content.ContextCompat.getColor(context,
                            com.appsters.unlimitedgames.R.color.green);
                    break;
                case WHACK_A_MOLE:
                    color = androidx.core.content.ContextCompat.getColor(context,
                            com.appsters.unlimitedgames.R.color.mole_red);
                    break;
                case MAZE:
                    color = androidx.core.content.ContextCompat.getColor(context,
                            com.appsters.unlimitedgames.R.color.purple_500);
                    break;
                case GAME2048:
                default:
                    applyStyle = false; // 2048 already has a background in the image
                    break;
            }

            if (applyStyle) {
                background.setColor(color);
                binding.gameImage.setBackground(background);
                binding.gameImage.setPadding(padding, padding, padding, padding);

                // Debugging: Don't tint the soccer ball for now to see what the image actually
                // looks like
                if (game.getGameType() != com.appsters.unlimitedgames.app.util.GameType.SOCCERSEPARATION) {
                    binding.gameImage.setColorFilter(android.graphics.Color.WHITE);
                } else {
                    binding.gameImage.clearColorFilter();
                }
            } else {
                binding.gameImage.setBackground(null);
                binding.gameImage.setPadding(0, 0, 0, 0);
                binding.gameImage.clearColorFilter();
            }

            binding.executePendingBindings();
        }
    }
}
