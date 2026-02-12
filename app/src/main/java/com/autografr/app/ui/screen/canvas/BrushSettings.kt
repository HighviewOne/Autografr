package com.autografr.app.ui.screen.canvas

import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.autografr.app.domain.model.BrushType
import com.autografr.app.domain.model.DrawingPath

fun DrawingPath.toStroke(): Stroke {
    return when {
        isEraser -> Stroke(
            width = strokeWidth * 3f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
        else -> Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    }
}

fun DrawingPath.getBlendMode(): BlendMode {
    return if (isEraser) BlendMode.Clear else BlendMode.SrcOver
}

object BrushPresets {
    fun penStroke(width: Float) = Stroke(
        width = width,
        cap = StrokeCap.Round,
        join = StrokeJoin.Round
    )

    fun markerStroke(width: Float) = Stroke(
        width = width * 2f,
        cap = StrokeCap.Square,
        join = StrokeJoin.Bevel
    )

    fun calligraphyStroke(width: Float) = Stroke(
        width = width * 1.5f,
        cap = StrokeCap.Butt,
        join = StrokeJoin.Round
    )

    fun glowStroke(width: Float) = Stroke(
        width = width * 2.5f,
        cap = StrokeCap.Round,
        join = StrokeJoin.Round
    )

    fun eraserStroke(width: Float) = Stroke(
        width = width * 3f,
        cap = StrokeCap.Round,
        join = StrokeJoin.Round
    )

    fun forType(type: BrushType, width: Float): Stroke = when (type) {
        BrushType.PEN -> penStroke(width)
        BrushType.MARKER -> markerStroke(width)
        BrushType.CALLIGRAPHY -> calligraphyStroke(width)
        BrushType.GLOW -> glowStroke(width)
        BrushType.ERASER -> eraserStroke(width)
    }
}
