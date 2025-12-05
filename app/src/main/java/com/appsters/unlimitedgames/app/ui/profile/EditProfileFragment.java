package com.appsters.unlimitedgames.app.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.appsters.unlimitedgames.R;
import com.appsters.unlimitedgames.app.ui.auth.AuthViewModel;
import com.appsters.unlimitedgames.databinding.FragmentEditProfileBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * A fragment that allows users to edit their profile information,
 * including username, email, and password. It also provides the functionality
 * for users to delete their account after confirming their password.
 */
public class EditProfileFragment extends Fragment {

    /** Binding for the fragment's layout. */
    private FragmentEditProfileBinding binding;
    /** ViewModel for profile-related data and actions. */
    private ProfileViewModel viewModel;
    /** ViewModel for authentication-related actions. */
    private AuthViewModel authViewModel;
    /** Navigation controller for handling screen transitions. */
    private NavController navController;

    /**
     * Inflates the fragment's layout.
     * 
     * @param inflater           The LayoutInflater object that can be used to
     *                           inflate any views in the fragment.
     * @param container          If non-null, this is the parent view that the
     *                           fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state.
     * @return The root view for the fragment's UI.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Initializes the ViewModels, NavController, and sets up UI observers and click
     * listeners
     * after the view has been created.
     * 
     * @param view               The view returned by
     *                           {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);
        viewModel.resetFlags(); // Reset edit flags first thing
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        navController = Navigation.findNavController(view);

        binding.setLifecycleOwner(getViewLifecycleOwner());

        setupObservers();
        setupClickListeners();
    }

    /**
     * Sets up observers on LiveData from the ViewModels to update the UI in
     * response to data changes,
     * such as user information, loading states, and success or error messages.
     */
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
                viewModel.resetFlags(); // resetting after userless logout to prevent errors on login
            }
        });
    }

    /**
     * Sets up click listeners for the interactive UI elements in the fragment, such
     * as the
     * "Save Changes" and "Delete Account" buttons.
     */
    private void setupClickListeners() {
        binding.btnSaveChanges.setOnClickListener(v -> saveChanges());
        binding.btnDeleteAccount.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    /**
     * Gathers the input from the various text fields and calls the ViewModel
     * to perform the profile update operation.
     */
    private void saveChanges() {
        String username = binding.etUsername.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String currentPassword = binding.etCurrentPassword.getText().toString();
        String newPassword = binding.etNewPassword.getText().toString();
        String confirmPassword = binding.etConfirmPassword.getText().toString();

        if (username.length() < 3) {
            Toast.makeText(getContext(), "Username must be at least 3 characters long.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if email is being updated but current password is not provided
        com.appsters.unlimitedgames.app.data.model.User currentUser = viewModel.getCurrentUser().getValue();
        if (currentUser != null && !email.equals(currentUser.getEmail()) && currentPassword.isEmpty()) {
            showPasswordPromptForUpdate(username, email, newPassword, confirmPassword);
            return;
        }

        // ViewModel handles all validation and sequencing
        viewModel.updateProfile(username, email, currentPassword, newPassword, confirmPassword);
    }

    /**
     * Displays a dialog prompting the user to enter their password to confirm
     * profile updates (like email change).
     */
    private void showPasswordPromptForUpdate(String username, String email, String newPassword,
            String confirmPassword) {
        FrameLayout container = new FrameLayout(requireContext());
        final com.google.android.material.textfield.TextInputEditText input = new com.google.android.material.textfield.TextInputEditText(
                requireContext());
        input.setInputType(
                android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setHint("Enter password");

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        int margin = (int) (16 * getResources().getDisplayMetrics().density); // 16dp margin
        params.setMargins(margin, 0, margin, 0);
        input.setLayoutParams(params);

        container.addView(input);

        new MaterialAlertDialogBuilder(requireContext(), R.style.DeleteDialogueTheme)
                .setTitle("Confirm Changes")
                .setMessage("Please enter your current password to update your email.")
                .setView(container)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Confirm", (dialog, which) -> {
                    String password = input.getText().toString();
                    if (!password.isEmpty()) {
                        viewModel.updateProfile(username, email, password, newPassword, confirmPassword);
                    } else {
                        Toast.makeText(getContext(), "Password is required.", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    /**
     * Displays an alert dialog to confirm whether the user wants to proceed with
     * deleting their account.
     * If confirmed, it proceeds to show the password prompt.
     */
    private void showDeleteConfirmationDialog() {
        new MaterialAlertDialogBuilder(requireContext(), R.style.DeleteDialogueTheme)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? This action is irreversible.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> showPasswordPromptForDelete())
                .show();
    }

    /**
     * Displays a dialog prompting the user to enter their password to confirm
     * account deletion.
     * The input field is placed in a FrameLayout to add horizontal margins for
     * better visual presentation.
     */
    private void showPasswordPromptForDelete() {
        FrameLayout container = new FrameLayout(requireContext());
        final com.google.android.material.textfield.TextInputEditText input = new com.google.android.material.textfield.TextInputEditText(
                requireContext());
        input.setInputType(
                android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setHint("Enter your password");

        // gemini added a frame layout to limit the size of the password entry text
        // field
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        int margin = (int) (16 * getResources().getDisplayMetrics().density); // 16dp margin
        params.setMargins(margin, 0, margin, 0);
        input.setLayoutParams(params);

        container.addView(input);

        new MaterialAlertDialogBuilder(requireContext(), R.style.DeleteDialogueTheme)
                .setTitle("Confirm Deletion")
                .setView(container)
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

    /**
     * Cleans up the binding when the fragment's view is destroyed to prevent memory
     * leaks.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
