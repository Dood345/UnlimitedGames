package com.appsters.unlimitedgames.app.data.repository;

import com.appsters.unlimitedgames.app.data.model.FriendList;
import com.appsters.unlimitedgames.app.data.model.FriendRequest;
import com.appsters.unlimitedgames.app.util.RequestStatus;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.List;
import java.util.UUID;

/**
 * Repository for managing friend relationships and friend requests.
 * Each user has a single document in the "friendships" collection with an array of friend IDs.
 */
public class FriendsRepository {

    private static final String FRIENDSHIPS_COLLECTION = "friendships";
    private static final String FRIEND_REQUESTS_COLLECTION = "friend_requests";

    private final FirebaseFirestore db;

    public FriendsRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Sends a friend request from one user to another.
     */
    public void sendFriendRequest(String fromUserId, String toUserId, OnCompleteListener<Void> listener) {
        String requestId = UUID.randomUUID().toString();
        FriendRequest request = new FriendRequest(requestId, fromUserId, toUserId);
        db.collection(FRIEND_REQUESTS_COLLECTION)
                .document(requestId)
                .set(request)
                .addOnCompleteListener(listener);
    }

    /**
     * Retrieves all pending friend requests for a given user.
     */
    public void getPendingRequests(String userId, OnCompleteListener<List<FriendRequest>> listener) {
        db.collection(FRIEND_REQUESTS_COLLECTION)
                .whereEqualTo("toUserId", userId)
                .whereEqualTo("status", RequestStatus.PENDING.name())
                .get()
                .continueWith((Task<QuerySnapshot> task) -> {
                    if (task.isSuccessful()) {
                        return task.getResult().toObjects(FriendRequest.class);
                    } else {
                        throw task.getException() != null ? task.getException() : new Exception("Failed to load requests");
                    }
                })
                .addOnCompleteListener(listener);
    }

    /**
     * Accepts a friend request and updates both users' friend lists atomically using a batch.
     * Also marks the request as ACCEPTED.
     */
    public void acceptFriendRequest(FriendRequest request, OnCompleteListener<Void> listener) {
        WriteBatch batch = db.batch();

        // Update friend request status
        batch.update(db.collection(FRIEND_REQUESTS_COLLECTION)
                        .document(request.getRequestId()),
                "status", RequestStatus.ACCEPTED.name());

        // Add each user to the other's friend list
        batch.set(db.collection(FRIENDSHIPS_COLLECTION)
                        .document(request.getFromUserId()),
                new FriendList(request.getToUserId()),
                com.google.firebase.firestore.SetOptions.merge());
        batch.update(db.collection(FRIENDSHIPS_COLLECTION)
                        .document(request.getFromUserId()),
                "friends", FieldValue.arrayUnion(request.getToUserId()));

        batch.set(db.collection(FRIENDSHIPS_COLLECTION)
                        .document(request.getToUserId()),
                new FriendList(request.getFromUserId()),
                com.google.firebase.firestore.SetOptions.merge());
        batch.update(db.collection(FRIENDSHIPS_COLLECTION)
                        .document(request.getToUserId()),
                "friends", FieldValue.arrayUnion(request.getFromUserId()));

        batch.commit().addOnCompleteListener(listener);
    }

    /**
     * Declines a friend request (updates its status to REJECTED).
     */
    public void declineFriendRequest(String requestId, OnCompleteListener<Void> listener) {
        db.collection(FRIEND_REQUESTS_COLLECTION)
                .document(requestId)
                .update("status", RequestStatus.REJECTED.name())
                .addOnCompleteListener(listener);
    }

    /**
     * Retrieves the user's FriendList document.
     */
    public void getFriendList(String userId, OnCompleteListener<FriendList> listener) {
        db.collection(FRIENDSHIPS_COLLECTION)
                .document(userId)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        return task.getResult().toObject(FriendList.class);
                    } else {
                        return new FriendList(); // empty list if not found
                    }
                })
                .addOnCompleteListener(listener);
    }

    /**
     * Removes a friend from the user's friend list.
     */
    public void removeFriend(String userId, String friendId, OnCompleteListener<Void> listener) {
        WriteBatch batch = db.batch();

        batch.update(db.collection(FRIENDSHIPS_COLLECTION)
                        .document(userId),
                "friends", FieldValue.arrayRemove(friendId));
        batch.update(db.collection(FRIENDSHIPS_COLLECTION)
                        .document(friendId),
                "friends", FieldValue.arrayRemove(userId));

        batch.commit().addOnCompleteListener(listener);
    }
}
