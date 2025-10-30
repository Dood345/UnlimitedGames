package com.appsters.unlimitedgames;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.appsters.unlimitedgames.databinding.ActivityMainBinding;
import com.appsters.unlimitedgames.ui.auth.AuthViewModel;

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
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        authViewModel.getAuthState().observe(this, authState -> {
            switch (authState) {
                case AUTHENTICATED:
                    navController.setGraph(R.navigation.nav_main);
                    binding.bottomNav.setVisibility(View.VISIBLE);
                    NavigationUI.setupWithNavController(binding.bottomNav, navController);
                    break;
                case UNAUTHENTICATED:
                    navController.setGraph(R.navigation.nav_auth);
                    binding.bottomNav.setVisibility(View.GONE);
                    break;
            }
        });
    }
}
