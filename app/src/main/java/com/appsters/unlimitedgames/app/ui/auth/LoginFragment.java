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

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
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
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
