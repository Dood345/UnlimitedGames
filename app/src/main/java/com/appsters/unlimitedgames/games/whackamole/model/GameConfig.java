package com.appsters.unlimitedgames.games.whackamole.model;

/**
 * Holds immutable configuration values for the Whack-a-Mole game.
 */
public final class GameConfig {

    private final int maxMisses;
    private final int numMoles;
    private final long initialInterval;
    private final long minInterval;
    private final long intervalDecrement;
    private final long gameDurationMillis;

    /**
     * Default game configuration, providing a standard set of rules for a balanced game.
     * <ul>
     *   <li>maxMisses: 5</li>
     *   <li>numMoles: 9</li>
     *   <li>initialInterval: 2000ms</li>
     *   <li>minInterval: 500ms</li>
     *   <li>intervalDecrement: 100ms</li>
     *   <li>gameDurationMillis: 30000ms</li>
     * </ul>
     */
    public static final GameConfig DEFAULT = new GameConfig(
            5,     // maxMisses
            9,     // numMoles
            2000,  // initialInterval in ms
            500,   // minInterval in ms
            100,    // intervalDecrement per spawn
            30000 // gameDurationMillis in ms
    );

    /**
     * Constructs a new game configuration.
     *
     * @param maxMisses         The number of allowed misses before the game ends.
     * @param numMoles          The total number of moles (or holes) available in the game grid.
     * @param initialInterval   The initial time in milliseconds between mole appearances.
     * @param minInterval       The minimum time in milliseconds between mole appearances, representing the max difficulty.
     * @param intervalDecrement The amount of time in milliseconds to reduce the interval by after each successful hit, speeding up the game.
     * @param gameDurationMillis The total duration of the game in milliseconds.
     */
    public GameConfig(int maxMisses, int numMoles, long initialInterval, long minInterval,
                      long intervalDecrement, long gameDurationMillis) {
        this.maxMisses = maxMisses;
        this.numMoles = numMoles;
        this.initialInterval = initialInterval;
        this.minInterval = minInterval;
        this.intervalDecrement = intervalDecrement;
        this.gameDurationMillis = gameDurationMillis;
    }

    /**
     * @return The maximum number of allowed misses.
     */
    public int getMaxMisses() {
        return maxMisses;
    }

    /**
     * @return The total number of moles in the game grid.
     */
    public int getNumMoles() {
        return numMoles;
    }

    /**
     * @return The initial interval in milliseconds between mole appearances.
     */
    public long getInitialInterval() {
        return initialInterval;
    }

    /**
     * @return The minimum interval in milliseconds between mole appearances.
     */
    public long getMinInterval() {
        return minInterval;
    }

    /**
     * @return The amount of time in milliseconds to decrease the spawn interval by.
     */
    public long getIntervalDecrement() {
        return intervalDecrement;
    }

    /**
     * @return The total duration of the game in milliseconds.
     */
    public long getGameDurationMillis() {
        return gameDurationMillis;
    }
}
