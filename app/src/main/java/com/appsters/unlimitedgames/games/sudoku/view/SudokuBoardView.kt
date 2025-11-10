package com.appsters.unlimitedgames.games.sudoku.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.appsters.unlimitedgames.games.sudoku.model.Board
import com.appsters.unlimitedgames.games.sudoku.model.Cell

class SudokuBoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var board: Board? = null
    private var selectedCell: Cell? = null
    private var listener: OnCellSelectedListener? = null

    private val thickLinePaint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.BLACK
        strokeWidth = 4f
    }

    private val thinLinePaint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.BLACK
        strokeWidth = 2f
    }

    private val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = 64f
        textAlign = Paint.Align.CENTER
    }

    private val fixedTextPaint = Paint().apply {
        color = Color.DKGRAY
        textSize = 64f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private val selectedCellPaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.LTGRAY
    }

    private val relatedCellPaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#E0E0E0") // A light gray
    }

    private var cellSize = 0f
    private val textBounds = Rect()

    fun setBoard(board: Board) {
        this.board = board
        invalidate()
    }

    fun setSelectedCell(cell: Cell?) {
        this.selectedCell = cell
        invalidate()
    }

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
        drawNumbers(canvas)
    }

    private fun drawSelectedAndRelatedCells(canvas: Canvas) {
        selectedCell?.let {
            // Highlight the selected cell
            canvas.drawRect(
                it.col * cellSize, it.row * cellSize, 
                (it.col + 1) * cellSize, (it.row + 1) * cellSize, 
                selectedCellPaint
            )

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
        }
    }

    private fun drawGrid(canvas: Canvas) {
        for (i in 0..9) {
            val paint = if (i % 3 == 0) thickLinePaint else thinLinePaint
            // Vertical lines
            canvas.drawLine(i * cellSize, 0f, i * cellSize, height.toFloat(), paint)
            // Horizontal lines
            canvas.drawLine(0f, i * cellSize, width.toFloat(), i * cellSize, paint)
        }
    }

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

    interface OnCellSelectedListener {
        fun onCellSelected(row: Int, col: Int)
    }
}
