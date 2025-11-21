package com.appsters.unlimitedgames.games.sudoku.model

/**
 * Represents the 9x9 Sudoku game board.
 * This class holds a 2D array of [Cell] objects and provides methods for accessing and modifying them.
 * It also includes logic for validating moves and checking if the puzzle is solved.
 *
 * @property cells A 2D array representing the grid of cells on the board.
 */
data class Board(
    val cells: Array<Array<Cell>> = Array(9) { row ->
        Array(9) { col ->
            Cell(row, col)
        }
    }
) {
    /**
     * Retrieves the cell at the specified row and column.
     */
    fun getCell(row: Int, col: Int): Cell = cells[row][col]
    
    /**
     * Sets the value of a cell, but only if it is not a fixed (pre-filled) cell.
     */
    fun setCell(row: Int, col: Int, value: Int) {
        if (!cells[row][col].isFixed) {
            cells[row][col].value = value
        }
    }
    
    /**
     * Returns a list of all cells in a specific row.
     */
    fun getRow(row: Int): List<Cell> = cells[row].toList()
    
    /**
     * Returns a list of all cells in a specific column.
     */
    fun getColumn(col: Int): List<Cell> = cells.map { it[col] }
    
    /**
     * Returns a list of all cells in a specific 3x3 box.
     * @param boxIndex The index of the box (0-8), calculated from top-left to bottom-right.
     */
    fun getBox(boxIndex: Int): List<Cell> {
        val boxRow = (boxIndex / 3) * 3
        val boxCol = (boxIndex % 3) * 3
        val boxCells = mutableListOf<Cell>()
        
        for (r in boxRow until boxRow + 3) {
            for (c in boxCol until boxCol + 3) {
                boxCells.add(cells[r][c])
            }
        }
        return boxCells
    }
    
    /**
     * Checks if placing a given value in a specific cell is a valid move according to Sudoku rules.
     * It checks for conflicts in the row, column, and 3x3 box.
     *
     * @return `true` if the move is valid, `false` otherwise.
     */
    fun isValid(row: Int, col: Int, value: Int): Boolean {
        // Check row
        if (getRow(row).any { it.value == value && it.col != col }) return false
        
        // Check column
        if (getColumn(col).any { it.value == value && it.row != row }) return false
        
        // Check 3x3 box
        val boxIndex = cells[row][col].getBox()
        if (getBox(boxIndex).any { it.value == value && (it.row != row || it.col != col) }) {
            return false
        }
        
        return true
    }

    /**
     * Clears the given number from the notes of all cells in the same row, column, and box.
     */
    fun clearNotes(row: Int, col: Int, number: Int) {
        // Clear from row
        for (c in 0 until 9) {
            getCell(row, c).notes.remove(number)
        }
        // Clear from column
        for (r in 0 until 9) {
            getCell(r, col).notes.remove(number)
        }
        // Clear from box
        val boxRow = (row / 3) * 3
        val boxCol = (col / 3) * 3
        for (r in boxRow until boxRow + 3) {
            for (c in boxCol until boxCol + 3) {
                getCell(r, c).notes.remove(number)
            }
        }
    }
    
    /**
     * Checks if the entire board is solved correctly.
     * A solved board has no empty cells and all values are valid.
     */
    fun isSolved(): Boolean {
        return cells.all { row ->
            row.all { cell ->
                cell.value != 0 && isValid(cell.row, cell.col, cell.value)
            }
        }
    }
    
    /**
     * Creates a deep copy of the board, including all its cells.
     */
    fun copy(): Board {
        val newCells = Array(9) { row ->
            Array(9) { col ->
                cells[row][col].copy()
            }
        }
        return Board(newCells)
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Board) return false
        return cells.contentDeepEquals(other.cells)
    }
    
    override fun hashCode(): Int {
        return cells.contentDeepHashCode()
    }
}
