package com.appsters.unlimitedgames.data.model;

import com.appsters.unlimitedgames.util.Privacy;
import com.google.firebase.firestore.PropertyName;

import java.util.HashMap;
import java.util.Map;

import com.google.firebase.firestore.PropertyName;

public class User {
    @PropertyName("uid")
    private String userId;
    private String username;
    private String email;
    private String profileImageUrl;
    private Privacy privacy;
    private long createdAt;
    private Map<String, Integer> highScores;

    public User() {
        this.privacy = Privacy.PUBLIC;
        this.createdAt = System.currentTimeMillis();
        this.highScores = new HashMap<>();
    }

    public User(String userId, String username, String email, String profileImageUrl) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.profileImageUrl = profileImageUrl;
        this.privacy = Privacy.PUBLIC;
        this.createdAt = System.currentTimeMillis();
        this.highScores = new HashMap<>();
    }

    // Getters and setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public Privacy getPrivacy() {
        return privacy;
    }

    public void setPrivacy(Privacy privacy) {
        this.privacy = privacy;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public Map<String, Integer> getHighScores() {
        return highScores;
    }

    public void setHighScores(Map<String, Integer> highScores) {
        this.highScores = highScores;
    }
}
