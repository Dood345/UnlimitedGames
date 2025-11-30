package com.appsters.unlimitedgames.games.sudoku.model

import com.appsters.unlimitedgames.games.sudoku.SudokuMenuFragment
/**
 * An object responsible for generating Sudoku puzzles.
 * Uses a backtracking algorithm to create valid, solvable puzzles.
 */
object PuzzleGenerator {

    /**
     * Generates a new Sudoku board based on the specified difficulty.
     *
     * @param difficulty The desired difficulty for the puzzle.
     * @return A [Board] object containing the generated puzzle.
     */
    fun generate(difficulty: SudokuMenuFragment.Difficulty): Board {
        return generatePuzzle(difficulty)
    }

    /**
     * Generates a new puzzle using backtracking algorithm.
     * 1. Creates a complete valid solution
     * 2. Removes cells based on difficulty
     * 3. Ensures the puzzle has a unique solution
     */
    private fun generatePuzzle(difficulty: SudokuMenuFragment.Difficulty): Board {
        // Create a solved board first
        val board = Board()
        fillBoard(board)

        // Remove cells based on difficulty
        val cellsToRemove = 81 - difficulty.givens
        removeCells(board, cellsToRemove)

        return board
    }

    /**
     * Fills the board with a valid complete Sudoku solution using backtracking.
     */
    private fun fillBoard(board: Board): Boolean {
        // Find next empty cell
        for (row in 0..8) {
            for (col in 0..8) {
                if (board.getCell(row, col).value == 0) {
                    // Try numbers 1-9 in random order
                    val numbers = (1..9).shuffled()

                    for (num in numbers) {
                        if (board.isValid(row, col, num)) {
                            board.setCell(row, col, num)
                            // DON'T set isFixed here - we're just solving

                            if (fillBoard(board)) {
                                return true
                            }

                            // Backtrack
                            board.setCell(row, col, 0)
                        }
                    }
                    return false
                }
            }
        }
        return true // Board is complete
    }

    /**
     * Removes cells from the board while ensuring the puzzle remains solvable
     * and has a unique solution.
     */
    private fun removeCells(board: Board, count: Int) {
        // Mark all cells as fixed (part of the puzzle)
        for (row in 0..8) {
            for (col in 0..8) {
                board.cells[row][col].isFixed = true
            }
        }

        // Create a shuffled list of all 81 cell coordinates
        // This ensures we try each cell exactly once in random order
        val cells = (0..80).shuffled()
        var removed = 0

        for (cellId in cells) {
            // Early exit: stop as soon as we've removed enough cells
            if (removed >= count) break

            val row = cellId / 9
            val col = cellId % 9
            val currentCell = board.getCell(row, col)

            // Save the value
            val backup = currentCell.value

            // Try removing it
            board.cells[row][col].isFixed = false
            board.setCell(row, col, 0)

            // Check if puzzle still has unique solution
            if (hasUniqueSolution(board)) {
                removed++
            } else {
                // Restore the value and keep it fixed
                board.setCell(row, col, backup)
                board.cells[row][col].isFixed = true
            }
        }
    }

    /**
     * Checks if the board has exactly one unique solution.
     * Uses a modified backtracking solver that counts solutions.
     */
    private fun hasUniqueSolution(board: Board): Boolean {
        val boardCopy = board.copy()
        val solutionCount = countSolutions(boardCopy, 2) // Stop after finding 2
        return solutionCount == 1
    }

    /**
     * Counts the number of solutions for the given board.
     * Stops counting after reaching maxCount for efficiency.
     */
    private fun countSolutions(board: Board, maxCount: Int): Int {
        return countSolutionsHelper(board, 0, maxCount)
    }

    private fun countSolutionsHelper(board: Board, currentCount: Int, maxCount: Int): Int {
        if (currentCount >= maxCount) return currentCount

        // Find next empty cell
        for (row in 0..8) {
            for (col in 0..8) {
                if (board.getCell(row, col).value == 0) {
                    var count = currentCount

                    for (num in 1..9) {
                        if (board.isValid(row, col, num)) {
                            board.setCell(row, col, num)
                            count = countSolutionsHelper(board, count, maxCount)
                            board.setCell(row, col, 0)

                            if (count >= maxCount) return count
                        }
                    }
                    return count
                }
            }
        }
        // Found a complete solution
        return currentCount + 1
    }

    /**
     * Solves the board using backtracking (used for validation).
     * Returns true if a solution exists.
     */
    fun solve(board: Board): Boolean {
        for (row in 0..8) {
            for (col in 0..8) {
                if (board.getCell(row, col).value == 0) {
                    for (num in 1..9) {
                        if (board.isValid(row, col, num)) {
                            board.setCell(row, col, num)

                            if (solve(board)) {
                                return true
                            }

                            board.setCell(row, col, 0)
                        }
                    }
                    return false
                }
            }
        }
        return true
    }

    /**
     * Creates a sample easy puzzle (fallback for testing).
     */
    private fun createSampleEasyPuzzle(): Board {
        val board = Board()
        val puzzle = arrayOf(
            intArrayOf(5, 3, 0, 0, 7, 0, 0, 0, 0),
            intArrayOf(6, 0, 0, 1, 9, 5, 0, 0, 0),
            intArrayOf(0, 9, 8, 0, 0, 0, 0, 6, 0),
            intArrayOf(8, 0, 0, 0, 6, 0, 0, 0, 3),
            intArrayOf(4, 0, 0, 8, 0, 3, 0, 0, 1),
            intArrayOf(7, 0, 0, 0, 2, 0, 0, 0, 6),
            intArrayOf(0, 6, 0, 0, 0, 0, 2, 8, 0),
            intArrayOf(0, 0, 0, 4, 1, 9, 0, 0, 5),
            intArrayOf(0, 0, 0, 0, 8, 0, 0, 7, 9)
        )

        return fillBoardFromArray(board, puzzle)
    }

    /**
     * Fills a board from a 2D integer array.
     * A value of 0 is treated as an empty cell, while any other value is a fixed cell.
     */
    private fun fillBoardFromArray(board: Board, puzzle: Array<IntArray>): Board {
        for (row in 0..8) {
            for (col in 0..8) {
                val value = puzzle[row][col]
                if (value != 0) {
                    board.cells[row][col] = Cell(row, col, value, isFixed = true)
                }
            }
        }
        return board
    }
}