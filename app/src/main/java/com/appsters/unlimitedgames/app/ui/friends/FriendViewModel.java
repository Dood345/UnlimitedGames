package com.appsters.unlimitedgames.app.ui.friends;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.appsters.unlimitedgames.app.data.model.Friend;
import com.appsters.unlimitedgames.app.data.model.User;
import com.appsters.unlimitedgames.app.data.repository.FriendRepository;
import com.appsters.unlimitedgames.app.data.repository.UserRepository;

import java.util.List;

public class FriendViewModel extends ViewModel {

    private final FriendRepository friendRepository;
    private final UserRepository userRepository;

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>(null);

    private final MutableLiveData<List<Friend>> friends = new MutableLiveData<>();
    private final MutableLiveData<List<Friend>> incomingRequests = new MutableLiveData<>();
    private final MutableLiveData<List<Friend>> outgoingRequests = new MutableLiveData<>();
    private final MutableLiveData<List<User>> newFriends = new MutableLiveData<>();

    // ✅ FIRESTORE-BASED CURRENT USER (LIKE ProfileViewModel)
    private final MutableLiveData<User> currentUser = new MutableLiveData<>();

    private final MutableLiveData<Boolean> actionSuccess = new MutableLiveData<>(false);

    public FriendViewModel() {
        friendRepository = new FriendRepository();
        userRepository = new UserRepository();
    }

    // ----------- LIVE DATA GETTERS -----------

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<List<Friend>> getFriends() {
        return friends;
    }

    public LiveData<List<Friend>> getIncomingRequests() {
        return incomingRequests;
    }

    public LiveData<List<Friend>> getOutgoingRequests() {
        return outgoingRequests;
    }

    public LiveData<List<User>> getNewFriends() {
        return newFriends;
    }

    // ✅ CURRENT USER GETTER
    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public LiveData<Boolean> getActionSuccess() {
        return actionSuccess;
    }

    public void resetFlags() {
        actionSuccess.setValue(false);
        errorMessage.setValue(null);
    }

    // ----------- LOAD CURRENT USER (FROM FIRESTORE) -----------

    public void loadCurrentUser(String userId) {
        isLoading.setValue(true);

        userRepository.getUser(userId, task -> {
            isLoading.setValue(false);

            if (task.isSuccessful() && task.getResult() != null) {
                currentUser.setValue(task.getResult());
            } else {
                errorMessage.setValue("Failed to load current user.");
            }
        });
    }

    // ----------- LOAD FRIEND LIST -----------

    public void loadFriends(String userId) {
        isLoading.setValue(true);

        friendRepository.getFriends(userId, task -> {
            if (!task.isSuccessful() || task.getResult() == null) {
                isLoading.setValue(false);
                errorMessage.setValue("Failed to load friends.");
                return;
            }

            List<Friend> friendList = task.getResult();
            if (friendList.isEmpty()) {
                isLoading.setValue(false);
                friends.setValue(friendList);
                return;
            }

            // Fetch profiles in parallel
            List<com.google.android.gms.tasks.Task<User>> userTasks = new java.util.ArrayList<>();

            for (Friend f : friendList) {
                String friendId = f.getFromUserId().equals(userId) ? f.getToUserId() : f.getFromUserId();

                // We need to use a TaskCompletionSource to wrap the UserRepository callback
                // into a Task
                com.google.android.gms.tasks.TaskCompletionSource<User> tcs = new com.google.android.gms.tasks.TaskCompletionSource<>();

                userRepository.getUser(friendId, userTask -> {
                    if (userTask.isSuccessful()) {
                        tcs.setResult(userTask.getResult());
                    } else {
                        // If one fails, just return null so we don't block others
                        tcs.setResult(null);
                    }
                });

                userTasks.add(tcs.getTask());
            }

            com.google.android.gms.tasks.Tasks.whenAllSuccess(userTasks).addOnSuccessListener(users -> {
                isLoading.setValue(false);
                // Users list corresponds to friendList order
                for (int i = 0; i < friendList.size(); i++) {
                    Object result = users.get(i);
                    if (result instanceof User) {
                        User u = (User) result;
                        friendList.get(i).setProfileBase64(u.getProfileImageUrl());
                    }
                }
                friends.setValue(friendList);
            });
        });
    }

    // ----------- LOAD INCOMING REQUESTS -----------

    public void loadIncomingRequests(String userId) {
        isLoading.setValue(true);

        friendRepository.getIncomingRequests(userId, task -> {
            isLoading.setValue(false);

            if (task.isSuccessful()) {
                incomingRequests.setValue(task.getResult());
            } else {
                errorMessage.setValue("Failed to load friend requests.");
            }
        });
    }

