package com.appsters.unlimitedgames.app.ui.profile;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.appsters.unlimitedgames.R;
import com.appsters.unlimitedgames.app.ui.auth.AuthState;
import com.appsters.unlimitedgames.app.ui.auth.AuthViewModel;
import com.appsters.unlimitedgames.databinding.FragmentEditProfileBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class EditProfileFragment extends Fragment {

    private FragmentEditProfileBinding binding;
    private ProfileViewModel viewModel;
    private AuthViewModel authViewModel;
    private NavController navController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);
        viewModel.resetFlags();                           // Reset edit flags first thing
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        navController = Navigation.findNavController(view);

        binding.setLifecycleOwner(getViewLifecycleOwner());

        setupObservers();
        setupClickListeners();
    }

    private void setupObservers() {
        viewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                binding.etUsername.setText(user.getUsername());
                binding.etEmail.setText(user.getEmail());
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getUpdateSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success) {
                Toast.makeText(getContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                navController.popBackStack();
            }
        });

        viewModel.getDeleteSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success) {
                Toast.makeText(getContext(), "Account deleted successfully.", Toast.LENGTH_SHORT).show();
                authViewModel.logout();
                viewModel.resetFlags();     //resetting after userless logout to prevent errors on login
            }
        });
    }

    private void setupClickListeners() {
        binding.btnSaveChanges.setOnClickListener(v -> saveChanges());
        binding.btnDeleteAccount.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    private void saveChanges() {
        String username = binding.etUsername.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String currentPassword = binding.etCurrentPassword.getText().toString();
        String newPassword = binding.etNewPassword.getText().toString();
        String confirmPassword = binding.etConfirmPassword.getText().toString();

        // ViewModel handles all validation and sequencing
        viewModel.updateProfile(username, email, currentPassword, newPassword, confirmPassword);
    }

    private void showDeleteConfirmationDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? This action is irreversible.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> showPasswordPromptForDelete())
                .show();
    }

    private void showPasswordPromptForDelete() {
        final com.google.android.material.textfield.TextInputEditText input = new com.google.android.material.textfield.TextInputEditText(requireContext());
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setHint("Enter your password");

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Confirm Deletion")
                .setView(input)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Confirm", (dialog, which) -> {
                    String password = input.getText().toString();
                    if (!password.isEmpty()) {
                        viewModel.deleteAccount(password);
                    } else {
                        Toast.makeText(getContext(), "Password is required.", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
