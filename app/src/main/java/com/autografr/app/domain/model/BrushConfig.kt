package com.autografr.app.domain.model

import androidx.compose.ui.graphics.Color

enum class BrushType {
    PEN,
    MARKER,
    CALLIGRAPHY,
    GLOW,
    ERASER
}

data class BrushConfig(
    val type: BrushType = BrushType.PEN,
    val color: Color = Color.Black,
    val strokeWidth: Float = 5f,
    val alpha: Float = 1f,
    val pressureSensitive: Boolean = true
) {
    companion object {
        val DEFAULT = BrushConfig()

        val PRESET_COLORS = listOf(
            Color.Black,
            Color.White,
            Color(0xFF1A237E), // Ink Blue
            Color(0xFFFFD700), // Gold
            Color(0xFFB00020), // Red
            Color(0xFF2E7D32), // Green
            Color(0xFF7B1FA2), // Purple
            Color(0xFFFF6F00), // Orange
        )
    }
}
