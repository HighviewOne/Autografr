package com.autografr.app.ui.screen.canvas

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import com.autografr.app.domain.model.DrawingPath
import com.autografr.app.domain.model.PathPoint

fun DrawingPath.toComposePath(): Path {
    val path = Path()
    if (points.isEmpty()) return path

    path.moveTo(points[0].x, points[0].y)

    if (points.size == 1) {
        path.lineTo(points[0].x + 0.1f, points[0].y + 0.1f)
        return path
    }

    for (i in 1 until points.size) {
        val prev = points[i - 1]
        val curr = points[i]
        val midX = (prev.x + curr.x) / 2f
        val midY = (prev.y + curr.y) / 2f
        path.quadraticTo(prev.x, prev.y, midX, midY)
    }

    val last = points.last()
    path.lineTo(last.x, last.y)

    return path
}

fun List<PathPoint>.toComposePath(): Path {
    val path = Path()
    if (isEmpty()) return path

    path.moveTo(first().x, first().y)

    if (size == 1) {
        path.lineTo(first().x + 0.1f, first().y + 0.1f)
        return path
    }

    for (i in 1 until size) {
        val prev = this[i - 1]
        val curr = this[i]
        val midX = (prev.x + curr.x) / 2f
        val midY = (prev.y + curr.y) / 2f
        path.quadraticTo(prev.x, prev.y, midX, midY)
    }

    path.lineTo(last().x, last().y)

    return path
}

fun PathPoint.toOffset(): Offset = Offset(x, y)
