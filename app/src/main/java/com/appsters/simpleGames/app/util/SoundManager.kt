package com.appsters.simpleGames.app.util

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.appsters.simpleGames.R

/**
 * Singleton object to manage sound effects across the application.
 * Uses SoundPool for low-latency playback.
 */
object SoundManager {

    private var soundPool: SoundPool? = null
    private val soundMap = HashMap<Int, Int>()
    private val volumeMap = HashMap<Int, Float>()
    private var isInitialized = false

    @JvmStatic
    fun init(context: Context) {
        if (isInitialized) return

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        // Preload sounds with specific volumes
        loadSound(context, R.raw.mole_hit, 0.92f) // -0.69dB
        loadSound(context, R.raw.slide, 0.89f)    // -1dB
        loadSound(context, R.raw.typewriter, 0.89f) // -1dB
        loadSound(context, R.raw.win, 0.79f)      // -2dB

        isInitialized = true
    }

    private fun loadSound(context: Context, resourceId: Int, volume: Float = 1.0f) {
        soundPool?.let { pool ->
            val soundId = pool.load(context, resourceId, 1)
            soundMap[resourceId] = soundId
            volumeMap[resourceId] = volume
        }
    }

    /**
     * Plays a sound if not muted.
     * @param resourceId The resource ID of the sound to play.
     * @param isMuted Whether the game is currently muted.
     */
    @JvmStatic
    fun playSound(resourceId: Int, isMuted: Boolean) {
        if (isMuted) return
        val soundId = soundMap[resourceId] ?: return
        val volume = volumeMap[resourceId] ?: 1.0f
        soundPool?.play(soundId, volume, volume, 0, 0, 1f)
    }

    /**
     * Toggles the mute state in the provided SharedPreferences.
     * @param prefs The SharedPreferences to store the mute state.
     * @return The new mute state (true if muted, false otherwise).
     */
    @JvmStatic
    fun toggleMute(prefs: android.content.SharedPreferences): Boolean {
        val isMuted = !prefs.getBoolean("is_muted", false)
        prefs.edit().putBoolean("is_muted", isMuted).apply()
        return isMuted
    }

    /**
     * Checks if the game is muted in the provided SharedPreferences.
     */
    @JvmStatic
    fun isMuted(prefs: android.content.SharedPreferences): Boolean {
        return prefs.getBoolean("is_muted", false)
    }

    @JvmStatic
    fun release() {
        soundPool?.release()
        soundPool = null
        soundMap.clear()
        volumeMap.clear()
        isInitialized = false
    }
}
