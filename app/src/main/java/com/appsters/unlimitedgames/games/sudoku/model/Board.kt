package com.appsters.unlimitedgames.games.sudoku.model

data class Board(
    val cells: Array<Array<Cell>> = Array(9) { row ->
        Array(9) { col ->
            Cell(row, col)
        }
    }
) {
    fun getCell(row: Int, col: Int): Cell = cells[row][col]
    
    fun setCell(row: Int, col: Int, value: Int) {
        if (!cells[row][col].isFixed) {
            cells[row][col].value = value
        }
    }
    
    fun getRow(row: Int): List<Cell> = cells[row].toList()
    
    fun getColumn(col: Int): List<Cell> = cells.map { it[col] }
    
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
    
    fun isSolved(): Boolean {
        return cells.all { row ->
            row.all { cell ->
                cell.value != 0 && isValid(cell.row, cell.col, cell.value)
            }
        }
    }
    
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
