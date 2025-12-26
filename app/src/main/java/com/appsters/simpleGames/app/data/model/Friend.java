package com.appsters.simpleGames.app.data.model;

import com.appsters.simpleGames.app.util.FriendStatus;
import java.util.Date;

public class Friend {

    private String id;

    // ✅ IDs
    private String fromUserId;
    private String toUserId;

    // ✅ Usernames (NEW)
    private String fromUsername;
    private String toUsername;

    private FriendStatus status;
    private Date createdAt;
    private Date updatedAt;

    // ✅ Transient (Not in Firestore)
    private String profileBase64;

    public Friend() {
    }

    public Friend(String fromUserId, String toUserId, String fromUsername, String toUsername) {
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.fromUsername = fromUsername;
        this.toUsername = toUsername;
        this.status = FriendStatus.PENDING;

        Date now = new Date();
        this.createdAt = now;
        this.updatedAt = now;
    }

    // ------------------------
    // ID
    // ------------------------

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // ------------------------
    // User IDs
    // ------------------------

    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getToUserId() {
        return toUserId;
    }

    public void setToUserId(String toUserId) {
        this.toUserId = toUserId;
    }

    // ------------------------
    // ✅ Usernames
    // ------------------------

    public String getFromUsername() {
        return fromUsername;
    }

    public void setFromUsername(String fromUsername) {
        this.fromUsername = fromUsername;
    }

    public String getToUsername() {
        return toUsername;
    }

    public void setToUsername(String toUsername) {
        this.toUsername = toUsername;
    }

    // ------------------------
    // Status + Timestamps
    // ------------------------

    public FriendStatus getStatus() {
        return status;
    }

    public void setStatus(FriendStatus status) {
        this.status = status;
        this.updatedAt = new Date();
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    // ------------------------
    // ✅ Transient
    // ------------------------

    @com.google.firebase.firestore.Exclude
    public String getProfileBase64() {
        return profileBase64;
    }

    public void setProfileBase64(String profileBase64) {
        this.profileBase64 = profileBase64;
    }
}
