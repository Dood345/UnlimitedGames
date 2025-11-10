package com.appsters.unlimitedgames.games.sudoku.model

import com.appsters.unlimitedgames.games.sudoku.SudokuMenuFragment
import kotlin.random.Random

object PuzzleGenerator {

    fun generate(difficulty: SudokuMenuFragment.Difficulty): Board {
        return when (difficulty) {
            SudokuMenuFragment.Difficulty.FREE_PLAY -> createEmptyBoard()
            else -> createPuzzleWithDifficulty(difficulty)
        }
    }

    private fun createEmptyBoard(): Board {
        // Return completely empty board for free play
        return Board()
    }

    private fun createPuzzleWithDifficulty(difficulty: SudokuMenuFragment.Difficulty): Board {
        // TODO: Implement proper puzzle generation with difficulty levels
        // For now, use sample puzzles based on difficulty
        return when (difficulty) {
            SudokuMenuFragment.Difficulty.EASY -> createSampleEasyPuzzle()
            SudokuMenuFragment.Difficulty.MEDIUM -> createSampleMediumPuzzle()
            SudokuMenuFragment.Difficulty.HARD -> createSampleHardPuzzle()
            SudokuMenuFragment.Difficulty.EXPERT -> createSampleExpertPuzzle()
            else -> createSampleEasyPuzzle()
        }
    }

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

    private fun createSampleMediumPuzzle(): Board {
        val board = Board()
        val puzzle = arrayOf(
            intArrayOf(0, 0, 0, 6, 0, 0, 4, 0, 0),
            intArrayOf(7, 0, 0, 0, 0, 3, 6, 0, 0),
            intArrayOf(0, 0, 0, 0, 9, 1, 0, 8, 0),
            intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0),
            intArrayOf(0, 5, 0, 1, 8, 0, 0, 0, 3),
            intArrayOf(0, 0, 0, 3, 0, 6, 0, 4, 5),
            intArrayOf(0, 4, 0, 2, 0, 0, 0, 6, 0),
            intArrayOf(9, 0, 3, 0, 0, 0, 0, 0, 0),
            intArrayOf(0, 2, 0, 0, 0, 0, 1, 0, 0)
        )

        return fillBoardFromArray(board, puzzle)
    }

    private fun createSampleHardPuzzle(): Board {
        val board = Board()
        val puzzle = arrayOf(
            intArrayOf(0, 0, 0, 0, 0, 0, 0, 1, 2),
            intArrayOf(0, 0, 0, 0, 3, 5, 0, 0, 0),
            intArrayOf(0, 0, 0, 6, 0, 0, 0, 7, 0),
            intArrayOf(7, 0, 0, 0, 0, 0, 3, 0, 0),
            intArrayOf(0, 0, 0, 4, 0, 0, 8, 0, 0),
            intArrayOf(1, 0, 0, 0, 0, 0, 0, 0, 0),
            intArrayOf(0, 0, 0, 1, 2, 0, 0, 0, 0),
            intArrayOf(0, 8, 0, 0, 0, 0, 0, 4, 0),
            intArrayOf(0, 5, 0, 0, 0, 0, 6, 0, 0)
        )

        return fillBoardFromArray(board, puzzle)
    }

    private fun createSampleExpertPuzzle(): Board {
        val board = Board()
        val puzzle = arrayOf(
            intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0),
            intArrayOf(0, 0, 0, 0, 0, 3, 0, 8, 5),
            intArrayOf(0, 0, 1, 0, 2, 0, 0, 0, 0),
            intArrayOf(0, 0, 0, 5, 0, 7, 0, 0, 0),
            intArrayOf(0, 0, 4, 0, 0, 0, 1, 0, 0),
            intArrayOf(0, 9, 0, 0, 0, 0, 0, 0, 0),
            intArrayOf(5, 0, 0, 0, 0, 0, 0, 7, 3),
            intArrayOf(0, 0, 2, 0, 1, 0, 0, 0, 0),
            intArrayOf(0, 0, 0, 0, 4, 0, 0, 0, 9)
        )

        return fillBoardFromArray(board, puzzle)
    }

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