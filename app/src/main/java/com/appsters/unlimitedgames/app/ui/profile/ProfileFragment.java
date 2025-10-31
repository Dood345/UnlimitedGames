package com.appsters.unlimitedgames.app.ui.profile;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.appsters.unlimitedgames.databinding.FragmentProfileBinding;
import com.appsters.unlimitedgames.app.ui.auth.AuthViewModel;
import com.appsters.unlimitedgames.app.util.ImageHelper;
import com.appsters.unlimitedgames.app.util.Privacy;

/**
 * A simple {@link Fragment} subclass that displays the user's profile.
 * Allows users to view their profile information, change their profile picture,
 * update their privacy settings, and log out.
 */
public class ProfileFragment extends Fragment {

    /** The binding for the profile fragment layout. */
    private FragmentProfileBinding binding;
    /** The view model for the profile fragment. */
    private ProfileViewModel viewModel;
    /** The view model for authentication. */
    private AuthViewModel authViewModel;

    /** A flag to indicate if it's the initial load of the fragment. */
    private boolean isInitialLoad = true;

    /**
     * An {@link ActivityResultLauncher} for requesting the storage permission.
     * If the permission is granted, it opens the image picker.
     * If the permission is denied, it shows a toast message.
     */
    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    openImagePicker();
                } else {
                    Toast.makeText(requireContext(), "Permission denied to read storage", Toast.LENGTH_SHORT).show();
                }
            });

    /**
     * An {@link ActivityResultLauncher} for picking an image from the gallery.
     * When an image is selected, it handles the image selection.
     */
    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        handleImageSelection(imageUri);
                    }
                }
            });

    /**
     * Inflates the layout for this fragment.
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     * @return The root view of the fragment.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Called when the fragment's view has been created.
     * @param view The created view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
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

    /**
     * Sets up the privacy spinner with the available privacy options.
     * When an item is selected, it updates the user's privacy settings.
     */
    private void setupPrivacySpinner() {
        ArrayAdapter<Privacy> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                Privacy.values()
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerPrivacy.setAdapter(adapter);

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
            }
        });
    }

    /**
     * Sets up observers for the view model's live data.
     */
    private void setupObservers() {
        viewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                binding.tvUsername.setText(user.getUsername());
                binding.tvEmail.setText(user.getEmail());

                binding.spinnerPrivacy.setSelection(user.getPrivacy().ordinal());
                isInitialLoad = false;

                loadProfileImage(user);
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

        viewModel.getLogoutComplete().observe(getViewLifecycleOwner(), logoutComplete -> {
            if (logoutComplete) {
                Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
                authViewModel.signOut();
            }
        });

        viewModel.getImageUploadSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                Toast.makeText(requireContext(), "Profile picture updated!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Sets up click listeners for the buttons in the fragment.
     */
    private void setupClickListeners() {
        binding.btnEditProfile.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Edit Profile - Coming Soon", Toast.LENGTH_SHORT).show();
        });

        binding.btnEditProfilePicture.setOnClickListener(v -> {
            requestStoragePermission();
        });

        binding.btnLogout.setOnClickListener(v -> {
            viewModel.logout();
        });
    }

    /**
     * Requests the storage permission.
     * If the permission is already granted, it opens the image picker.
     * Otherwise, it launches the permission request.
     */
    private void requestStoragePermission() {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            openImagePicker();
        } else {
            requestPermissionLauncher.launch(permission);
        }
    }

    /**
     * Opens the image picker to allow the user to select an image.
     */
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    /**
     * Handles the selection of an image from the image picker.
     * It compresses the image to a Base64 string and updates the user's profile picture.
     * @param imageUri The URI of the selected image.
     */
    private void handleImageSelection(Uri imageUri) {
        // Show loading
        binding.progressBar.setVisibility(View.VISIBLE);

        // Compress image in background thread
        new Thread(() -> {
            String base64Image = ImageHelper.compressImageToBase64(requireContext(), imageUri);

            // Update UI on main thread
            requireActivity().runOnUiThread(() -> {
                binding.progressBar.setVisibility(View.GONE);

                if (base64Image != null) {
                    long imageSize = ImageHelper.getBase64ImageSize(base64Image);
                    if (imageSize < 5 * 1024 * 1024) { // Check if under 5MB
                        viewModel.updateProfilePicture(base64Image);
                    } else {
                        Toast.makeText(requireContext(),
                                "Image is too large. Please choose a smaller image.",
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(requireContext(),
                            "Failed to process image. Please try another image.",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    /**
     * Loads the user's profile image.
     * If the user has a profile image URL, it decodes the Base64 string and displays the image.
     * Otherwise, it shows an avatar with the user's initials.
     * @param user The user whose profile image to load.
     */
    private void loadProfileImage(com.appsters.unlimitedgames.app.data.model.User user) {
        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
            // Load Base64 image
            Bitmap bitmap = ImageHelper.decodeBase64ToBitmap(user.getProfileImageUrl());
            if (bitmap != null) {
                binding.profileImage.setImageBitmap(bitmap);
            } else {
                // Fallback to initials if decode fails
                showInitialsAvatar(user);
            }
        } else {
            // Show initials avatar
            showInitialsAvatar(user);
        }
    }

    /**
     * Shows an avatar with the user's initials.
     * @param user The user whose initials to display.
     */
    private void showInitialsAvatar(com.appsters.unlimitedgames.app.data.model.User user) {
        String initials = ImageHelper.getInitials(user.getUsername());
        String color = user.getProfileColor() != null ? user.getProfileColor() : "#4ECDC4";

        int size = (int) (120 * getResources().getDisplayMetrics().density);
        binding.profileImage.setImageBitmap(
                ImageHelper.createInitialsAvatar(initials, color, size)
        );
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
