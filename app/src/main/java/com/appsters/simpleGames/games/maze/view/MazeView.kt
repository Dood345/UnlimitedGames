package com.appsters.simpleGames.games.maze.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RadialGradient
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.appsters.simpleGames.R
import com.appsters.simpleGames.games.maze.model.Maze
import com.appsters.simpleGames.games.maze.model.MazeItem
import com.appsters.simpleGames.games.maze.model.PowerUpType
import java.util.LinkedList
import kotlin.math.abs
import kotlin.math.sqrt

class MazeView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var maze: Maze? = null

    // Player position, velocity
    var playerX = 0.5f
        private set
    var playerY = 0.5f
        private set
    private var playerVX = 0f
    private var playerVY = 0f

    // Input state
    var inputX = 0f
    var inputY = 0f

    private var exitCol = 0
    private var exitRow = 0

    // Paint objects
    private val wallPaint = Paint().apply { strokeWidth = 5f }
    private val playerPaint = Paint()
    private val exitPaint = Paint()

    // Layout variables
    private var cellSize = 0f
    private var hMargin = 0f
    private var vMargin = 0f

    // Physics variables (Dynamic)
    var maxSpeed = 0.2f
    var acceleration = 0.02f
    private val FRICTION = 0.94f

    private var lastFrameTime = 0L

    private val gameLoop = object : Runnable {
        override fun run() {
            val now = System.currentTimeMillis()
            val dt = if (lastFrameTime > 0) now - lastFrameTime else 16L
            lastFrameTime = now

            update(dt)
            invalidate()
            postOnAnimation(this)
        }
    }

    init {
        wallPaint.color = ContextCompat.getColor(context, R.color.maze_wall_color)
        playerPaint.color = ContextCompat.getColor(context, R.color.maze_player_color)
        exitPaint.color = ContextCompat.getColor(context, R.color.maze_exit_color)
    }

    fun setMaze(maze: Maze) {
        this.maze = maze
        playerX = 0.5f
        playerY = 0.5f
        playerVX = 0f
        playerVY = 0f
        exitCol = maze.width - 1
        exitRow = maze.height - 1
        lastCol = playerX.toInt()
        lastRow = playerY.toInt()
        
        replayBuffer.clear()
        isRewinding = false
        onRewindComplete = null

        removeCallbacks(gameLoop)
        lastFrameTime = 0L
        postOnAnimation(gameLoop)

        invalidate()
    }

    fun setPlayerPosition(x: Float, y: Float) {
        playerX = x
        playerY = y
        invalidate()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (maze != null) {
            removeCallbacks(gameLoop)
            lastFrameTime = 0L
            postOnAnimation(gameLoop)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeCallbacks(gameLoop)
    }

    fun stopGame() {
        removeCallbacks(gameLoop)
    }

    // Listener for tile changes (steps)
    var onTileChangedListener: (() -> Unit)? = null
    var onUpdateListener: ((dt: Long) -> Unit)? = null
    var onWallCollisionListener: ((col: Int, row: Int, wallType: Int) -> Unit)? = null // 0=Top, 1=Bottom, 2=Left, 3=Right
    private var lastCol = 0
    private var lastRow = 0

    // Visual Mechanics
    var visibilityRadius = 4 // In tiles
    private val trailPoints = LinkedList<PointF>()
    private val maxTrailLength = 20
    private val trailPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.maze_player_color)
        strokeWidth = 10f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val artifactPaint = Paint().apply { color = Color.YELLOW }
    private val xpPaint = Paint().apply { color = Color.parseColor("#00FFFF") } // Cyan for XP
    private val speedPaint = Paint().apply { color = Color.RED }
    private val visionPaint = Paint().apply { color = Color.MAGENTA } // Purple-ish
    private val staminaPaint = Paint().apply { color = Color.GREEN }
    private val fogPaint = Paint().apply { color = Color.BLACK } // Fog color
    private val vignettePaint = Paint()

    // Rewind State
    private val replayBuffer = ArrayList<PointF>()
    private var isRewinding = false
    private var onRewindComplete: (() -> Unit)? = null
    private var rewindSpeedMultiplier = 3 // How many frames to skip/process per tick

    private fun update(dt: Long) {
        if (isRewinding) {
            updateRewind()
            return
        }

        onUpdateListener?.invoke(dt)
        val currentMaze = maze ?: return

        // Apply input (Analog)
        // Deadzone check is done in Joystick, but we can double check
        if (abs(inputX) > 0.01f) playerVX += inputX * acceleration
        if (abs(inputY) > 0.01f) playerVY += inputY * acceleration

        // Clamp to max speed
        val speed = sqrt(playerVX * playerVX + playerVY * playerVY)
        if (speed > maxSpeed) {
            playerVX = (playerVX / speed) * maxSpeed
            playerVY = (playerVY / speed) * maxSpeed
        }

        // Apply friction
        playerVX *= FRICTION
        playerVY *= FRICTION

        // Stop movement if velocity is very low
        if (abs(playerVX) < 0.001f) playerVX = 0f
        if (abs(playerVY) < 0.001f) playerVY = 0f

        // Record Path for Rewind
        if (speed > 0.001f || replayBuffer.isEmpty()) {
            // Only record if moving or if it's the first point
            // We can optimize by only adding if distance > threshold
            if (replayBuffer.isEmpty()) {
                replayBuffer.add(PointF(playerX, playerY))
            } else {
                val last = replayBuffer.last()
                val distSq = (playerX - last.x) * (playerX - last.x) + (playerY - last.y) * (playerY - last.y)
                if (distSq > 0.0001f) { // Small threshold
                    replayBuffer.add(PointF(playerX, playerY))
                }
            }
        }

        // Update Trail
        if (speed > 0.01f) {
            trailPoints.add(PointF(playerX, playerY))
            if (trailPoints.size > maxTrailLength) {
                trailPoints.removeFirst()
            }
        } else if (trailPoints.isNotEmpty()) {
            trailPoints.removeFirst() // Fade out when stopped
        }

        // Update Fog of War (Reveal tiles)
        val pCol = playerX.toInt()
        val pRow = playerY.toInt()
        val minCol = (pCol - visibilityRadius).coerceAtLeast(0)
        val maxCol = (pCol + visibilityRadius).coerceAtMost(currentMaze.width - 1)
        val minRow = (pRow - visibilityRadius).coerceAtLeast(0)
        val maxRow = (pRow + visibilityRadius).coerceAtMost(currentMaze.height - 1)

        for (row in minRow..maxRow) {
            for (col in minCol..maxCol) {
                if ((col - pCol) * (col - pCol) + (row - pRow) * (row - pRow) <= visibilityRadius * visibilityRadius) {
                    currentMaze.cells[row][col].isRevealed = true
                }
            }
        }

        if (playerVX == 0f && playerVY == 0f) return

        val playerRadius = 1f / 4f // Slightly smaller radius for collision

        // --- X-Axis Collision ---
        var newX = playerX + playerVX
        val topRow = (playerY - playerRadius).toInt().coerceIn(0, currentMaze.height - 1)
        val bottomRow = (playerY + playerRadius).toInt().coerceIn(0, currentMaze.height - 1)

        if (playerVX > 0) { // Moving Right
            val nextCol = (newX + playerRadius).toInt()
            val currCol = playerX.toInt()
            if (nextCol > currCol && nextCol < currentMaze.width) {
                var collided = false
                for (row in topRow..bottomRow) {
                    if (currentMaze.cells[row][currCol].rightWall) {
                        collided = true
                        onWallCollisionListener?.invoke(currCol, row, 3) // Right Wall
                        break
                    }
                }
                if (collided) {
                    newX = currCol + 1f - playerRadius - 0.01f
                    playerVX = 0f
                }
            }
        } else if (playerVX < 0) { // Moving Left
            val nextCol = (newX - playerRadius).toInt()
            val currCol = playerX.toInt()
            if (nextCol < currCol && nextCol >= 0) {
                var collided = false
                for (row in topRow..bottomRow) {
                    if (currentMaze.cells[row][currCol].leftWall) {
                        collided = true
                        onWallCollisionListener?.invoke(currCol, row, 2) // Left Wall
                        break
                    }
                }
                if (collided) {
                    newX = currCol + playerRadius + 0.01f
                    playerVX = 0f
                }
            }
        }
        playerX = newX.coerceIn(playerRadius, currentMaze.width - playerRadius)

        // --- Y-Axis Collision ---
        var newY = playerY + playerVY
        val leftCol = (playerX - playerRadius).toInt().coerceIn(0, currentMaze.width - 1)
        val rightCol = (playerX + playerRadius).toInt().coerceIn(0, currentMaze.width - 1)

        if (playerVY > 0) { // Moving Down
            val nextRow = (newY + playerRadius).toInt()
            val currRow = playerY.toInt()
            if (nextRow > currRow && nextRow < currentMaze.height) {
                var collided = false
                for (col in leftCol..rightCol) {
                    if (currentMaze.cells[currRow][col].bottomWall) {
                        collided = true
                        onWallCollisionListener?.invoke(col, currRow, 1) // Bottom Wall
                        break
                    }
                }
                if (collided) {
                    newY = currRow + 1f - playerRadius - 0.01f
                    playerVY = 0f
                }
            }
        } else if (playerVY < 0) { // Moving Up
            val nextRow = (newY - playerRadius).toInt()
            val currRow = playerY.toInt()
            if (nextRow < currRow && nextRow >= 0) {
                var collided = false
                for (col in leftCol..rightCol) {
                    if (currentMaze.cells[currRow][col].topWall) {
                        collided = true
                        onWallCollisionListener?.invoke(col, currRow, 0) // Top Wall
                        break
                    }
                }
                if (collided) {
                    newY = currRow + playerRadius + 0.01f
                    playerVY = 0f
                }
            }
        }
        playerY = newY.coerceIn(playerRadius, currentMaze.height - playerRadius)

        // Check for tile change
        val newCol = playerX.toInt()
        val newRow = playerY.toInt()
        if (newCol != lastCol || newRow != lastRow) {
            lastCol = newCol
            lastRow = newRow
            onTileChangedListener?.invoke()
        }

        // Win condition
        if (playerX.toInt() == exitCol && playerY.toInt() == exitRow) {
            // Don't stop game loop here, let Activity handle it via listener
            // removeCallbacks(gameLoop)
        }
    }

    fun startRewind(onComplete: () -> Unit) {
        if (replayBuffer.isEmpty()) {
            onComplete()
            return
        }
        isRewinding = true
        onRewindComplete = onComplete

        // Ensure loop is running
        removeCallbacks(gameLoop)
        postOnAnimation(gameLoop)
    }

    private fun updateRewind() {
        if (replayBuffer.isEmpty()) {
            isRewinding = false
            onRewindComplete?.invoke()
            onRewindComplete = null
            return
        }

        // Process multiple points for speed
        for (i in 0 until rewindSpeedMultiplier) {
            if (replayBuffer.isNotEmpty()) {
                val point = replayBuffer.removeAt(replayBuffer.size - 1)
                playerX = point.x
                playerY = point.y

                // Add to trail for visual effect (reverse trail?)
                // Or just keep existing trail logic?
                // Let's add to trail so it looks like we are moving
                trailPoints.add(PointF(playerX, playerY))
                if (trailPoints.size > maxTrailLength) {
                    trailPoints.removeFirst()
                }
            } else {
                break
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        maze?.let { maze ->
            val mazeWidth = maze.width
            val mazeHeight = maze.height

            cellSize = if (mazeWidth > 0 && mazeHeight > 0) {
                if (width / mazeWidth < height / mazeHeight) {
                    width / (mazeWidth + 1).toFloat()
                } else {
                    height / (mazeHeight + 1).toFloat()
                }
            } else {
                0f
            }

            hMargin = (width - mazeWidth * cellSize) / 2
            vMargin = (height - mazeHeight * cellSize) / 2

            canvas.translate(hMargin, vMargin)

            // Draw maze
            for (row in 0 until mazeHeight) {
                for (col in 0 until mazeWidth) {
                    val cell = maze.cells[row][col]
                    val x1 = col * cellSize
                    val y1 = row * cellSize
                    val x2 = (col + 1) * cellSize
                    val y2 = (row + 1) * cellSize

                    if (!cell.isRevealed) {
                        // Draw Fog
                        canvas.drawRect(x1, y1, x2, y2, fogPaint)
                    } else {
                        // Draw Walls
                        if (cell.topWall) canvas.drawLine(x1, y1, x2, y1, wallPaint)
                        if (cell.bottomWall) canvas.drawLine(x1, y2, x2, y2, wallPaint)
                        if (cell.leftWall) canvas.drawLine(x1, y1, x1, y2, wallPaint)
                        if (cell.rightWall) canvas.drawLine(x2, y1, x2, y2, wallPaint)
                    }
                }
            }

            // Draw Items (Only revealed ones)
            for (item in maze.items) {
                if (maze.cells[item.y][item.x].isRevealed) {
                    val cx = (item.x + 0.5f) * cellSize
                    val cy = (item.y + 0.5f) * cellSize
                    val paint = when (item) {
                        is MazeItem.Artifact -> artifactPaint
                        is MazeItem.XpOrb -> xpPaint
                        is MazeItem.PowerUp -> {
                            when (item.type) {
                                PowerUpType.STAMINA_REFILL -> staminaPaint
                                PowerUpType.SPEED_BOOST -> speedPaint
                                PowerUpType.VISION_EXPAND -> visionPaint
                            }
                        }
                    }

                    canvas.drawRect(cx - cellSize/4, cy - cellSize/4, cx + cellSize/4, cy + cellSize/4, paint)
                }
            }

            // Draw Trail
            if (trailPoints.size > 1) {
                for (i in 0 until trailPoints.size - 1) {
                    val p1 = trailPoints[i]
                    val p2 = trailPoints[i+1]
                    trailPaint.alpha = (255 * (i.toFloat() / trailPoints.size)).toInt()
                    canvas.drawLine(p1.x * cellSize, p1.y * cellSize, p2.x * cellSize, p2.y * cellSize, trailPaint)
                }
            }

            // Draw player
            val playerDrawX = playerX * cellSize
            val playerDrawY = playerY * cellSize
            canvas.drawCircle(playerDrawX, playerDrawY, cellSize / 3, playerPaint)

            // Draw exit (Only if revealed)
            if (maze.cells[exitRow][exitCol].isRevealed) {
                val exitCX = (exitCol + 0.5f) * cellSize
                val exitCY = (exitRow + 0.5f) * cellSize
                canvas.drawCircle(exitCX, exitCY, cellSize / 3, exitPaint)
            }

            // Draw Vignette
            // Radius tracks player vision: Vision Radius - 2
            val vignetteRadius = (visibilityRadius + 0f).coerceAtLeast(1.0f) * cellSize
            val gradient = RadialGradient(
                playerDrawX, playerDrawY,
                vignetteRadius,
                intArrayOf(Color.TRANSPARENT, Color.parseColor("#CC000000")),
                floatArrayOf(0.3f, 1.0f),
                Shader.TileMode.CLAMP
            )
            vignettePaint.shader = gradient
            // Draw rect covering the whole screen (relative to translated canvas)
            canvas.drawRect(-hMargin, -vMargin, width.toFloat() - hMargin, height.toFloat() - vMargin, vignettePaint)
        }
    }
}