package com.appsters.unlimitedgames.app.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.appsters.unlimitedgames.app.data.GameDataSource;
import com.appsters.unlimitedgames.app.data.model.Game;
import com.appsters.unlimitedgames.databinding.FragmentHomeBinding;
import com.appsters.unlimitedgames.games.sudoku.SudokuActivity;
import com.appsters.unlimitedgames.games.whackamole.WhackAMoleTitleActivity;

/**
 * A {@link Fragment} that displays a list of games.
 * This fragment is the home screen of the app.
 */
public class HomeFragment extends Fragment implements GameAdapter.OnItemClickListener {

    /** The binding for the home fragment layout. */
    private FragmentHomeBinding binding;

    /**
     * Inflates the layout for this fragment.
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to. The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     * @return The root view of the fragment.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Called when the fragment's view has been created.
     *
     * @param view The created view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        GameAdapter adapter = new GameAdapter(GameDataSource.getGames());
        adapter.setOnItemClickListener(this);
        binding.gamesRecyclerView.setAdapter(adapter);
        binding.gamesRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
    }

    /**
     * Called when the view is destroyed.
     * It nullifies the binding to prevent memory leaks.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * Handles clicks on the game items in the RecyclerView.
     * It navigates to the corresponding game screen.
     *
     * @param game The game that was clicked.
     */
    @Override
    public void onItemClick(Game game) {
        if ("Whack-a-Mole".equals(game.getTitle())) {
            Intent intent = new Intent(getActivity(), WhackAMoleTitleActivity.class);
            startActivity(intent);
        } else if ("Sudoku".equals(game.getTitle())) {
            Intent intent = new Intent(getActivity(), SudokuActivity.class);
            startActivity(intent);
        } else {
            androidx.navigation.NavController navController = NavHostFragment.findNavController(this);
            if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() == com.appsters.unlimitedgames.R.id.homeFragment) {
                navController.navigate(game.getActionId());
            }
        }
    }
}
