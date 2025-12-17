package com.appsters.unlimitedgames.games.poker.repo;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Stores Poker-specific state per signed-in user.
 *
 * Coins are used as the player's score for Poker and can be regenerated via "free coins".
 *
 * Free coins rules:
 * - First time a user opens Poker, they get 100 coins immediately.
 * - Then 10 coins become claimable every 2.5 hours.
 * - If the 10 coins are available but not claimed, the timer for the next grant does NOT start.
 */
public class RandPokerRepository {

    private static final String PREFS_NAME = "PokerPrefs";

    private static final String COINS_KEY_PREFIX = "coins_";
    private static final String NEXT_FREE_AT_KEY_PREFIX = "next_free_at_";
    private static final String FREE_AVAILABLE_KEY_PREFIX = "free_available_";
    private static final String INITIALIZED_KEY_PREFIX = "initialized_";

    // 2.5 hours in millis
    public static final long FREE_COINS_INTERVAL_MS = (long) (2.5 * 60 * 60 * 1000);
    public static final int INITIAL_COINS = 100;
    public static final int FREE_COINS_AMOUNT = 10;

    private final SharedPreferences sharedPreferences;

    public RandPokerRepository(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    @Nullable
    private FirebaseUser getCurrentUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    private String key(String prefix) {
        FirebaseUser user = getCurrentUser();
        if (user == null) return null;
        return prefix + user.getUid();
    }

    /**
     * Ensures the user has initial state. Safe to call repeatedly.
     */
    public void ensureInitialized() {
        String initKey = key(INITIALIZED_KEY_PREFIX);
        String coinsKey = key(COINS_KEY_PREFIX);
        String nextKey = key(NEXT_FREE_AT_KEY_PREFIX);
        String availKey = key(FREE_AVAILABLE_KEY_PREFIX);
        if (initKey == null || coinsKey == null || nextKey == null || availKey == null) return;

        boolean initialized = sharedPreferences.getBoolean(initKey, false);
        if (!initialized) {
            long now = System.currentTimeMillis();
            sharedPreferences.edit()
                    .putBoolean(initKey, true)
                    .putInt(coinsKey, INITIAL_COINS)
                    // Start the first timer immediately; user must wait 2.5h for the next 10 coins
                    .putLong(nextKey, now + FREE_COINS_INTERVAL_MS)
                    .putBoolean(availKey, false)
                    .apply();
        } else {
            // Update availability if timer has elapsed (but do NOT move the next time if already available)
            refreshFreeCoinsAvailability();
        }
    }

    public int getCoins() {
        String coinsKey = key(COINS_KEY_PREFIX);
        if (coinsKey == null) return 0;
        ensureInitialized();
        return sharedPreferences.getInt(coinsKey, 0);
    }

    public void setCoins(int coins) {
        String coinsKey = key(COINS_KEY_PREFIX);
        if (coinsKey == null) return;
        ensureInitialized();
        sharedPreferences.edit().putInt(coinsKey, Math.max(0, coins)).apply();
    }

    public void addCoins(int delta) {
        int current = getCoins();
        setCoins(current + delta);
    }

    public boolean canClaimFreeCoins() {
        String availKey = key(FREE_AVAILABLE_KEY_PREFIX);
        if (availKey == null) return false;
        ensureInitialized();
        refreshFreeCoinsAvailability();
        return sharedPreferences.getBoolean(availKey, false);
    }

    public long getNextFreeCoinsAtMs() {
        String nextKey = key(NEXT_FREE_AT_KEY_PREFIX);
        if (nextKey == null) return Long.MAX_VALUE;
        ensureInitialized();
        refreshFreeCoinsAvailability();
        return sharedPreferences.getLong(nextKey, Long.MAX_VALUE);
    }

    /**
     * Claims the free coins if available. Returns true if coins were granted.
     */
    public boolean claimFreeCoins() {
        String availKey = key(FREE_AVAILABLE_KEY_PREFIX);
        String nextKey = key(NEXT_FREE_AT_KEY_PREFIX);
        String coinsKey = key(COINS_KEY_PREFIX);
        if (availKey == null || nextKey == null || coinsKey == null) return false;

        ensureInitialized();
        refreshFreeCoinsAvailability();

        boolean available = sharedPreferences.getBoolean(availKey, false);
        if (!available) return false;

        long now = System.currentTimeMillis();
        int currentCoins = sharedPreferences.getInt(coinsKey, 0);

        sharedPreferences.edit()
                .putInt(coinsKey, currentCoins + FREE_COINS_AMOUNT)
                .putBoolean(availKey, false)
                .putLong(nextKey, now + FREE_COINS_INTERVAL_MS)
                .apply();
        return true;
    }

    /**
     * If the timer elapsed and coins are not marked available yet, mark them available.
     * IMPORTANT: If already available, do nothing so the "next timer" does not start.
     */
    private void refreshFreeCoinsAvailability() {
        String availKey = key(FREE_AVAILABLE_KEY_PREFIX);
        String nextKey = key(NEXT_FREE_AT_KEY_PREFIX);
        if (availKey == null || nextKey == null) return;

        boolean available = sharedPreferences.getBoolean(availKey, false);
        if (available) return;

        long nextAt = sharedPreferences.getLong(nextKey, Long.MAX_VALUE);
        long now = System.currentTimeMillis();
        if (now >= nextAt) {
            sharedPreferences.edit().putBoolean(availKey, true).apply();
        }
    }

    public void clearAllState() {
        String initKey = key(INITIALIZED_KEY_PREFIX);
        String coinsKey = key(COINS_KEY_PREFIX);
        String nextKey = key(NEXT_FREE_AT_KEY_PREFIX);
        String availKey = key(FREE_AVAILABLE_KEY_PREFIX);
        if (initKey == null || coinsKey == null || nextKey == null || availKey == null) return;

        sharedPreferences.edit()
                .remove(initKey)
                .remove(coinsKey)
                .remove(nextKey)
                .remove(availKey)
                .apply();
    }
}
