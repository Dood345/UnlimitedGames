package com.appsters.unlimitedgames.app.data.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a user's list of friend IDs.
 * Each user has one document in the "friendships" collection
 * with an array of friend IDs.
 */
public class FriendList {
    private List<String> friends;

    /** Required empty constructor for Firestore. */
    public FriendList() {
        this.friends = new ArrayList<>();
    }

    /** Constructor to create a new list with the first friend. */
    public FriendList(String firstFriendId) {
        this.friends = new ArrayList<>();
        this.friends.add(firstFriendId);
    }

    /** Constructor to set an existing list. */
    public FriendList(List<String> friends) {
        this.friends = friends != null ? friends : new ArrayList<>();
    }

    public List<String> getFriends() {
        return friends;
    }

    public void setFriends(List<String> friends) {
        this.friends = friends;
    }

    /** Helper method to add a friend locally (not persisted). */
    public void addFriend(String friendId) {
        if (!friends.contains(friendId)) {
            friends.add(friendId);
        }
    }

    /** Helper method to remove a friend locally (not persisted). */
    public void removeFriend(String friendId) {
        friends.remove(friendId);
    }
}
