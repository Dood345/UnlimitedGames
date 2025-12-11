package com.appsters.unlimitedgames.app.data.repository;

import com.appsters.unlimitedgames.app.data.model.Friend;
import com.appsters.unlimitedgames.app.data.model.User;
import com.appsters.unlimitedgames.app.util.FriendStatus;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FriendRepository {

    private static final String COLLECTION = "friends";
    private final FirebaseFirestore db;

    public FriendRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    public void sendFriendRequest(
            String fromUserId,
            String toUserId,
            String fromUsername,
            String toUsername,
            OnCompleteListener<Void> listener) {
        Friend request = new Friend(fromUserId, toUserId, fromUsername, toUsername);

        db.collection(COLLECTION)
                .add(request)
                .continueWithTask(task -> {
                    if (!task.isSuccessful() || task.getResult() == null) {
                        return com.google.android.gms.tasks.Tasks.forResult(null);
                    }

                    String docId = task.getResult().getId();
                    request.setId(docId);

                    return task.getResult().update("id", docId);
                })
                .addOnCompleteListener(listener);
    }

    public void acceptFriendRequest(String requestId, OnCompleteListener<Void> listener) {
        db.collection(COLLECTION)
                .document(requestId)
                .update(
                        "status", FriendStatus.ACCEPTED.name(),
                        "updatedAt", new Date())
                .addOnCompleteListener(listener);
    }

    public void declineFriendRequest(String requestId, OnCompleteListener<Void> listener) {
        db.collection(COLLECTION)
                .document(requestId)
                .update(
                        "status", FriendStatus.DECLINED.name(),
                        "updatedAt", new Date())
                .addOnCompleteListener(listener);
    }

    public void removeFriend(String userA, String userB, OnCompleteListener<Void> listener) {
        db.collection(COLLECTION)
                .where(
                        Filter.or(
                                Filter.and(
                                        Filter.equalTo("fromUserId", userA),
                                        Filter.equalTo("toUserId", userB)),
                                Filter.and(
                                        Filter.equalTo("fromUserId", userB),
                                        Filter.equalTo("toUserId", userA))))
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful() || task.getResult() == null) {
                        listener.onComplete(
                                com.google.android.gms.tasks.Tasks.forException(
                                        task.getException() != null ? task.getException()
                                                : new Exception("Failed to load friend documents")));
                        return;
                    }

                    List<com.google.android.gms.tasks.Task<Void>> deletes = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        deletes.add(
                                db.collection(COLLECTION)
                                        .document(doc.getId())
                                        .delete());
                    }

                    com.google.android.gms.tasks.Tasks.whenAll(deletes)
                            .addOnCompleteListener(listener);
                });
    }

    public void getFriends(String userId, OnCompleteListener<List<Friend>> listener) {
        db.collection(COLLECTION)
                .whereEqualTo("status", FriendStatus.ACCEPTED.name())
                .where(
                        com.google.firebase.firestore.Filter.or(
                                com.google.firebase.firestore.Filter.equalTo("fromUserId", userId),
                                com.google.firebase.firestore.Filter.equalTo("toUserId", userId)))
                .get()
                .addOnCompleteListener(task -> {
                    List<Friend> friends = new ArrayList<>();

                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            friends.add(doc.toObject(Friend.class));
                        }
                    }

                    listener.onComplete(
                            com.google.android.gms.tasks.Tasks.forResult(friends));
                });
    }

    public void getIncomingRequests(String userId, OnCompleteListener<List<Friend>> listener) {
        db.collection(COLLECTION)
                .whereEqualTo("status", FriendStatus.PENDING.name())
                .whereEqualTo("toUserId", userId)
                .get()
                .addOnCompleteListener(task -> {

                    List<Friend> requests = new ArrayList<>();

                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            requests.add(doc.toObject(Friend.class));
                        }
                    }

                    listener.onComplete(
                            com.google.android.gms.tasks.Tasks.forResult(requests));
                });
    }

    public void getOutgoingRequests(String userId, OnCompleteListener<List<Friend>> listener) {
        db.collection(COLLECTION)
                .whereEqualTo("status", FriendStatus.PENDING.name())
                .whereEqualTo("fromUserId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    List<Friend> outgoing = new ArrayList<>();
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            outgoing.add(doc.toObject(Friend.class));
                        }
                    }
                    listener.onComplete(com.google.android.gms.tasks.Tasks.forResult(outgoing));
                });
    }

    public void getAllFriendRelations(String userId, OnCompleteListener<List<Friend>> listener) {

        db.collection(COLLECTION)
                .where(
                        Filter.or(
                                Filter.equalTo("fromUserId", userId),
                                Filter.equalTo("toUserId", userId)))
                .get()
                .addOnCompleteListener(task -> {

                    List<Friend> relations = new ArrayList<>();

                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            relations.add(doc.toObject(Friend.class));
                        }
                    }

                    listener.onComplete(
                            com.google.android.gms.tasks.Tasks.forResult(relations));
                });
    }

    public void loadAllNewFriends(String currentUserId, OnCompleteListener<List<User>> listener) {

        // First load all PUBLIC users
        db.collection("users")
                .whereEqualTo("privacy", "PUBLIC")
                .get()
                .addOnCompleteListener(userTask -> {

                    List<User> allUsers = new ArrayList<>();

                    if (userTask.isSuccessful() && userTask.getResult() != null) {
                        for (QueryDocumentSnapshot doc : userTask.getResult()) {
                            User u = doc.toObject(User.class);

                            // Skip myself
                            if (!u.getUserId().equals(currentUserId)) {
                                allUsers.add(u);
                            }
                        }
                    }

                    // Then load relations to filter out existing friends/pending requests
                    getAllFriendRelations(currentUserId, relTask -> {

                        List<Friend> relations = relTask.getResult() != null ? relTask.getResult() : new ArrayList<>();

                        List<User> finalList = new ArrayList<>();

                        for (User u : allUsers) {

                            boolean related = false;

                            for (Friend f : relations) {
                                if (f.getFromUserId().equals(u.getUserId()) ||
                                        f.getToUserId().equals(u.getUserId())) {
                                    related = true;
                                    break;
                                }
                            }

                            if (!related) {
                                finalList.add(u);
                            }
                        }

                        listener.onComplete(
                                com.google.android.gms.tasks.Tasks.forResult(finalList));
                    });
                });
    }

    /**
     * Searches for new friends using a privacy-aware split query approach.
     * <p>
     * Task 1: Find PUBLIC users who match the search (Partial Match).
     * Task 2: Find ANY user who matches EXACTLY (Exact Match overrides privacy).
     * Task 3: Merge results, removing duplicates and the current user.
     *
     * @param currentUserId The ID of the current user (to exclude from results).
     * @param searchQuery   The search query string.
     * @param listener      Listener to handle the result list.
     */
    public void searchNewFriends(String currentUserId, String searchQuery, OnCompleteListener<List<User>> listener) {
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            listener.onComplete(com.google.android.gms.tasks.Tasks.forResult(new ArrayList<>()));
            return;
        }

        String query = searchQuery.trim();

        // Task 1: Find PUBLIC users who match the search (Partial Match)
        // Note: Filtering by privacy="PUBLIC" here requires a composite index on
        // (privacy, username)
        // which triggers FAILED_PRECONDITION errors if missing.
        // Workaround: Query by username only, then filter privacy in code.
        com.google.android.gms.tasks.Task<com.google.firebase.firestore.QuerySnapshot> publicSearch = db
                .collection("users")
                .whereGreaterThanOrEqualTo("username", query)
                .whereLessThan("username", query + "\uf8ff")
                .get();

        // Task 2: Find ANY user who matches EXACTLY (Exact Match overrides privacy)
        com.google.android.gms.tasks.Task<com.google.firebase.firestore.QuerySnapshot> exactSearch = db
                .collection("users")
                .whereEqualTo("username", query)
                .get();

        // Task 3: Run both in parallel
        com.google.android.gms.tasks.Tasks.whenAllSuccess(publicSearch, exactSearch).addOnSuccessListener(results -> {
            // 'results' is a list of objects.
            // Index 0 = publicSearch result
            // Index 1 = exactSearch result

            List<User> finalUsers = new ArrayList<>();
            java.util.Set<String> seenIds = new java.util.HashSet<>(); // Avoid duplicates

            // Process Public Results
            com.google.firebase.firestore.QuerySnapshot publicSnap = (com.google.firebase.firestore.QuerySnapshot) results
                    .get(0);
            for (com.google.firebase.firestore.DocumentSnapshot doc : publicSnap.getDocuments()) {
                User u = doc.toObject(User.class);
                // Manual privacy check since we removed valid query filter
                if (u != null && "PUBLIC".equals(u.getPrivacy()) && !u.getUserId().equals(currentUserId)) {
                    finalUsers.add(u);
                    seenIds.add(u.getUserId());
                }
            }

            // Process Exact Match Results
            com.google.firebase.firestore.QuerySnapshot exactSnap = (com.google.firebase.firestore.QuerySnapshot) results
                    .get(1);
            for (com.google.firebase.firestore.DocumentSnapshot doc : exactSnap.getDocuments()) {
                User u = doc.toObject(User.class);
                // Only add if not already in the list (and not myself)
                if (u != null && !seenIds.contains(u.getUserId()) && !u.getUserId().equals(currentUserId)) {
                    finalUsers.add(u);
                }
            }

            // Return the clean, secure list
            listener.onComplete(com.google.android.gms.tasks.Tasks.forResult(finalUsers));
        }).addOnFailureListener(e -> {
            listener.onComplete(com.google.android.gms.tasks.Tasks.forException(e));
        });
    }

    public void deleteAllFriends(String userId, OnCompleteListener<Void> listener) {
        db.collection(COLLECTION)
                .where(Filter.or(
                        Filter.equalTo("fromUserId", userId),
                        Filter.equalTo("toUserId", userId)))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<com.google.android.gms.tasks.Task<Void>> deletes = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            deletes.add(doc.getReference().delete());
                        }

                        if (deletes.isEmpty()) {
                            listener.onComplete(com.google.android.gms.tasks.Tasks.forResult(null));
                        } else {
                            com.google.android.gms.tasks.Tasks.whenAll(deletes)
                                    .addOnCompleteListener(listener);
                        }
                    } else {
                        listener.onComplete(
                                com.google.android.gms.tasks.Tasks.forException(
                                        task.getException() != null ? task.getException()
                                                : new Exception("Failed to load friend documents")));
                    }
                });
    }

}
