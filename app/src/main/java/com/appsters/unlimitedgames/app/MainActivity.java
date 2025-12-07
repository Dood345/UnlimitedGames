package com.appsters.unlimitedgames.app;

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

import com.appsters.unlimitedgames.R;
import com.appsters.unlimitedgames.databinding.ActivityMainBinding;
import com.appsters.unlimitedgames.app.ui.auth.AuthViewModel;
import com.appsters.unlimitedgames.app.ui.auth.AuthViewModelFactory;
import com.appsters.unlimitedgames.app.ui.auth.AuthState;

/**
 * The main activity of the application, serving as the primary entry point and
 * navigation host.
 * This activity manages the overall application layout, including the toolbar
 * and bottom navigation.
 * It observes the authentication state to dynamically switch between the main
 * application content
 * ({@code nav_main}) and the authentication flow ({@code nav_auth}).
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private AuthViewModel authViewModel;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;

    /**
     * Called when the activity is first created. This method initializes the view
     * binding, sets up
     * the action bar, and configures the navigation controller with top-level
     * destinations.
     * It also sets up an observer to monitor the authentication state and adjust
     * the UI accordingly.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle
     *                           contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.
     *                           Otherwise, it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        AuthViewModelFactory factory = new AuthViewModelFactory(getApplication());
        authViewModel = new ViewModelProvider(this, factory).get(AuthViewModel.class);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();

            // Define top-level destinations for the AppBarConfiguration
            appBarConfiguration = new AppBarConfiguration.Builder(R.id.homeFragment, R.id.friendsFragment,
                    R.id.leaderboardFragment, R.id.profileFragment)
                    .build();

            // Set up the toolbar with NavController and AppBarConfiguration
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

            // Observe authentication state to update the navigation graph
            authViewModel.getAuthState().observe(this, authState -> {
                if (authState != null) {
                    handleAuthState(authState);
                }
            });
        }

        // Apply window insets to handle system bars
        ViewCompat.setOnApplyWindowInsetsListener(binding.mainContainer, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return windowInsets;
        });
    }

    /**
     * Handles changes in the user's authentication state. It updates the navigation
     * graph and the
     * visibility of UI components like the toolbar and bottom navigation based on
     * whether the user
     * is authenticated, unauthenticated, or in a loading state.
     *
     * @param authState The current authentication state (e.g., AUTHENTICATED,
     *                  UNAUTHENTICATED).
     */
    private void handleAuthState(AuthState authState) {
        switch (authState) {
            case LOADING:
                // Hide UI elements during the loading process
                binding.bottomNav.setVisibility(View.GONE);
                if (getSupportActionBar() != null)
                    getSupportActionBar().hide();
                break;

            case AUTHENTICATED:
                // If the user is authenticated, show the main application graph and UI
                if (navController.getGraph().getId() != R.id.nav_main) {
                    navController.setGraph(R.navigation.nav_main);
                }
                binding.bottomNav.setVisibility(View.VISIBLE);
                if (getSupportActionBar() != null)
                    getSupportActionBar().show();
                setupBottomNavigation();

                // Ensure Home is selected and highlighted
                binding.bottomNav.setSelectedItemId(R.id.homeFragment);
                break;

            case UNAUTHENTICATED:
                // If the user is not authenticated, show the authentication flow
                if (navController.getGraph().getId() != R.id.nav_auth) {
                    navController.setGraph(R.navigation.nav_auth);
                }
                binding.bottomNav.setVisibility(View.GONE);
                if (getSupportActionBar() != null)
                    getSupportActionBar().hide();
                break;

            case ERROR:
                // The auth fragments handle showing the error to the user
                // Revert to unauthenticated state after an error
                break;
        }
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
