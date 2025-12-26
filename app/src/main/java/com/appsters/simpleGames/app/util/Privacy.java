package com.appsters.simpleGames.app.util;

import androidx.annotation.NonNull;

public enum Privacy {
    PUBLIC("Public"),
    PRIVATE("Private"),
    FRIENDS_ONLY("Friends Only");

    private final String displayName;

    Privacy(String displayName) {
        this.displayName = displayName;
    }

    @NonNull
    @Override
    public String toString() {
        return displayName;
    }
}
