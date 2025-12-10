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

    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public LiveData<List<Friend>> getFriends() { return friends; }
    public LiveData<List<Friend>> getIncomingRequests() { return incomingRequests; }
    public LiveData<List<Friend>> getOutgoingRequests() { return outgoingRequests; }
    public LiveData<List<User>> getNewFriends() { return newFriends; }

    // ✅ CURRENT USER GETTER
    public LiveData<User> getCurrentUser() { return currentUser; }

    public LiveData<Boolean> getActionSuccess() { return actionSuccess; }

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
            isLoading.setValue(false);

            if (task.isSuccessful()) {
                friends.setValue(task.getResult());
            } else {
                errorMessage.setValue("Failed to load friends.");
            }
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
            String toUsername
    ) {
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
                }
        );
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
}
