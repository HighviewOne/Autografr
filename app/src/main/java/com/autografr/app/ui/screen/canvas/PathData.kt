package com.autografr.app.ui.screen.canvas

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import com.autografr.app.domain.model.DrawingPath
import com.autografr.app.domain.model.PathPoint

/** Builds a preview path from normalized (0..1) points scaled to a [width] x [height] canvas. */
fun DrawingPath.toComposePath(width: Float, height: Float): Path = points.toComposePath(width, height)

fun List<PathPoint>.toComposePath(width: Float, height: Float): Path {
    val path = Path()
    if (isEmpty()) return path

    path.moveTo(first().x * width, first().y * height)

    if (size == 1) {
        path.lineTo(first().x * width + 0.1f, first().y * height + 0.1f)
        return path
    }

    for (i in 1 until size) {
        val prev = this[i - 1]
        val curr = this[i]
        val midX = (prev.x + curr.x) / 2f * width
        val midY = (prev.y + curr.y) / 2f * height
        path.quadraticTo(prev.x * width, prev.y * height, midX, midY)
    }

    path.lineTo(last().x * width, last().y * height)

    return path
}

fun PathPoint.toOffset(): Offset = Offset(x, y)
