package com.autografr.app.ui.screen.canvas

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.autografr.app.domain.model.BrushConfig
import com.autografr.app.domain.model.BrushType
import com.autografr.app.domain.model.DrawingPath
import com.autografr.app.domain.model.PathPoint

class DrawingEngine {

    private val _paths = mutableStateListOf<DrawingPath>()
    val paths: List<DrawingPath> get() = _paths

    private val _redoStack = mutableStateListOf<DrawingPath>()

    private var currentPoints = mutableListOf<PathPoint>()

    var brushConfig by mutableStateOf(BrushConfig.DEFAULT)
        private set

    var canUndo by mutableStateOf(false)
        private set

    var canRedo by mutableStateOf(false)
        private set

    fun updateBrush(config: BrushConfig) {
        brushConfig = config
    }

    fun updateColor(color: Color) {
        brushConfig = brushConfig.copy(color = color)
    }

    fun updateStrokeWidth(width: Float) {
        brushConfig = brushConfig.copy(strokeWidth = width)
    }

    fun updateBrushType(type: BrushType) {
        brushConfig = brushConfig.copy(type = type)
    }

    fun startPath(x: Float, y: Float, pressure: Float = 1f) {
        currentPoints = mutableListOf(PathPoint(x, y, pressure))
    }

    fun addPoint(x: Float, y: Float, pressure: Float = 1f) {
        currentPoints.add(PathPoint(x, y, pressure))
    }

    fun endPath() {
        if (currentPoints.size < 2) {
            currentPoints.clear()
            return
        }

        val path = DrawingPath(
            id = System.currentTimeMillis().toString(),
            points = currentPoints.toList(),
            color = if (brushConfig.type == BrushType.ERASER) Color.Transparent else brushConfig.color,
            strokeWidth = brushConfig.strokeWidth,
            alpha = brushConfig.alpha,
            isEraser = brushConfig.type == BrushType.ERASER
        )

        _paths.add(path)
        _redoStack.clear()
        currentPoints.clear()
        updateUndoRedoState()
    }

    fun undo() {
        if (_paths.isNotEmpty()) {
            val removed = _paths.removeAt(_paths.lastIndex)
            _redoStack.add(removed)
            updateUndoRedoState()
        }
    }

    fun redo() {
        if (_redoStack.isNotEmpty()) {
            val restored = _redoStack.removeAt(_redoStack.lastIndex)
            _paths.add(restored)
            updateUndoRedoState()
        }
    }

    fun clearAll() {
        _paths.clear()
        _redoStack.clear()
        currentPoints.clear()
        updateUndoRedoState()
    }

    private fun updateUndoRedoState() {
        canUndo = _paths.isNotEmpty()
        canRedo = _redoStack.isNotEmpty()
    }

    fun getCurrentPathPoints(): List<PathPoint> = currentPoints.toList()

    fun composeToBitmap(backgroundBitmap: Bitmap): Bitmap {
        val result = backgroundBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)

        val scaleX = result.width.toFloat() / 1f
        val scaleY = result.height.toFloat() / 1f

        for (drawingPath in _paths) {
            val paint = createPaint(drawingPath)
            val path = createAndroidPath(drawingPath.points, scaleX, scaleY)
            canvas.drawPath(path, paint)
        }

        return result
    }

    fun composeToBytes(backgroundBitmap: Bitmap, quality: Int = 95): ByteArray {
        val bitmap = composeToBitmap(backgroundBitmap)
        val stream = java.io.ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        return stream.toByteArray()
    }

    private fun createPaint(drawingPath: DrawingPath): Paint {
        return Paint().apply {
            color = drawingPath.color.toArgb()
            strokeWidth = drawingPath.strokeWidth * 3f
            alpha = (drawingPath.alpha * 255).toInt()
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            isAntiAlias = true

            if (drawingPath.isEraser) {
                xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.CLEAR)
            }
        }
    }

    private fun createAndroidPath(points: List<PathPoint>, scaleX: Float, scaleY: Float): Path {
        val path = Path()
        if (points.isEmpty()) return path

        path.moveTo(points[0].x * scaleX, points[0].y * scaleY)

        if (points.size == 1) {
            path.lineTo(points[0].x * scaleX + 0.1f, points[0].y * scaleY + 0.1f)
            return path
        }

        // Bezier smoothing for natural-looking curves
        for (i in 1 until points.size) {
            val prev = points[i - 1]
            val curr = points[i]
            val midX = (prev.x + curr.x) / 2f * scaleX
            val midY = (prev.y + curr.y) / 2f * scaleY
            path.quadTo(prev.x * scaleX, prev.y * scaleY, midX, midY)
        }

        val last = points.last()
        path.lineTo(last.x * scaleX, last.y * scaleY)

        return path
    }
}
