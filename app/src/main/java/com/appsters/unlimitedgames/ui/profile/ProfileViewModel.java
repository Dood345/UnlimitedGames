package com.appsters.unlimitedgames.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.appsters.unlimitedgames.data.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileViewModel extends ViewModel {

    private final FirebaseAuth auth;
    private final FirebaseFirestore db;

    private final MutableLiveData<User> currentUser = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public ProfileViewModel() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

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

    public void updatePrivacy(com.appsters.unlimitedgames.util.Privacy privacy) {
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
}