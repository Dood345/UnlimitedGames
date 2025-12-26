package com.appsters.simpleGames.app.data.repository;

import com.appsters.simpleGames.app.data.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.List;

/**
 * A repository for managing user data in Firestore.
 * This class provides methods for creating, retrieving, updating, and deleting users,
 * as well as searching for users.
 */
public class UserRepository {
    private final FirebaseFirestore db;
    private static final String USERS_COLLECTION = "users";

    /**
     * Constructs a new UserRepository and initializes the Firestore instance.
     */
    public UserRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Creates a new user in Firestore.
     *
     * @param user     The user to create.
     * @param listener A listener that will be called when the operation is complete.
     */
    public void createUser(User user, OnCompleteListener<Void> listener) {
        db.collection(USERS_COLLECTION)
                .document(user.getUserId())
                .set(user)
                .addOnCompleteListener(listener);
    }

    /**
     * Gets a user from Firestore.
     *
     * @param userId   The ID of the user to retrieve.
     * @param listener A listener that will be called with the {@link User} object, or an exception
     *                 if the operation fails.
     */
    public void getUser(String userId, OnCompleteListener<User> listener) {
        db.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        User user = task.getResult().toObject(User.class);
                        if (user != null) {
                            return user;
                        } else {
                            throw new Exception("User data is null");
                        }
                    } else {
                        if (task.getException() != null) {
                            throw task.getException();
                        } else {
                            throw new Exception("User not found");
                        }
                    }
                })
                .addOnCompleteListener(listener);
    }

    /**
     * Updates an existing user in Firestore.
     *
     * @param user     The user to update.
     * @param listener A listener that will be called when the operation is complete.
     */
    public void updateUser(User user, OnCompleteListener<Void> listener) {
        db.collection(USERS_COLLECTION)
                .document(user.getUserId())
                .set(user, SetOptions.merge())
                .addOnCompleteListener(listener);
    }

    /**
     * Deletes a user from Firestore.
     *
     * @param userId   The ID of the user to delete.
     * @param listener A listener that will be called when the operation is complete.
     */
    public void deleteUser(String userId, OnCompleteListener<Void> listener) {
        db.collection(USERS_COLLECTION)
                .document(userId)
                .delete()
                .addOnCompleteListener(listener);
    }

    /**
     * Searches for users in Firestore.
     *
     * @param query    The search query.
     * @param listener A listener that will be called with a list of {@link User} objects that match
     *                 the query, or an exception if the operation fails.
     */
    public void searchUsers(String query, OnCompleteListener<List<User>> listener) {
        db.collection(USERS_COLLECTION)
                .whereEqualTo("username", query)
                .get()
                .continueWith((Task<QuerySnapshot> task) -> {
                    if (task.isSuccessful()) {
                        return task.getResult().toObjects(User.class);
                    } else {
                        if (task.getException() != null) {
                            throw task.getException();
                        } else {
                            throw new Exception("No users found");
                        }
                    }
                })
                .addOnCompleteListener(listener);
    }
}
