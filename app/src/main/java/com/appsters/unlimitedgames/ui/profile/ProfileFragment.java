package com.appsters.unlimitedgames.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.appsters.unlimitedgames.R;
import com.appsters.unlimitedgames.databinding.FragmentProfileBinding;
import com.appsters.unlimitedgames.ui.auth.AuthViewModel;
import com.appsters.unlimitedgames.util.ImageHelper;
import com.appsters.unlimitedgames.util.Privacy;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ProfileViewModel viewModel;
    private AuthViewModel authViewModel;
    private boolean isInitialLoad = true; // Prevent privacy update on initial load

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(getViewLifecycleOwner());

        setupPrivacySpinner();
        setupObservers();
        setupClickListeners();

        viewModel.loadCurrentUser();
    }

    private void setupPrivacySpinner() {
        ArrayAdapter<Privacy> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                Privacy.values()
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerPrivacy.setAdapter(adapter);

        // Handle privacy changes
        binding.spinnerPrivacy.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!isInitialLoad) {
                    Privacy newPrivacy = Privacy.values()[position];
                    viewModel.updatePrivacy(newPrivacy);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void setupObservers() {
        viewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                binding.tvUsername.setText(user.getUsername());
                binding.tvEmail.setText(user.getEmail());

                // Set privacy spinner
                binding.spinnerPrivacy.setSelection(user.getPrivacy().ordinal());
                isInitialLoad = false; // Allow privacy updates after initial load

                // Load profile image
                loadProfileImage(user);

                // TODO: Load high scores into RecyclerView
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupClickListeners() {
        binding.btnEditProfile.setOnClickListener(v -> {
            // TODO: Navigate to edit profile
            // Navigation.findNavController(v).navigate(R.id.action_profile_to_editProfile);
            Toast.makeText(requireContext(), "Edit Profile - Coming Soon", Toast.LENGTH_SHORT).show();
        });

        binding.btnEditProfilePicture.setOnClickListener(v -> {
            // TODO: Open image picker
            Toast.makeText(requireContext(), "Upload Picture - Coming Soon", Toast.LENGTH_SHORT).show();
        });

        binding.btnLogout.setOnClickListener(v -> {
            authViewModel.signOut();
            Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadProfileImage(com.appsters.unlimitedgames.data.model.User user) {
        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
            // TODO: Load image from URL using Glide or Picasso
            // For now, show placeholder
            binding.profileImage.setImageResource(R.drawable.ic_profile);
        } else {
            // Generate initials avatar
            String initials = ImageHelper.getInitials(user.getUsername());
            String color = user.getProfileColor() != null ? user.getProfileColor() : "#4ECDC4";

            int size = (int) (120 * getResources().getDisplayMetrics().density);
            binding.profileImage.setImageBitmap(
                    ImageHelper.createInitialsAvatar(initials, color, size)
            );
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
