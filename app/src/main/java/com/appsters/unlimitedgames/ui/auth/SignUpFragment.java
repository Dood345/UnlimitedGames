package com.appsters.unlimitedgames.ui.auth;

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

public class SignUpFragment extends Fragment {

    private FragmentSignupBinding binding;
    private AuthViewModel viewModel;
    private NavController navController;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSignupBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}