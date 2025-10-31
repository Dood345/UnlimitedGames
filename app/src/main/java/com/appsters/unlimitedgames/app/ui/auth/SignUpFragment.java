package com.appsters.unlimitedgames.app.ui.auth;

import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.appsters.unlimitedgames.R;
import com.appsters.unlimitedgames.databinding.FragmentSignupBinding;

/**
 * A simple {@link Fragment} subclass that handles user registration.
 * It allows new users to create an account by providing a username, email, and password.
 */
public class SignUpFragment extends Fragment {

    /** The binding for the sign-up fragment layout. */
    private FragmentSignupBinding binding;
    /** The view model for authentication. */
    private AuthViewModel viewModel;
    /** The navigation controller for navigating between fragments. */
    private NavController navController;

    /**
     * Called when the fragment is created.
     * It initializes the {@link AuthViewModel}.
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
    }

    /**
     * Inflates the layout for this fragment.
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
        binding = FragmentSignupBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Called when the fragment's view has been created.
     * It sets up the click listeners and observers for the fragment.
     * @param view The created view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        binding.buttonSignUp.setOnClickListener(v -> {
            String username = binding.editTextUsername.getText().toString().trim();
            String email = binding.editTextEmail.getText().toString().trim();
            String password = binding.editTextPassword.getText().toString().trim();

            // Validate username
            if (username.isEmpty()) {
                binding.editTextUsername.setError("Username is required");
                return;
            }

            if (username.length() < 3) {
                binding.editTextUsername.setError("Username must be at least 3 characters");
                return;
            }

            // Validate email
            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.editTextEmail.setError("Enter a valid email");
                return;
            }

            // Validate password
            if (password.isEmpty() || password.length() < 6) {
                binding.editTextPassword.setError("Password must be at least 6 characters");
                return;
            }

            viewModel.signUp(username, email, password);
        });

        binding.textViewLogin.setOnClickListener(v -> {
            navController.navigate(R.id.action_signUpFragment_to_loginFragment);
        });

        viewModel.getAuthState().observe(getViewLifecycleOwner(), authState -> {
            switch (authState) {
                case LOADING:
                    binding.progressBar.setVisibility(View.VISIBLE);
                    binding.buttonSignUp.setEnabled(false);
                    break;
                case AUTHENTICATED:
                    // Navigation is handled by MainActivity
                    binding.progressBar.setVisibility(View.GONE);
                    binding.buttonSignUp.setEnabled(true);
                    break;
                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    binding.buttonSignUp.setEnabled(true);
                    Toast.makeText(requireContext(), viewModel.getErrorMessage(), Toast.LENGTH_SHORT).show();
                    break;
                case UNAUTHENTICATED:
                    binding.progressBar.setVisibility(View.GONE);
                    binding.buttonSignUp.setEnabled(true);
                    break;
            }
        });
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
}
