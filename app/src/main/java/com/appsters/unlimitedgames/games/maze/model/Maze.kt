package com.appsters.unlimitedgames.games.maze.model

import kotlin.random.Random
import com.appsters.unlimitedgames.games.maze.model.MazeItem

data class Cell(
    val col: Int, val row: Int,
    var topWall: Boolean = true,
    var bottomWall: Boolean = true,
    var leftWall: Boolean = true,
    var rightWall: Boolean = true,
    var isVisited: Boolean = false
)

class Maze(val width: Int, val height: Int) {
    val cells = Array(height) { row ->
        Array(width) { col ->
            Cell(col, row)
        }
    }

    val items = mutableListOf<MazeItem>()

    private val stack = mutableListOf<Cell>()

    fun generate() {
        var current = cells[0][0]
        current.isVisited = true
        var visitedCount = 1

        while (visitedCount < width * height) {
            val neighbors = getNonVisitedNeighbors(current)
            if (neighbors.isNotEmpty()) {
                stack.add(current)
                val neighbor = neighbors[Random.nextInt(neighbors.size)]
                removeWall(current, neighbor)
                current = neighbor
                current.isVisited = true
                visitedCount++
            } else if (stack.isNotEmpty()) {
                current = stack.removeLast()
            }
        }
    }

    private fun getNonVisitedNeighbors(cell: Cell): List<Cell> {
        val neighbors = mutableListOf<Cell>()

        // Top neighbor
        if (cell.row > 0) {
            val top = cells[cell.row - 1][cell.col]
            if (!top.isVisited) {
                neighbors.add(top)
            }
        }

        // Bottom neighbor
        if (cell.row < height - 1) {
            val bottom = cells[cell.row + 1][cell.col]
            if (!bottom.isVisited) {
                neighbors.add(bottom)
            }
        }

        // Left neighbor
        if (cell.col > 0) {
            val left = cells[cell.row][cell.col - 1]
            if (!left.isVisited) {
                neighbors.add(left)
            }
        }

        // Right neighbor
        if (cell.col < width - 1) {
            val right = cells[cell.row][cell.col + 1]
            if (!right.isVisited) {
                neighbors.add(right)
            }
        }

        return neighbors
    }

    private fun removeWall(current: Cell, neighbor: Cell) {
        // Neighbor is top
        if (current.row == neighbor.row + 1) {
            current.topWall = false
            neighbor.bottomWall = false
        }

        // Neighbor is bottom
        if (current.row == neighbor.row - 1) {
            current.bottomWall = false
            neighbor.topWall = false
        }

        // Neighbor is left
        if (current.col == neighbor.col + 1) {
            current.leftWall = false
            neighbor.rightWall = false
        }

        // Neighbor is right
        if (current.col == neighbor.col - 1) {
            current.rightWall = false
            neighbor.leftWall = false
        }
    }
}
