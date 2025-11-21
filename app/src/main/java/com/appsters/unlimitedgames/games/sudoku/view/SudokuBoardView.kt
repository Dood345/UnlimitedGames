package com.appsters.unlimitedgames.games.sudoku.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.appsters.unlimitedgames.R
import com.appsters.unlimitedgames.games.sudoku.model.Board
import com.appsters.unlimitedgames.games.sudoku.model.Cell

/**
 * A custom [View] that renders the Sudoku game board.
 * This view is responsible for drawing the grid, numbers, and user selections.
 * It also handles touch events to allow the user to select cells.
 */
class SudokuBoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var board: Board? = null
    private var selectedCell: Cell? = null
    private var listener: OnCellSelectedListener? = null
    private var playerColor: Int

    private val thickLinePaint: Paint
    private val thinLinePaint: Paint
    private val textPaint: Paint
    private val fixedTextPaint: Paint
    private val selectedCellPaint: Paint

    private val relatedCellPaint: Paint
    private val noteTextPaint: Paint

    private var cellSize = 0f
    private val textBounds = Rect()

    init {
        val lineColor = ContextCompat.getColor(context, R.color.sudoku_board_line_color)
        val textColor = ContextCompat.getColor(context, R.color.sudoku_board_text_color)
        playerColor = textColor
        val fixedTextColor = ContextCompat.getColor(context, R.color.sudoku_board_fixed_text_color)
        val selectedCellColor = ContextCompat.getColor(context, R.color.sudoku_board_selected_cell_color)
        val relatedCellColor = ContextCompat.getColor(context, R.color.sudoku_board_related_cell_color)

        thickLinePaint = Paint().apply {
            style = Paint.Style.STROKE
            color = lineColor
            strokeWidth = 4f
        }

        thinLinePaint = Paint().apply {
            style = Paint.Style.STROKE
            color = lineColor
            strokeWidth = 2f
        }

        textPaint = Paint().apply {
            color = textColor
            textSize = 64f
            textAlign = Paint.Align.CENTER
        }

        fixedTextPaint = Paint().apply {
            color = fixedTextColor
            textSize = 64f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }

        selectedCellPaint = Paint().apply {
            style = Paint.Style.FILL
            color = selectedCellColor
        }

        relatedCellPaint = Paint().apply {
            style = Paint.Style.FILL
            color = relatedCellColor
        }

        noteTextPaint = Paint().apply {
            color = textColor
            textSize = 24f
            textAlign = Paint.Align.CENTER
        }
    }

    /**
     * Sets the Sudoku board to be displayed by the view.
     * @param board The [Board] to render.
     */
    fun setBoard(board: Board) {
        this.board = board
        invalidate()
    }

    /**
     * Sets the currently selected cell to highlight it on the board.
     * @param cell The [Cell] that is currently selected, or `null` for no selection.
     */
    fun setSelectedCell(cell: Cell?) {
        this.selectedCell = cell
        invalidate()
    }

    /**
     * Sets the color for the user-entered numbers.
     */
    fun setPlayerColor(color: Int) {
        playerColor = color
        textPaint.color = playerColor
        invalidate()
    }

    /**
     * Sets a listener to be notified when a cell is selected.
     */
    fun setOnCellSelectedListener(listener: OnCellSelectedListener?) {
        this.listener = listener
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val size = measuredWidth.coerceAtMost(measuredHeight)
        setMeasuredDimension(size, size)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        cellSize = w / 9f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (board == null) return

        drawSelectedAndRelatedCells(canvas)
        drawGrid(canvas)
        drawNotes(canvas)
        drawNumbers(canvas)
    }

    /**
     * Draws highlights for the selected cell and its related row, column, and box.
     */
    private fun drawSelectedAndRelatedCells(canvas: Canvas) {
        selectedCell?.let {
            // Highlight the row, column, and box of the selected cell
            for (i in 0..8) {
                // Row
                canvas.drawRect(
                    i * cellSize, it.row * cellSize,
                    (i + 1) * cellSize, (it.row + 1) * cellSize,
                    relatedCellPaint
                )
                // Column
                canvas.drawRect(
                    it.col * cellSize, i * cellSize,
                    (it.col + 1) * cellSize, (i + 1) * cellSize,
                    relatedCellPaint
                )
            }

            val boxRow = (it.row / 3) * 3
            val boxCol = (it.col / 3) * 3
            for (r in boxRow until boxRow + 3) {
                for (c in boxCol until boxCol + 3) {
                    canvas.drawRect(
                        c * cellSize, r * cellSize,
                        (c + 1) * cellSize, (r + 1) * cellSize,
                        relatedCellPaint
                    )
                }
            }

            // Highlight the selected cell
            canvas.drawRect(
                it.col * cellSize, it.row * cellSize,
                (it.col + 1) * cellSize, (it.row + 1) * cellSize,
                selectedCellPaint
            )
        }
    }

    /**
     * Draws the main 9x9 grid lines of the Sudoku board.
     */
    private fun drawGrid(canvas: Canvas) {
        for (i in 0..9) {
            val paint = if (i % 3 == 0) thickLinePaint else thinLinePaint
            // Vertical lines
            canvas.drawLine(i * cellSize, 0f, i * cellSize, height.toFloat(), paint)
            // Horizontal lines
            canvas.drawLine(0f, i * cellSize, width.toFloat(), i * cellSize, paint)
        }
    }

    /**
     * Draws the numbers inside the cells of the board.
     * Fixed numbers are drawn in a bold style.
     */
    private fun drawNumbers(canvas: Canvas) {
        board?.cells?.forEach { row ->
            row.forEach { cell ->
                if (!cell.isEmpty()) {
                    val paint = if (cell.isFixed) fixedTextPaint else textPaint
                    val text = cell.value.toString()
                    paint.getTextBounds(text, 0, text.length, textBounds)
                    val textHeight = textBounds.height()
                    canvas.drawText(
                        text,
                        cell.col * cellSize + cellSize / 2,
                        cell.row * cellSize + cellSize / 2 + textHeight / 2,
                        paint
                    )
                }
            }
        }
    }

    /**
     * Draws the pencil marks (notes) inside the cells.
     */
    private fun drawNotes(canvas: Canvas) {
        board?.cells?.forEach { row ->
            row.forEach { cell ->
                if (cell.isEmpty() && cell.notes.isNotEmpty()) {
                    val cellLeft = cell.col * cellSize
                    val cellTop = cell.row * cellSize
                    val noteSize = cellSize / 3f

                    for (note in 1..9) {
                        if (cell.notes.contains(note)) {
                            val rowInCell = (note - 1) / 3
                            val colInCell = (note - 1) % 3
                            val x = cellLeft + colInCell * noteSize + noteSize / 2
                            val y = cellTop + rowInCell * noteSize + noteSize / 2 + noteTextPaint.textSize / 3 // Adjustment for vertical centering

                            canvas.drawText(note.toString(), x, y, noteTextPaint)
                        }
                    }
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            val row = (event.y / cellSize).toInt()
            val col = (event.x / cellSize).toInt()

            if (row in 0..8 && col in 0..8) {
                listener?.onCellSelected(row, col)
                return true
            }
        }
        return false
    }

    /**
     * An interface to listen for when a cell is selected on the board.
     */
    interface OnCellSelectedListener {
        /**
         * Called when a user taps on a cell.
         * @param row The row of the selected cell.
         * @param col The column of the selected cell.
         */
        fun onCellSelected(row: Int, col: Int)
    }
}
