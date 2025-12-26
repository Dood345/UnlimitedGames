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

        // Preload sounds
        loadSound(context, R.raw.mole_hit)
        loadSound(context, R.raw.slide)
        loadSound(context, R.raw.typewriter)
        loadSound(context, R.raw.win)

        isInitialized = true
    }

    private fun loadSound(context: Context, resourceId: Int) {
        soundPool?.let { pool ->
            val soundId = pool.load(context, resourceId, 1)
            soundMap[resourceId] = soundId
        }
    }

    @JvmStatic
    fun playSound(resourceId: Int) {
        val soundId = soundMap[resourceId] ?: return
        soundPool?.play(soundId, 1f, 1f, 0, 0, 1f)
    }

    @JvmStatic
    fun release() {
        soundPool?.release()
        soundPool = null
        soundMap.clear()
        isInitialized = false
    }
}