    // ----------- LOAD OUTGOING REQUESTS -----------

    public void loadOutgoingRequests(String userId) {
        isLoading.setValue(true);

        friendRepository.getOutgoingRequests(userId, task -> {
            isLoading.setValue(false);

            if (task.isSuccessful()) {
                outgoingRequests.setValue(task.getResult());
            } else {
                errorMessage.setValue("Failed to load outgoing requests.");
            }
        });
    }

    // ----------- SEND FRIEND REQUEST -----------

    public void sendFriendRequest(
            String fromUserId,
            String toUserId,
            String fromUsername,
            String toUsername) {
        isLoading.setValue(true);

        friendRepository.sendFriendRequest(
                fromUserId,
                toUserId,
                fromUsername,
                toUsername,
                task -> {
                    isLoading.setValue(false);

                    if (task.isSuccessful()) {
                        actionSuccess.setValue(true);
                    } else {
                        errorMessage.setValue("Failed to send friend request.");
                    }
                });
    }

    // ----------- ACCEPT FRIEND REQUEST -----------

    public void acceptFriendRequest(String requestId) {
        isLoading.setValue(true);

        friendRepository.acceptFriendRequest(requestId, task -> {
            isLoading.setValue(false);

            if (task.isSuccessful()) {
                actionSuccess.setValue(true);
            } else {
                errorMessage.setValue("Failed to accept request.");
            }
        });
    }

    // ----------- DECLINE FRIEND REQUEST -----------

    public void declineFriendRequest(String requestId) {
        isLoading.setValue(true);

        friendRepository.declineFriendRequest(requestId, task -> {
            isLoading.setValue(false);

            if (task.isSuccessful()) {
                actionSuccess.setValue(true);
            } else {
                errorMessage.setValue("Failed to decline request.");
            }
        });
    }

    // ----------- CANCEL FRIEND REQUEST -----------

    public void cancelFriendRequest(String requestId) {
        isLoading.setValue(true);

        friendRepository.deleteRequest(requestId, task -> {
            isLoading.setValue(false);

            if (task.isSuccessful()) {
                actionSuccess.setValue(true);
            } else {
                errorMessage.setValue("Failed to cancel request.");
            }
        });
    }

    // ----------- REMOVE FRIEND -----------

    public void removeFriend(String userA, String userB) {
        isLoading.setValue(true);

        friendRepository.removeFriend(userA, userB, task -> {
            isLoading.setValue(false);

            if (task.isSuccessful()) {
                actionSuccess.setValue(true);
            } else {
                errorMessage.setValue("Failed to remove friend.");
            }
        });
    }

    // ----------- FIND NEW FRIENDS -----------

    public void loadAllNewFriends(String currentUserId) {
        isLoading.setValue(true);

        friendRepository.loadAllNewFriends(currentUserId, task -> {
            isLoading.setValue(false);

            if (task.isSuccessful()) {
                newFriends.setValue(task.getResult());
            } else {
                errorMessage.setValue("Failed to load users.");
            }
        });
    }

    public void searchNewFriends(String query) {
        User me = currentUser.getValue();
        if (me == null) {
            errorMessage.setValue("Current user not loaded.");
            return;
        }

        if (query == null || query.trim().isEmpty()) {
            loadAllNewFriends(me.getUserId());
            return;
        }

        isLoading.setValue(true);
        friendRepository.searchNewFriends(me.getUserId(), query, task -> {
            isLoading.setValue(false);
            if (task.isSuccessful()) {
                newFriends.setValue(task.getResult());
            } else {
                errorMessage.setValue("Search failed.");
            }
        });
    }

    // ✅ LISTENER FOR NOTIFICATIONS
    private com.google.firebase.firestore.ListenerRegistration requestCountListener;
    private final MutableLiveData<Integer> ongoingRequestCount = new MutableLiveData<>(0);

    public LiveData<Integer> getOngoingRequestCount() {
        return ongoingRequestCount;
    }

    public void listenToRequestCount(String userId) {
        if (requestCountListener != null) {
            requestCountListener.remove();
        }

        requestCountListener = friendRepository.listenToIncomingRequestsCount(userId, (count, error) -> {
            if (error != null) {
                // Log error
                return;
            }
            ongoingRequestCount.setValue(count);
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (requestCountListener != null) {
            requestCountListener.remove();
        }
    }

}
