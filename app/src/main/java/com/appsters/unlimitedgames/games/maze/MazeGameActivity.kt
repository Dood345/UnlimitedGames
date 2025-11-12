package com.appsters.unlimitedgames.games.maze

import android.os.Bundle
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.appsters.unlimitedgames.R
import com.appsters.unlimitedgames.games.maze.model.Maze

class MazeGameActivity : AppCompatActivity() {

    private lateinit var mazeView: MazeView
    private lateinit var maze: Maze
    private lateinit var dPad: DirectionalPadView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maze_game)

        mazeView = findViewById(R.id.maze_view)
        dPad = findViewById(R.id.d_pad)

        maze = Maze(15, 15)
        maze.generate()
        mazeView.setMaze(maze)

        dPad.listener = object : DirectionalPadView.OnDirectionalPadListener {
            override fun onDirectionChanged(dx: Int, dy: Int) {
                mazeView.isPressingUp = dy == -1
                mazeView.isPressingDown = dy == 1
                mazeView.isPressingLeft = dx == -1
                mazeView.isPressingRight = dx == 1
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> mazeView.isPressingUp = true
            KeyEvent.KEYCODE_DPAD_DOWN -> mazeView.isPressingDown = true
            KeyEvent.KEYCODE_DPAD_LEFT -> mazeView.isPressingLeft = true
            KeyEvent.KEYCODE_DPAD_RIGHT -> mazeView.isPressingRight = true
            else -> return super.onKeyDown(keyCode, event)
        }
        return true
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> mazeView.isPressingUp = false
            KeyEvent.KEYCODE_DPAD_DOWN -> mazeView.isPressingDown = false
            KeyEvent.KEYCODE_DPAD_LEFT -> mazeView.isPressingLeft = false
            KeyEvent.KEYCODE_DPAD_RIGHT -> mazeView.isPressingRight = false
            else -> return super.onKeyUp(keyCode, event)
        }
        return true
    }
}
