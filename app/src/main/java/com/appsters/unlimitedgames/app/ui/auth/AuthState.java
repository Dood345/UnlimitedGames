package com.appsters.unlimitedgames.app.ui.auth;

/**
 * Represents the authentication state of the user.
 */
public enum AuthState {
    /**
     * The user is authenticated.
     */
    AUTHENTICATED,
    /**
     * The user is not authenticated.
     */
    UNAUTHENTICATED,
    /**
     * The authentication state is currently being determined.
     */
    LOADING,
    /**
     * An error occurred during authentication.
     */
    ERROR
}
