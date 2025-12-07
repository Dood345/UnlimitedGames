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
import com.appsters.unlimitedgames.databinding.FragmentLoginBinding;
import com.appsters.unlimitedgames.app.ui.auth.AuthViewModelFactory;

/**
 * A simple {@link Fragment} subclass that handles user login.
 * It allows users to enter their email and password to log in to the app.
 * It also provides an option to navigate to the sign-up screen.
 */
public class LoginFragment extends Fragment {

    /** The binding for the login fragment layout. */
    private FragmentLoginBinding binding;
    /** The view model for authentication. */
    private AuthViewModel viewModel;
    /** The navigation controller for navigating between fragments. */
    private NavController navController;

    /**
     * Called when the fragment is created.
     * It initializes the {@link AuthViewModel}.
     * 
     * @param savedInstanceState If the fragment is being re-created from
     *                           a previous saved state, this is the state.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AuthViewModelFactory factory = new AuthViewModelFactory(requireActivity().getApplication());
        viewModel = new ViewModelProvider(requireActivity(), factory).get(AuthViewModel.class);
    }

    /**
     * Inflates the layout for this fragment.
     * 
     * @param inflater           The LayoutInflater object that can be used to
     *                           inflate
     *                           any views in the fragment,
     * @param container          If non-null, this is the parent view that the
     *                           fragment's
     *                           UI should be attached to. The fragment should not
     *                           add the view itself,
     *                           but this can be used to generate the LayoutParams
     *                           of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     * @return The root view of the fragment.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Called when the fragment's view has been created.
     * It sets up the click listeners and observers for the fragment.
     * 
     * @param view               The created view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        binding.buttonLogin.setOnClickListener(v -> {
            String email = binding.editTextEmail.getText().toString().trim();
            String password = binding.editTextPassword.getText().toString().trim();

            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.editTextEmail.setError("Enter a valid email");
                return;
            }

            if (password.isEmpty()) {
                binding.editTextPassword.setError("Password is required");
                return;
            }

            viewModel.signIn(email, password);
        });

        binding.textViewSignUp.setOnClickListener(v -> {
            navController.navigate(R.id.action_loginFragment_to_signUpFragment);
        });

        viewModel.getAuthState().observe(getViewLifecycleOwner(), authState -> {
            switch (authState) {
                case LOADING:
                    binding.progressBar.setVisibility(View.VISIBLE);
                    break;
                case AUTHENTICATED:
                    // Navigation is handled by MainActivity
                    binding.progressBar.setVisibility(View.GONE);
                    break;
                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), viewModel.getErrorMessage(), Toast.LENGTH_SHORT).show();
                    break;
                case UNAUTHENTICATED:
                    binding.progressBar.setVisibility(View.GONE);
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
