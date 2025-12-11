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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.appsters.unlimitedgames.R;
import com.appsters.unlimitedgames.app.data.model.User;
import com.appsters.unlimitedgames.databinding.FragmentProfileBinding;
import com.appsters.unlimitedgames.app.ui.auth.AuthViewModel;
import com.appsters.unlimitedgames.app.ui.auth.AuthViewModelFactory;
import com.appsters.unlimitedgames.app.util.ImageHelper;
import com.appsters.unlimitedgames.app.util.Privacy;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ProfileViewModel viewModel;
    private AuthViewModel authViewModel;
    private NavController navController;

    private boolean isInitialLoad = true;

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    openImagePicker();
                } else {
                    Toast.makeText(requireContext(), "Permission denied to read storage", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        handleImageSelection(imageUri);
                    }
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);
        viewModel.resetFlags();
        AuthViewModelFactory factory = new AuthViewModelFactory(requireActivity().getApplication());
        authViewModel = new ViewModelProvider(requireActivity(), factory).get(AuthViewModel.class);
        navController = Navigation.findNavController(view);
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(getViewLifecycleOwner());

        setupPrivacySpinner();
        setupRecyclerView();
        setupObservers();
        setupClickListeners();

        viewModel.loadCurrentUser(requireContext());
    }

    private void setupPrivacySpinner() {
        ArrayAdapter<Privacy> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                Privacy.values());
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

    private void setupRecyclerView() {
        binding.rvHighScores.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

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

        viewModel.getUserScores().observe(getViewLifecycleOwner(), scores -> {
            if (scores != null) {
                binding.rvHighScores.setAdapter(new ProfileScoreAdapter(scores));
            }
        });

        viewModel.getAverageRank().observe(getViewLifecycleOwner(), avgRank -> {
            if (avgRank != null) {
                binding.tvAverageRank.setText(String.format("Average Rank: %.2f", avgRank));
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
                authViewModel.logout();
            }
        });

        viewModel.getImageUploadSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                Toast.makeText(requireContext(), "Profile picture updated!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupClickListeners() {
        binding.btnEditProfile.setOnClickListener(v -> {
            navController.navigate(R.id.action_profileFragment_to_editProfileFragment);
        });

        binding.btnEditProfilePicture.setOnClickListener(v -> {
            requestStoragePermission();
        });

        binding.btnLogout.setOnClickListener(v -> {
            viewModel.logout();
        });
    }

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

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void handleImageSelection(Uri imageUri) {
        binding.progressBar.setVisibility(View.VISIBLE);

        new Thread(() -> {
            String base64Image = ImageHelper.compressImageToBase64(requireContext(), imageUri);

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

    private void loadProfileImage(User user) {
        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
            Bitmap bitmap = ImageHelper.decodeBase64ToBitmap(user.getProfileImageUrl());
            if (bitmap != null) {
                binding.profileImage.setImageBitmap(bitmap);
            } else {
                showInitialsAvatar(user);
            }
        } else {
            showInitialsAvatar(user);
        }
    }

    private void showInitialsAvatar(User user) {
        String initials = ImageHelper.getInitials(user.getUsername());
        String color = user.getProfileColor() != null ? user.getProfileColor() : "#4ECDC4";

        int size = (int) (120 * getResources().getDisplayMetrics().density);
        binding.profileImage.setImageBitmap(
                ImageHelper.createInitialsAvatar(initials, color, size));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
