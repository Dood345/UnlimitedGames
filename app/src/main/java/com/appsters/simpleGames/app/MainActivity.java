package com.appsters.simpleGames.app;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.appsters.simpleGames.R;
import com.appsters.simpleGames.databinding.ActivityMainBinding;
import com.appsters.simpleGames.app.ui.auth.AuthViewModel;
import com.appsters.simpleGames.app.ui.auth.AuthViewModelFactory;
import com.appsters.simpleGames.app.ui.auth.AuthState;

/**
 * The main activity of the application, serving as the primary entry point and
 * navigation host.
 * This activity manages the overall application layout, including the toolbar
 * and bottom navigation.
 * It observes the authentication state to dynamically switch between the main
 * application content
 * ({@code nav_main}) and the authentication flow ({@code nav_auth}).
 */
import com.appsters.simpleGames.app.ui.friends.FriendViewModel;
import com.google.android.material.badge.BadgeDrawable;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private AuthViewModel authViewModel;
    private FriendViewModel friendViewModel; // ✅
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        AuthViewModelFactory factory = new AuthViewModelFactory(getApplication());
        authViewModel = new ViewModelProvider(this, factory).get(AuthViewModel.class);
        friendViewModel = new ViewModelProvider(this).get(FriendViewModel.class); // ✅

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();

            appBarConfiguration = new AppBarConfiguration.Builder(R.id.homeFragment, R.id.friendsFragment,
                    R.id.leaderboardFragment, R.id.profileFragment)
                    .build();

            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

            authViewModel.getAuthState().observe(this, authState -> {
                if (authState != null) {
                    handleAuthState(authState);
                }
            });

            // ✅ Observe Request Count
            friendViewModel.getOngoingRequestCount().observe(this, count -> {
                BadgeDrawable badge = binding.bottomNav.getOrCreateBadge(R.id.friendsFragment);
                if (count > 0) {
                    badge.setVisible(true);
                    badge.setNumber(count);
                } else {
                    badge.setVisible(false);
                }
            });
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.mainContainer, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return windowInsets;
        });
    }

    private void handleAuthState(AuthState authState) {
        switch (authState) {
            case LOADING:
                binding.bottomNav.setVisibility(View.GONE);
                if (getSupportActionBar() != null)
                    getSupportActionBar().hide();
                break;

            case AUTHENTICATED:
                if (navController.getGraph().getId() != R.id.nav_main) {
                    navController.setGraph(R.navigation.nav_main);
                }
                binding.bottomNav.setVisibility(View.VISIBLE);
                if (getSupportActionBar() != null)
                    getSupportActionBar().show();
                setupBottomNavigation();

                binding.bottomNav.setSelectedItemId(R.id.homeFragment);

                // ✅ Start listening
                com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance()
                        .getCurrentUser();
                if (user != null) {
                    friendViewModel.listenToRequestCount(user.getUid());
                    syncHighScores(user.getUid());
                }
                break;

            case UNAUTHENTICATED:
                if (navController.getGraph().getId() != R.id.nav_auth) {
                    navController.setGraph(R.navigation.nav_auth);
                }
                binding.bottomNav.setVisibility(View.GONE);
                if (getSupportActionBar() != null)
                    getSupportActionBar().hide();
                break;

            case ERROR:
                break;
        }
    }

    private void syncHighScores(String userId) {
        com.appsters.simpleGames.app.data.repository.LeaderboardRepository leaderboardRepository = new com.appsters.simpleGames.app.data.repository.LeaderboardRepository(
                getApplication());
        leaderboardRepository.getUserScores(userId, (isSuccess, scores, error) -> {
            if (isSuccess && scores != null) {
                // Fetch all registered games from the registry
                java.util.List<com.appsters.simpleGames.games.interfaces.IGame> games = com.appsters.simpleGames.app.managers.GameRegistry
                        .getRegisteredGames(getApplication());

                // Delegate sync logic to each game
                for (com.appsters.simpleGames.games.interfaces.IGame game : games) {
                    game.syncHighScore(getApplicationContext(), scores);
                }
            }
        });
    }

    /**
     * Sets up the listener for the bottom navigation view. This custom listener
     * handles clicks on
     * navigation items, ensuring that selecting an item either navigates to the
     * destination or,
     * if already in that destination's back stack, pops back to the root of that
     * tab.
     */
    private void setupBottomNavigation() {
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            int destinationId = -1;

            if (itemId == R.id.profileFragment) {
                destinationId = R.id.profileFragment;
            } else if (itemId == R.id.homeFragment) {
                destinationId = R.id.homeFragment;
            } else if (itemId == R.id.leaderboardFragment) {
                destinationId = R.id.leaderboardFragment;
            } else if (itemId == R.id.friendsFragment) {
                destinationId = R.id.friendsFragment;
            }

            if (destinationId != -1) {
                // If not already at the destination, try to pop back to it or navigate
                if (navController.getCurrentDestination() == null ||
                        navController.getCurrentDestination().getId() != destinationId) {

                    boolean popped = navController.popBackStack(destinationId, false);

                    if (!popped) {
                        navController.navigate(destinationId);
                    }
                }
                return true;
            }

            return false;
        });
    }

    /**
     * Handles the Up button navigation. This is delegated to the NavController to
     * ensure proper
     * hierarchical navigation within the app.
     *
     * @return {@code true} if navigation was successful, {@code false} otherwise.
     */
    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }

    /**
     * Called when the activity is being destroyed. This is the final call that the
     * activity
     * receives. It cleans up resources by setting the view binding to null to
     * prevent memory leaks.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
