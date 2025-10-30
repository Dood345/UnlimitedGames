package com.appsters.unlimitedgames.ui.friends;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import android.util.Log;

import com.appsters.unlimitedgames.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class FriendsViewModel extends ViewModel {

    private static final String TAG = "FriendsViewModel";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    private final MutableLiveData<List<User>> friendRequests = new MutableLiveData<>();
    private final MutableLiveData<List<User>> friends = new MutableLiveData<>();
    private final MutableLiveData<List<User>> searchResults = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public LiveData<List<User>> getFriendRequests() {
        return friendRequests;
    }

    public LiveData<List<User>> getFriends() {
        return friends;
    }

    public LiveData<List<User>> getSearchResults() {
        return searchResults;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void fetchFriendRequests() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return;

        loading.setValue(true);
        String currentUserId = currentUser.getUid();
        DocumentReference userDoc = db.collection("users").document(currentUserId);

        userDoc.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> requestUids = (List<String>) documentSnapshot.get("friendRequestsReceived");
                if (requestUids != null && !requestUids.isEmpty()) {
                    db.collection("users").whereIn("uid", requestUids).get().addOnSuccessListener(queryDocumentSnapshots -> {
                        List<User> users = queryDocumentSnapshots.toObjects(User.class);
                        friendRequests.setValue(users);
                        loading.setValue(false);
                    }).addOnFailureListener(e -> {
                        Log.e(TAG, "Error fetching friend request users: ", e);
                        loading.setValue(false);
                        error.setValue("Failed to load friend requests.");
                    });
                } else {
                    friendRequests.setValue(new ArrayList<>());
                    loading.setValue(false);
                }
            } else {
                loading.setValue(false);
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error fetching user document: ", e);
            loading.setValue(false);
            error.setValue("Failed to load user data.");
        });
    }

    public void fetchFriends() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return;

        loading.setValue(true);
        String currentUserId = currentUser.getUid();
        DocumentReference userDoc = db.collection("users").document(currentUserId);

        userDoc.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> friendUids = (List<String>) documentSnapshot.get("friends");
                if (friendUids != null && !friendUids.isEmpty()) {
                    db.collection("users").whereIn("uid", friendUids).get().addOnSuccessListener(queryDocumentSnapshots -> {
                        List<User> users = queryDocumentSnapshots.toObjects(User.class);
                        friends.setValue(users);
                        loading.setValue(false);
                    }).addOnFailureListener(e -> {
                        Log.e(TAG, "Error fetching friend users: ", e);
                        loading.setValue(false);
                        error.setValue("Failed to load friends.");
                    });
                } else {
                    friends.setValue(new ArrayList<>());
                    loading.setValue(false);
                }
            } else {
                loading.setValue(false);
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error fetching user document for friends: ", e);
            loading.setValue(false);
            error.setValue("Failed to load user data.");
        });
    }

    public void acceptFriendRequest(User user) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return;

        String currentUserId = currentUser.getUid();
        DocumentReference currentUserDoc = db.collection("users").document(currentUserId);
        DocumentReference friendUserDoc = db.collection("users").document(user.getUid());

        currentUserDoc.update("friends", FieldValue.arrayUnion(user.getUid()));
        currentUserDoc.update("friendRequestsReceived", FieldValue.arrayRemove(user.getUid()));

        friendUserDoc.update("friends", FieldValue.arrayUnion(currentUserId));
        friendUserDoc.update("friendRequestsSent", FieldValue.arrayRemove(currentUserId));

        fetchFriendRequests();
        fetchFriends();
    }

    public void declineFriendRequest(User user) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return;

        String currentUserId = currentUser.getUid();
        DocumentReference currentUserDoc = db.collection("users").document(currentUserId);
        DocumentReference friendUserDoc = db.collection("users").document(user.getUid());

        currentUserDoc.update("friendRequestsReceived", FieldValue.arrayRemove(user.getUid()));
        friendUserDoc.update("friendRequestsSent", FieldValue.arrayRemove(currentUserId));

        fetchFriendRequests();
    }

    public void searchUsers(String emailQuery) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return;

        loading.setValue(true);
        if (emailQuery.isEmpty()) {
            searchResults.setValue(new ArrayList<>());
            loading.setValue(false);
            return;
        }

        db.collection("users")
                .whereEqualTo("email", emailQuery)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<User> users = queryDocumentSnapshots.toObjects(User.class);
                    String currentUserId = auth.getCurrentUser().getUid();
                    users.removeIf(u -> u.getUid().equals(currentUserId));
                    searchResults.setValue(users);
                    loading.setValue(false);
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Error searching users: ", e);
                    loading.setValue(false);
                    error.setValue("Search failed.");
                });
    }

    public void sendFriendRequest(User targetUser) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return;
        
        String currentUserId = currentUser.getUid();
        DocumentReference currentUserDoc = db.collection("users").document(currentUserId);
        DocumentReference targetUserDoc = db.collection("users").document(targetUser.getUid());

        currentUserDoc.update("friendRequestsSent", FieldValue.arrayUnion(targetUser.getUid()));

        targetUserDoc.update("friendRequestsReceived", FieldValue.arrayUnion(currentUserId));
    }
}
