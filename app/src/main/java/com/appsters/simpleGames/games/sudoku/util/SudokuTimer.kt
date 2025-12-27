package com.appsters.simpleGames.games.sudoku.util

import android.os.Handler
import android.os.Looper

/**
 * A timer class for the Sudoku game.
 * This class uses a [Handler] to provide periodic updates on a background thread.
 * It can be started, paused, resumed, and reset.
 *
 * @param onTick A callback function that is invoked every second, providing the total elapsed time in milliseconds.
 */
class SudokuTimer(
    private val onTick: (Long) -> Unit // Callback with elapsed time in ms
) {
    private val handler = Handler(Looper.getMainLooper())
    private var startTime: Long = 0L
    private var elapsedTime: Long = 0L
    private var isRunning = false
    
    private val tickRunnable = object : Runnable {
        override fun run() {
            if (isRunning) {
                elapsedTime = System.currentTimeMillis() - startTime
                onTick(elapsedTime)
                handler.postDelayed(this, 1000) // Update every second
            }
        }
    }
    
    /**
     * Starts the timer if it is not already running.
     */
    fun start() {
        if (!isRunning) {
            startTime = System.currentTimeMillis() - elapsedTime
            isRunning = true
            handler.post(tickRunnable)
        }
    }
    
    /**
     * Pauses the timer.
     */
    fun pause() {
        isRunning = false
        handler.removeCallbacks(tickRunnable)
    }
    
    /**
     * Resumes the timer from where it was paused.
     */
    fun resume() {
        start()
    }
    
    /**
     * Stops the timer and resets the elapsed time to zero.
     */
    fun reset() {
        pause()
        elapsedTime = 0L
        onTick(0L)
    }
    
    /**
     * Gets the current elapsed time in milliseconds.
     */
    fun getElapsedTime(): Long = elapsedTime
    
    /**
     * Sets the elapsed time to a specific value.
     */
    fun setElapsedTime(time: Long) {
        elapsedTime = time
    }
    
    /**
     * Checks if the timer is currently running.
     */
    fun isRunning(): Boolean = isRunning
}
