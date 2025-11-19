package com.appsters.unlimitedgames.games.maze

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.appsters.unlimitedgames.R
import com.appsters.unlimitedgames.games.maze.model.Maze
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
    var isPressingUp = false
    var isPressingDown = false
    var isPressingLeft = false
    var isPressingRight = false

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

    private val gameLoop = object : Runnable {
        override fun run() {
            update()
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

        removeCallbacks(gameLoop)
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
    private var lastCol = 0
    private var lastRow = 0

    private fun update() {
        val currentMaze = maze ?: return

        // Apply input
        if (isPressingUp) playerVY -= acceleration
        if (isPressingDown) playerVY += acceleration
        if (isPressingLeft) playerVX -= acceleration
        if (isPressingRight) playerVX += acceleration

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
            removeCallbacks(gameLoop) // Stop game
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

            // Draw maze walls
            for (row in 0 until mazeHeight) {
                for (col in 0 until mazeWidth) {
                    val cell = maze.cells[row][col]
                    val x1 = col * cellSize
                    val y1 = row * cellSize
                    val x2 = (col + 1) * cellSize
                    val y2 = (row + 1) * cellSize

                    if (cell.topWall) canvas.drawLine(x1, y1, x2, y1, wallPaint)
                    if (cell.bottomWall) canvas.drawLine(x1, y2, x2, y2, wallPaint)
                    if (cell.leftWall) canvas.drawLine(x1, y1, x1, y2, wallPaint)
                    if (cell.rightWall) canvas.drawLine(x2, y1, x2, y2, wallPaint)
                }
            }

            // Draw player
            val playerDrawX = playerX * cellSize
            val playerDrawY = playerY * cellSize
            canvas.drawCircle(playerDrawX, playerDrawY, cellSize / 3, playerPaint)

            // Draw exit
            val exitCX = (exitCol + 0.5f) * cellSize
            val exitCY = (exitRow + 0.5f) * cellSize
            canvas.drawCircle(exitCX, exitCY, cellSize / 3, exitPaint)
        }
    }
}
