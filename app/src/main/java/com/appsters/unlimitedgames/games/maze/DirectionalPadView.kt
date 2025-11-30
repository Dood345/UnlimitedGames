package com.appsters.unlimitedgames.games.maze

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.appsters.unlimitedgames.R
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

class DirectionalPadView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    interface OnDirectionalPadListener {
        fun onDirectionChanged(dx: Int, dy: Int)
    }

    var listener: OnDirectionalPadListener? = null

    private var upPressed = false
    private var downPressed = false
    private var leftPressed = false
    private var rightPressed = false

    private val inactivePaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val activePaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val circleBounds = RectF()

    init {
        inactivePaint.color = ContextCompat.getColor(context, R.color.d_pad_inactive_color)
        activePaint.color = ContextCompat.getColor(context, R.color.d_pad_active_color)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val size = min(w, h).toFloat()
        val padding = size * 0.1f
        circleBounds.set(padding, padding, size - padding, size - padding)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Draw the four arcs, now correctly mapped to their directions
        canvas.drawArc(circleBounds, 225f, 90f, true, if (upPressed) activePaint else inactivePaint)
        canvas.drawArc(circleBounds, 315f, 90f, true, if (rightPressed) activePaint else inactivePaint)
        canvas.drawArc(circleBounds, 45f, 90f, true, if (downPressed) activePaint else inactivePaint)
        canvas.drawArc(circleBounds, 135f, 90f, true, if (leftPressed) activePaint else inactivePaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        val wasUpPressed = upPressed
        val wasDownPressed = downPressed
        val wasLeftPressed = leftPressed
        val wasRightPressed = rightPressed

        when (event.action) {
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_MOVE -> {
                val centerX = width / 2f
                val centerY = height / 2f
                val radius = min(width, height) / 2f

                val dxTouch = x - centerX
                val dyTouch = y - centerY

                val distance = sqrt(dxTouch.pow(2) + dyTouch.pow(2))

                if (distance < radius) {
                    val angle = (Math.toDegrees(atan2(dyTouch.toDouble(), dxTouch.toDouble())) + 360) % 360

                    upPressed = angle > 225 && angle < 315
                    downPressed = angle > 45 && angle < 135
                    leftPressed = angle > 135 && angle < 225
                    rightPressed = angle > 315 || angle < 45
                } else {
                    upPressed = false
                    downPressed = false
                    leftPressed = false
                    rightPressed = false
                }

                var dx = 0
                var dy = 0

                if (upPressed) dy = -1
                if (downPressed) dy = 1
                if (leftPressed) dx = -1
                if (rightPressed) dx = 1

                listener?.onDirectionChanged(dx, dy)

                if (wasUpPressed != upPressed || wasDownPressed != downPressed || wasLeftPressed != leftPressed || wasRightPressed != rightPressed) {
                    invalidate()
                }

                return true
            }
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                upPressed = false
                downPressed = false
                leftPressed = false
                rightPressed = false
                listener?.onDirectionChanged(0, 0)
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}
