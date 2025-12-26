package com.appsters.simpleGames.app.util

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class TypewriterView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatTextView(context, attrs) {

    private var textToAnimate: CharSequence = ""
    private var currentIndex = 0
    private var characterDelay: Long = 50 // Default delay in milliseconds

    private val handler = Handler(Looper.getMainLooper())

    private val characterAdder = object : Runnable {
        override fun run() {
            text = textToAnimate.subSequence(0, currentIndex++)
            com.appsters.simpleGames.app.util.SoundManager.playSound(com.appsters.simpleGames.R.raw.typewriter)
            if (currentIndex <= textToAnimate.length) {
                handler.postDelayed(this, characterDelay)
            }
        }
    }

    fun animateText(text: CharSequence) {
        textToAnimate = text
        currentIndex = 0
        com.appsters.simpleGames.app.util.SoundManager.init(context)
        setText("") // Clear the text view before starting
        handler.removeCallbacks(characterAdder)
        handler.postDelayed(characterAdder, characterDelay)
    }

    fun setCharacterDelay(millis: Long) {
        characterDelay = millis
    }
}