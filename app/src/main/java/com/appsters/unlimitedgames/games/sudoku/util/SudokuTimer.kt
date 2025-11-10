package com.appsters.unlimitedgames.games.sudoku.util

import android.os.Handler
import android.os.Looper

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

    fun start() {
        if (!isRunning) {
            startTime = System.currentTimeMillis() - elapsedTime
            isRunning = true
            handler.post(tickRunnable)
        }
    }

    fun pause() {
        isRunning = false
        handler.removeCallbacks(tickRunnable)
    }

    fun resume() {
        start()
    }

    fun reset() {
        pause()
        elapsedTime = 0L
        onTick(0L)
    }

    fun getElapsedTime(): Long = elapsedTime

    fun setElapsedTime(time: Long) {
        elapsedTime = time
    }

    fun isRunning(): Boolean = isRunning
}