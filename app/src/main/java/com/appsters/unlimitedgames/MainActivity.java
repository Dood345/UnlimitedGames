package com.appsters.unlimitedgames;

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
import androidx.navigation.ui.NavigationUI;

import com.appsters.unlimitedgames.databinding.ActivityMainBinding;
import com.appsters.unlimitedgames.ui.auth.AuthViewModel;
import com.appsters.unlimitedgames.ui.auth.AuthState;


/**
 * The main activity of the application.
 * This activity hosts the navigation for the entire application and observes the authentication
 * state to determine whether to show the main application graph or the authentication graph.
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private AuthViewModel authViewModel;
    private NavController navController;

    /**
     * Called when the activity is first created.
     * This method initializes the activity, sets up the view model and navigation, and observes
     * the authentication state.
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

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();

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
                // Show loading indicator, hide bottom nav
                binding.bottomNav.setVisibility(View.GONE);
                // You could add a loading overlay here if desired
                break;

            case AUTHENTICATED:
                // User is logged in - show main app
                if (navController.getGraph().getId() != R.id.nav_main) {
                    navController.setGraph(R.navigation.nav_main);
                }
                binding.bottomNav.setVisibility(View.VISIBLE);
                NavigationUI.setupWithNavController(binding.bottomNav, navController);
                break;

            case UNAUTHENTICATED:
                // User is not logged in - show auth flow
                if (navController.getGraph().getId() != R.id.nav_auth) {
                    navController.setGraph(R.navigation.nav_auth);
                }
                binding.bottomNav.setVisibility(View.GONE);
                break;

            case ERROR:
                // Show error message but stay on current screen
//                String errorMsg = authViewModel.getErrorMessage();
//                if (errorMsg != null) {
//                    Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
//                }
                // After showing error, revert to unauthenticated state
                // The auth fragments will handle showing the error to the user
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}