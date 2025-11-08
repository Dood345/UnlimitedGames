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
import com.appsters.unlimitedgames.app.ui.auth.AuthState;


/**
 * The main activity of the application.
 * This activity hosts the navigation for the entire application and observes the authentication
 * state to determine whether to show the main application graph or the authentication graph.
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private AuthViewModel authViewModel;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;

    /**
     * Called when the activity is first created.
     * This method initializes the activity, sets up the view model and navigation, and observes
     * the authentication state. Upon logging out auth state is reset, taking us back to auth flow.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();

            // Define top-level destinations
            appBarConfiguration = new AppBarConfiguration.Builder(R.id.homeFragment, R.id.friendsFragment, R.id.leaderboardFragment, R.id.profileFragment)
                    .build();

            // Set up the toolbar with NavController
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

            authViewModel.getAuthState().observe(this, authState -> {
                if (authState != null) {
                    handleAuthState(authState);
                }
            });
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.mainContainer, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return windowInsets;
        });
    }

    /**
     * Handles changes in authentication state.
     * Updates the navigation graph and UI visibility based on the current auth state.
     *
     * @param authState The current authentication state
     */
    private void handleAuthState(AuthState authState) {
        switch (authState) {
            case LOADING:
                // Show loading indicator, hide bottom nav and toolbar
                binding.bottomNav.setVisibility(View.GONE);
                getSupportActionBar().hide();
                break;

            case AUTHENTICATED:
                // User is logged in - show main app
                if (navController.getGraph().getId() != R.id.nav_main) {
                    navController.setGraph(R.navigation.nav_main);
                }
                binding.bottomNav.setVisibility(View.VISIBLE);
                getSupportActionBar().show();
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
                break;

            case UNAUTHENTICATED:
                // User is not logged in - show auth flow
                if (navController.getGraph().getId() != R.id.nav_auth) {
                    navController.setGraph(R.navigation.nav_auth);
                }
                binding.bottomNav.setVisibility(View.GONE);
                getSupportActionBar().hide();
                break;

            case ERROR:
                // After showing error, revert to unauthenticated state
                // The auth fragments will handle showing the error to the user
                break;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
