package com.appsters.unlimitedgames.app.data.repository;

import com.appsters.unlimitedgames.app.data.model.FriendRequest;
import com.appsters.unlimitedgames.app.data.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

/**
 * A repository for managing friends-related data in Firestore.
 * This class provides methods for sending, accepting, and rejecting friend requests,
 * as well as removing friends and retrieving a user's friends list and pending requests.
 */
public class FriendsRepository {
    private FirebaseFirestore db;

    /**
     * Constructs a new FriendsRepository and initializes the Firestore instance.
     */
    public FriendsRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    // TODO: Implement these methods

    /**
     * Sends a friend request from one user to another.
     *
     * @param fromUserId The ID of the user sending the request.
     * @param toUserId   The ID of the user receiving the request.
     * @param listener   A listener that will be called when the operation is complete.
     */
    public void sendFriendRequest(String fromUserId, String toUserId, OnCompleteListener<Void> listener) {}

    /**
     * Accepts a friend request.
     *
     * @param requestId The ID of the friend request to accept.
     * @param listener  A listener that will be called when the operation is complete.
     */
    public void acceptFriendRequest(String requestId, OnCompleteListener<Void> listener) {}

    /**
     * Rejects a friend request.
     *
     * @param requestId The ID of the friend request to reject.
     * @param listener  A listener that will be called when the operation is complete.
     */
    public void rejectFriendRequest(String requestId, OnCompleteListener<Void> listener) {}

    /**
     * Removes a friend from a user's friend list.
     *
     * @param userId   The ID of the user removing the friend.
     * @param friendId The ID of the friend to remove.
     * @param listener A listener that will be called when the operation is complete.
     */
    public void removeFriend(String userId, String friendId, OnCompleteListener<Void> listener) {}

    /**
     * Gets a list of a user's friends.
     *
     * @param userId   The ID of the user whose friends to retrieve.
     * @param listener A listener that will be called with a list of {@link User} objects representing
     *                 the user's friends, or an exception if the operation fails.
     */
    public void getFriends(String userId, OnCompleteListener<List<User>> listener) {}

    /**
     * Gets a list of a user's pending friend requests.
     *
     * @param userId   The ID of the user whose pending requests to retrieve.
     * @param listener A listener that will be called with a list of {@link FriendRequest} objects
     *                 representing the user's pending requests, or an exception if the operation
     *                 fails.
     */
    public void getPendingRequests(String userId, OnCompleteListener<List<FriendRequest>> listener) {}
}
