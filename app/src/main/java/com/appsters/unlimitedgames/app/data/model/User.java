package com.appsters.unlimitedgames.app.data.model;

import com.appsters.unlimitedgames.app.util.Privacy;
import com.google.firebase.firestore.PropertyName;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class User {
    @PropertyName("uid")
    private String userId;
    private String username;
    private String email;
    private String profileImageUrl;
    private String profileColor; // Add this field
    private Privacy privacy;
    private long createdAt;
    private Map<String, Integer> highScores;

    public User() {
        this.privacy = Privacy.PUBLIC;
        this.createdAt = System.currentTimeMillis();
        this.highScores = new HashMap<>();
        this.profileColor = "#4ECDC4"; // Default color
    }

    public User(String userId, String username, String email, String profileImageUrl) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.profileImageUrl = profileImageUrl;
        this.privacy = Privacy.PUBLIC;
        this.createdAt = System.currentTimeMillis();
        this.highScores = new HashMap<>();
        this.profileColor = generateColorForUser(userId); // Generate based on userId
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

    // Add getter and setter for profileColor
    public String getProfileColor() {
        return profileColor;
    }

    public void setProfileColor(String profileColor) {
        this.profileColor = profileColor;
    }

    private String generateColorForUser(String userId) {
        String[] colors = {
                "#FF6B6B", "#4ECDC4", "#45B7D1", "#FFA07A",
                "#98D8C8", "#F7DC6F", "#BB8FCE", "#85C1E2"
        };
        int index = Math.abs(userId.hashCode()) % colors.length;
        return colors[index];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(userId, user.userId) &&
                Objects.equals(email, user.email);
    }
}
