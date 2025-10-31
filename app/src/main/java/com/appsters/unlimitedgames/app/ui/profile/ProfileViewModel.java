package com.appsters.unlimitedgames.app.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.appsters.unlimitedgames.app.data.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.appsters.unlimitedgames.app.util.Privacy;

/**
 * ViewModel for the Profile screen.
 * Handles loading and updating user profile data, including privacy settings and profile picture.
 * Also handles user logout.
 */
public class ProfileViewModel extends ViewModel {

    /** Firebase authentication instance. */
    private final FirebaseAuth auth;
    /** Firebase Firestore instance. */
    private final FirebaseFirestore db;

    /** LiveData holding the current user's profile information. */
    private final MutableLiveData<User> currentUser = new MutableLiveData<>();
    /** LiveData indicating if a data loading operation is in progress. */
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    /** LiveData for displaying error messages. */
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    /** LiveData to signal when logout is complete. */
    private final MutableLiveData<Boolean> logoutComplete = new MutableLiveData<>(false);
    /** LiveData to signal if the image upload was successful. */
    private final MutableLiveData<Boolean> imageUploadSuccess = new MutableLiveData<>();

    /**
     * Constructor for ProfileViewModel.
     * Initializes FirebaseAuth and FirebaseFirestore instances.
     */
    public ProfileViewModel() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Returns the LiveData for the current user.
     * @return A LiveData object containing the current user's data.
     */
    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    /**
     * Returns the LiveData for the loading state.
     * @return A LiveData object indicating if data is being loaded.
     */
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    /**
     * Returns the LiveData for error messages.
     * @return A LiveData object containing any error messages.
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * Returns the LiveData for the logout completion status.
     * @return A LiveData object that is true when logout is complete.
     */
    public LiveData<Boolean> getLogoutComplete() {
        return logoutComplete;
    }

    /**
     * Returns the LiveData for the image upload success status.
     * @return A LiveData object that is true when image upload is successful.
     */
    public LiveData<Boolean> getImageUploadSuccess() {
        return imageUploadSuccess;
    }

    /**
     * Loads the current user's profile from Firestore.
     * It fetches the user data associated with the currently authenticated Firebase user.
     */
    public void loadCurrentUser() {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser == null) {
            errorMessage.setValue("No user logged in");
            return;
        }

        isLoading.setValue(true);
        db.collection("users")
                .document(firebaseUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    isLoading.setValue(false);
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        currentUser.setValue(user);
                    } else {
                        errorMessage.setValue("User profile not found");
                    }
                })
                .addOnFailureListener(e -> {
                    isLoading.setValue(false);
                    errorMessage.setValue("Failed to load profile: " + e.getMessage());
                });
    }

    /**
     * Updates the user's privacy setting in Firestore.
     * @param privacy The new privacy setting.
     */
    public void updatePrivacy(Privacy privacy) {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser == null) return;

        isLoading.setValue(true);
        db.collection("users")
                .document(firebaseUser.getUid())
                .update("privacy", privacy.name())
                .addOnSuccessListener(aVoid -> {
                    isLoading.setValue(false);
                    User user = currentUser.getValue();
                    if (user != null) {
                        user.setPrivacy(privacy);
                        currentUser.setValue(user);
                    }
                })
                .addOnFailureListener(e -> {
                    isLoading.setValue(false);
                    errorMessage.setValue("Failed to update privacy: " + e.getMessage());
                });
    }

    /**
     * Updates the user's profile picture in Firestore.
     * @param base64Image The new profile picture encoded as a Base64 string.
     */
    public void updateProfilePicture(String base64Image) {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser == null) return;

        isLoading.setValue(true);
        imageUploadSuccess.setValue(false); // Reset before upload
        db.collection("users")
                .document(firebaseUser.getUid())
                .update("profileImageUrl", base64Image)
                .addOnSuccessListener(aVoid -> {
                    isLoading.setValue(false);
                    User user = currentUser.getValue();
                    if (user != null) {
                        user.setProfileImageUrl(base64Image);
                        currentUser.setValue(user);
                        imageUploadSuccess.setValue(true);
                    }
                })
                .addOnFailureListener(e -> {
                    isLoading.setValue(false);
                    errorMessage.setValue("Failed to update profile picture: " + e.getMessage());
                    imageUploadSuccess.setValue(false);
                });
    }

    /**
     * Logs out the current user.
     */
    public void logout() {
        auth.signOut();
        logoutComplete.setValue(true);
    }
}
