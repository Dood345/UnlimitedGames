package com.appsters.simpleGames.app.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.appsters.simpleGames.R
import kotlin.math.min
import kotlin.math.sqrt

class DirectionalPadView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    interface OnDirectionalPadListener {
        fun onJoystickMoved(xPercent: Float, yPercent: Float)
    }

    var listener: OnDirectionalPadListener? = null

    // Joystick State
    private val center = PointF()
    private val thumbPos = PointF()
    private var isTouching = false
    private var joystickRadius = 0f
    private var thumbRadius = 0f

    // Paints
    private val basePaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = ContextCompat.getColor(context, R.color.d_pad_inactive_color)
        alpha = 100 // Semi-transparent base
    }

    private val thumbPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = ContextCompat.getColor(context, R.color.d_pad_active_color)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        center.set(w / 2f, h / 2f)
        thumbPos.set(center)
        val minDim = min(w, h).toFloat()
        // Reduced radii to prevent clipping
        // Total extent = 0.35 + 0.12 = 0.47 < 0.5
        joystickRadius = minDim * 0.35f
        thumbRadius = minDim * 0.12f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Draw Base
        canvas.drawCircle(center.x, center.y, joystickRadius, basePaint)

        // Draw Thumb
        canvas.drawCircle(thumbPos.x, thumbPos.y, thumbRadius, thumbPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_MOVE -> {
                isTouching = true
                val dx = event.x - center.x
                val dy = event.y - center.y
                val distance = sqrt(dx * dx + dy * dy)

                if (distance < joystickRadius) {
                    thumbPos.set(event.x, event.y)
                } else {
                    // Clamp to radius
                    val ratio = joystickRadius / distance
                    thumbPos.set(center.x + dx * ratio, center.y + dy * ratio)
                }

                updateDirection()
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                isTouching = false
                thumbPos.set(center)
                listener?.onJoystickMoved(0f, 0f)
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun updateDirection() {
        val dx = thumbPos.x - center.x
        val dy = thumbPos.y - center.y

        // Normalize to -1.0 to 1.0 based on joystickRadius
        // This allows for analog control (variable speed if we want)
        val xPercent = (dx / joystickRadius).coerceIn(-1f, 1f)
        val yPercent = (dy / joystickRadius).coerceIn(-1f, 1f)

        listener?.onJoystickMoved(xPercent, yPercent)
    }
}