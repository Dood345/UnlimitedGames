package com.appsters.unlimitedgames.games.maze

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.appsters.unlimitedgames.R
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

class DirectionalPadView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    interface OnDirectionalPadListener {
        fun onDirectionChanged(dx: Int, dy: Int)
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
        joystickRadius = minDim * 0.4f
        thumbRadius = minDim * 0.15f
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
                listener?.onDirectionChanged(0, 0)
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun updateDirection() {
        val dx = thumbPos.x - center.x
        val dy = thumbPos.y - center.y
        val distance = sqrt(dx * dx + dy * dy)

        // Deadzone
        if (distance < joystickRadius * 0.2f) {
            listener?.onDirectionChanged(0, 0)
            return
        }

        val angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble()))
        // Map angle to 4 directions
        // Right: -45 to 45
        // Down: 45 to 135
        // Left: 135 to 180, -180 to -135
        // Up: -135 to -45

        var dX = 0
        var dY = 0

        if (angle >= -45 && angle < 45) {
            dX = 1
        } else if (angle >= 45 && angle < 135) {
            dY = 1
        } else if (angle >= 135 || angle < -135) {
            dX = -1
        } else if (angle >= -135 && angle < -45) {
            dY = -1
        }

        listener?.onDirectionChanged(dX, dY)
    }
}
