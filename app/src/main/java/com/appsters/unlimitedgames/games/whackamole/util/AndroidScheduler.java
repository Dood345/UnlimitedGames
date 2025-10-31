package com.appsters.unlimitedgames.games.whackamole.util;

import android.os.Handler;
import android.os.Looper;

import com.example.whackamole.util.Scheduler;

/**
 * AndroidScheduler uses Handler and Looper to post delayed tasks
 * on the main thread.
 */
public class AndroidScheduler implements Scheduler {

    private final Handler handler;

    /**
     * Creates a scheduler using the given Looper.
     * Typically, pass Looper.getMainLooper() for UI tasks.
     *
     * @param looper Looper to attach the Handler to
     */
    public AndroidScheduler(Looper looper) {
        this.handler = new Handler(looper);
    }

    /**
     * Post a Runnable to be executed after the specified delay.
     *
     * @param runnable    Runnable task
     * @param delayMillis delay in milliseconds
     */
    @Override
    public void postDelayed(Runnable runnable, long delayMillis) {
        handler.postDelayed(runnable, delayMillis);
    }

    /**
     * Remove pending executions of the given Runnable.
     *
     * @param runnable Runnable task
     */
    @Override
    public void removeCallbacks(Runnable runnable) {
        handler.removeCallbacks(runnable);
    }

    /**
     * Remove all pending executions of the given Runnable.
     *
     * @param runnable Runnable task
     */
    @Override
    public void removeCallbacksAndMessages(Runnable runnable) {
        handler.removeCallbacksAndMessages(runnable);
    }
}
