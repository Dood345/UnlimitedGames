package com.appsters.simpleGames.games.whackamole.model;

import androidx.annotation.NonNull;

/**
 * Immutable data class representing a Mole in the game.
 * Each mole has an ID, visibility status, and color (which determines points).
 */
public final class Mole {

    private final int id;
    private final boolean visible;
    private final MoleColor color;

    /**
     * Constructor that assigns a random color.
     *
     * @param id      unique identifier for the mole
     * @param visible whether the mole is currently visible
     */
    public Mole(int id, boolean visible) {
        this(id, visible, MoleColor.randomColor());
    }

    /**
     * Constructor that allows explicit color assignment.
     *
     * @param id      unique identifier for the mole
     * @param visible whether the mole is currently visible
     * @param color   specific MoleColor
     */
    public Mole(int id, boolean visible, MoleColor color) {
        this.id = id;
        this.visible = visible;
        this.color = color;
    }

    /**
     * Get the mole ID
     *
     * @return unique ID of the mole
     */
    public int getId() {
        return id;
    }

    /**
     * Check if the mole is visible
     *
     * @return true if visible, false otherwise
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Get the color of the mole
     *
     * @return specific MoleColor
     */
    public MoleColor getColor() {
        return color;
    }

    /**
     * String constructor for moles
     * 
     * @return string representing mole
     */
    @Override
    @NonNull
    public String toString() {
        return "Mole{" + "id=" + id + ", visible=" + visible + ", color=" + color + '}';
    }
}
