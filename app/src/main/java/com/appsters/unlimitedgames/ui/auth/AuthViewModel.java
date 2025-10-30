package com.appsters.unlimitedgames.ui.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.appsters.unlimitedgames.data.model.User;
import com.appsters.unlimitedgames.ui.auth.AuthState;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * ViewModel for authentication-related operations.
 * This ViewModel handles user sign-up, sign-in, and sign-out, and exposes the current
 * authentication state to the UI.
 */
public class AuthViewModel extends ViewModel {

    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore db;
    private final MutableLiveData<AuthState> authState = new MutableLiveData<>();
    private final MutableLiveData<FirebaseUser> user = new MutableLiveData<>();
    private String errorMessage;

    /**
     * Constructs a new AuthViewModel and initializes Firebase Auth.
     * It also checks the current authentication state to see if a user is already signed in.
     */
    public AuthViewModel() {
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Check if a user is already logged in
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null && !currentUser.isAnonymous()) {
            user.setValue(currentUser);
            authState.setValue(AuthState.AUTHENTICATED);
        } else {
            authState.setValue(AuthState.UNAUTHENTICATED);
        }
    }

    /**
     * Gets the current authentication state.
     *
     * @return A LiveData object that emits the current {@link AuthState}.
     */
    public LiveData<AuthState> getAuthState() {
        return authState;
    }

    /**
     * Gets the currently authenticated user.
     *
     * @return A LiveData object that emits the current {@link FirebaseUser}, or null if no user is
     *         authenticated.
     */
    public LiveData<FirebaseUser> getUser() {
        return user;
    }

    /**
     * Gets the last error message that occurred during an authentication operation.
     *
     * @return The error message, or null if no error has occurred.
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Signs up a new user with the given email and password.
     * Creates both a Firebase Auth account and a Firestore user profile.
     * The authentication state will be updated to {@link AuthState#LOADING} during the operation,
     * and then to {@link AuthState#AUTHENTICATED} on success or {@link AuthState#ERROR} on failure.
     *
     * @param email    The user's email address.
     * @param password The user's password.
     */
    public void signUp(String username, String email, String password) {
        authState.setValue(AuthState.LOADING);
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            createUserProfile(firebaseUser, username, email);
                        } else {
                            errorMessage = "Failed to get user information";
                            authState.setValue(AuthState.ERROR);
                        }
                    } else {
                        errorMessage = task.getException() != null ?
                                task.getException().getMessage() : "Sign up failed";
                        authState.setValue(AuthState.ERROR);
                    }
                });
    }

    /**
     * Creates a user profile document in Firestore.
     *
     * @param firebaseUser The Firebase Auth user
     * @param email        The user's email address
     */
    private void createUserProfile(FirebaseUser firebaseUser, String username, String email) {
        String userId = firebaseUser.getUid();
        User newUser = new User(userId, username, email, "");

        db.collection("users")
                .document(userId)
                .set(newUser)
                .addOnSuccessListener(aVoid -> {
                    user.setValue(firebaseUser);
                    authState.setValue(AuthState.AUTHENTICATED);
                })
                .addOnFailureListener(e -> {
                    errorMessage = "Failed to create user profile: " + e.getMessage();
                    authState.setValue(AuthState.ERROR);
                    firebaseUser.delete();
                });
    }

    /**
     * Signs in an existing user with the given email and password.
     * The authentication state will be updated to {@link AuthState#LOADING} during the operation,
     * and then to {@link AuthState#AUTHENTICATED} on success or {@link AuthState#ERROR} on failure.
     *
     * @param email    The user's email address.
     * @param password The user's password.
     */
    public void signIn(String email, String password) {
        authState.setValue(AuthState.LOADING);
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        user.setValue(firebaseAuth.getCurrentUser());
                        authState.setValue(AuthState.AUTHENTICATED);
                    } else {
                        errorMessage = task.getException() != null ?
                                task.getException().getMessage() : "Sign in failed";
                        authState.setValue(AuthState.ERROR);
                    }
                });
    }

    /**
     * Signs out the currently authenticated user.
     * The authentication state will be updated to {@link AuthState#UNAUTHENTICATED}.
     */
    public void signOut() {
        firebaseAuth.signOut();
        authState.setValue(AuthState.UNAUTHENTICATED);
    }
}