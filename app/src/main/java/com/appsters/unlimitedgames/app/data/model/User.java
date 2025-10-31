package com.appsters.unlimitedgames.app.data.model;

import com.appsters.unlimitedgames.app.util.Privacy;
import com.google.firebase.firestore.PropertyName;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a user in the application.
 */
public class User {
    /** The unique ID of the user. */
    @PropertyName("uid")
    private String userId;
    /** The username of the user. */
    private String username;
    /** The email address of the user. */
    private String email;
    /** The URL of the user's profile image. */
    private String profileImageUrl;
    /** The hexadecimal color code for the user's profile. */
    private String profileColor; // Add this field
    /** The privacy setting for the user's profile. */
    private Privacy privacy;
    /** The timestamp of when the user account was created. */
    private long createdAt;
    /** A map of game names to the user's high scores. */
    private Map<String, Integer> highScores;

    /**
     * Default constructor required for calls to DataSnapshot.getValue(User.class).
     * Initializes a new user with default values.
     */
    public User() {
        this.privacy = Privacy.PUBLIC;
        this.createdAt = System.currentTimeMillis();
        this.highScores = new HashMap<>();
        this.profileColor = "#4ECDC4"; // Default color
    }

    /**
     * Constructs a new User with the specified details.
     *
     * @param userId          The unique ID of the user.
     * @param username        The username of the user.
     * @param email           The email address of the user.
     * @param profileImageUrl The URL of the user's profile image.
     */
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

    /**
     * Gets the user ID.
     *
     * @return The user ID.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the user ID.
     *
     * @param userId The new user ID.
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Gets the username.
     *
     * @return The username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username.
     *
     * @param username The new username.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the email address.
     *
     * @return The email address.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email address.
     *
     * @param email The new email address.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the profile image URL.
     *
     * @return The profile image URL.
     */
    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    /**
     * Sets the profile image URL.
     *
     * @param profileImageUrl The new profile image URL.
     */
    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    /**
     * Gets the privacy setting.
     *
     * @return The privacy setting.
     */
    public Privacy getPrivacy() {
        return privacy;
    }

    /**
     * Sets the privacy setting.
     *
     * @param privacy The new privacy setting.
     */
    public void setPrivacy(Privacy privacy) {
        this.privacy = privacy;
    }

    /**
     * Gets the creation timestamp.
     *
     * @return The creation timestamp.
     */
    public long getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the creation timestamp.
     *
     * @param createdAt The new creation timestamp.
     */
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Gets the high scores map.
     *
     * @return The high scores map.
     */
    public Map<String, Integer> getHighScores() {
        return highScores;
    }

    /**
     * Sets the high scores map.
     *
     * @param highScores The new high scores map.
     */
    public void setHighScores(Map<String, Integer> highScores) {
        this.highScores = highScores;
    }

    // Add getter and setter for profileColor
    /**
     * Gets the profile color.
     *
     * @return The profile color.
     */
    public String getProfileColor() {
        return profileColor;
    }

    /**
     * Sets the profile color.
     *
     * @param profileColor The new profile color.
     */
    public void setProfileColor(String profileColor) {
        this.profileColor = profileColor;
    }

    /**
     * Generates a color for the user based on their user ID.
     *
     * @param userId The user ID.
     * @return A hexadecimal color string.
     */
    private String generateColorForUser(String userId) {
        String[] colors = {
                "#FF6B6B", "#4ECDC4", "#45B7D1", "#FFA07A",
                "#98D8C8", "#F7DC6F", "#BB8FCE", "#85C1E2"
        };
        int index = Math.abs(userId.hashCode()) % colors.length;
        return colors[index];
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * Two users are considered equal if they have the same user ID and email.
     *
     * @param o The reference object with which to compare.
     * @return {@code true} if this object is the same as the obj argument;
     *         {@code false} otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(userId, user.userId) &&
                Objects.equals(email, user.email);
    }
}
