package com.autografr.app.ui.screen.canvas

import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import com.autografr.app.domain.model.BrushType
import com.autografr.app.domain.model.DrawingPath

fun DrawingPath.toStroke(): Stroke = BrushPresets.forType(brushType, strokeWidth)

fun DrawingPath.getBlendMode(): BlendMode = when (brushType) {
    BrushType.ERASER -> BlendMode.Clear
    BrushType.GLOW -> BlendMode.Plus
    else -> BlendMode.SrcOver
}

object BrushPresets {
    /** Stroke width multiplier per brush — shared by the on-screen preview and the export. */
    fun widthMultiplier(type: BrushType): Float = when (type) {
        BrushType.PEN -> 1f
        BrushType.MARKER -> 2f
        BrushType.CALLIGRAPHY -> 1.5f
        BrushType.GLOW -> 2.5f
        BrushType.ERASER -> 3f
    }

    fun forType(type: BrushType, width: Float): Stroke {
        val w = width * widthMultiplier(type)
        return when (type) {
            BrushType.PEN, BrushType.GLOW, BrushType.ERASER ->
                Stroke(width = w, cap = StrokeCap.Round, join = StrokeJoin.Round)
            BrushType.MARKER -> Stroke(width = w, cap = StrokeCap.Square, join = StrokeJoin.Bevel)
            BrushType.CALLIGRAPHY -> Stroke(width = w, cap = StrokeCap.Butt, join = StrokeJoin.Round)
        }
    }
}
