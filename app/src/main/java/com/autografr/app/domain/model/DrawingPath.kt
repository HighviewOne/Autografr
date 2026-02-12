package com.autografr.app.domain.model

import androidx.compose.ui.graphics.Color

data class PathPoint(
    val x: Float,
    val y: Float,
    val pressure: Float = 1f,
    val timestamp: Long = System.currentTimeMillis()
)

data class DrawingPath(
    val id: String = "",
    val points: List<PathPoint> = emptyList(),
    val color: Color = Color.Black,
    val strokeWidth: Float = 5f,
    val alpha: Float = 1f,
    val isEraser: Boolean = false
)
