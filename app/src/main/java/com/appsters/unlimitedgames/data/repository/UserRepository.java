package com.appsters.unlimitedgames.data.repository;

import com.appsters.unlimitedgames.data.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

/**
 * A repository for managing user data in Firestore.
 * This class provides methods for creating, retrieving, updating, and deleting users,
 * as well as searching for users.
 */
public class UserRepository {
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    /**
     * Constructs a new UserRepository and initializes the Firestore and FirebaseAuth instances.
     */
    public UserRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    // TODO: Implement these methods

    /**
     * Creates a new user in Firestore.
     *
     * @param user     The user to create.
     * @param listener A listener that will be called with the created {@link User} object, or an
     *                 exception if the operation fails.
     */
    public void createUser(User user, OnCompleteListener<User> listener) {}

    /**
     * Gets a user from Firestore.
     *
     * @param userId   The ID of the user to retrieve.
     * @param listener A listener that will be called with the {@link User} object, or an exception
     *                 if the operation fails.
     */
    public void getUser(String userId, OnCompleteListener<User> listener) {}

    /**
     * Updates an existing user in Firestore.
     *
     * @param user     The user to update.
     * @param listener A listener that will be called when the operation is complete.
     */
    public void updateUser(User user, OnCompleteListener<Void> listener) {}

    /**
     * Deletes a user from Firestore.
     *
     * @param userId   The ID of the user to delete.
     * @param listener A listener that will be called when the operation is complete.
     */
    public void deleteUser(String userId, OnCompleteListener<Void> listener) {}

    /**
     * Searches for users in Firestore.
     *
     * @param query    The search query.
     * @param listener A listener that will be called with a list of {@link User} objects that match
     *                 the query, or an exception if the operation fails.
     */
    public void searchUsers(String query, OnCompleteListener<List<User>> listener) {}
}
