package com.appsters.simpleGames.games.whackamole.model;

import java.util.Random;

/**
 * Represents the different types of moles that can appear, each with a distinct color and point value.
 * This enum is used to determine the score awarded for hitting a specific mole.
 */
public enum MoleColor {
    /** A red mole, worth 5 points. */
    RED(5),
    /** A blue mole, worth 3 points. */
    BLUE(3),
    /** A green mole, worth 2 points. */
    GREEN(2),
    /** A yellow mole, worth 1 point. */
    YELLOW(1),
    /** A rare purple mole, worth 10 points. */
    PURPLE(10);

    private final int points;

    MoleColor(int points) {
        this.points = points;
    }

    /**
     * Gets the point value associated with this mole color.
     *
     * @return The number of points awarded for hitting a mole of this color.
     */
    public int getPoints() {
        return points;
    }

    /**
     * Selects and returns a random MoleColor from the set of all possible colors.
     * Each color has an equal chance of being chosen.
     *
     * @return A randomly selected {@link MoleColor}.
     */
    public static MoleColor randomColor() {
        MoleColor[] values = values();
        return values[new Random().nextInt(values.length)];
    }
}
