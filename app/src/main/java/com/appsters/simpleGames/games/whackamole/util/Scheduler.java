package com.appsters.simpleGames.games.whackamole.util;

/**
 * An interface that abstracts the Android Handler class to allow for easier testing.
 * Implementations of this interface can either use a real Android Handler or a fake scheduler
 * for unit tests, enabling control over timing-dependent logic.
 */
public interface Scheduler {
    /**
     * Causes the Runnable r to be added to the message queue, to be run after the specified
     * amount of time elapses.
     *
     * @param r The Runnable that will be executed.
     * @param delayMs The delay (in milliseconds) until the Runnable will be executed.
     */
    void postDelayed(Runnable r, long delayMs);

    /**
     * Remove any pending posts of Runnable r that are in the message queue.
     *
     * @param r The Runnable to remove from the queue.
     */
    void removeCallbacks(Runnable r);

    /**
     * Remove any pending posts of callbacks and sent messages whose obj is token. If token is null,
     * all callbacks and messages will be removed.
     *
     * @param r The Runnable to remove from the queue (can be null).
     */
    void removeCallbacksAndMessages(Runnable r);
}
