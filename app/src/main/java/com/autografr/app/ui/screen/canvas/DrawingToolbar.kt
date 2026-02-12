package com.autografr.app.ui.screen.canvas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.autografr.app.domain.model.BrushConfig
import com.autografr.app.domain.model.BrushType

@Composable
fun DrawingToolbar(
    brushConfig: BrushConfig,
    canUndo: Boolean,
    canRedo: Boolean,
    onColorSelected: (Color) -> Unit,
    onStrokeWidthChanged: (Float) -> Unit,
    onBrushTypeSelected: (BrushType) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 4.dp,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Brush type selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BrushTypeButton(
                    icon = Icons.Default.Edit,
                    label = "Pen",
                    isSelected = brushConfig.type == BrushType.PEN,
                    onClick = { onBrushTypeSelected(BrushType.PEN) }
                )
                BrushTypeButton(
                    icon = Icons.Default.Brush,
                    label = "Marker",
                    isSelected = brushConfig.type == BrushType.MARKER,
                    onClick = { onBrushTypeSelected(BrushType.MARKER) }
                )
                BrushTypeButton(
                    icon = Icons.Default.Create,
                    label = "Calli",
                    isSelected = brushConfig.type == BrushType.CALLIGRAPHY,
                    onClick = { onBrushTypeSelected(BrushType.CALLIGRAPHY) }
                )
                BrushTypeButton(
                    icon = Icons.Default.FlashOn,
                    label = "Glow",
                    isSelected = brushConfig.type == BrushType.GLOW,
                    onClick = { onBrushTypeSelected(BrushType.GLOW) }
                )
                BrushTypeButton(
                    icon = Icons.Default.DeleteSweep,
                    label = "Eraser",
                    isSelected = brushConfig.type == BrushType.ERASER,
                    onClick = { onBrushTypeSelected(BrushType.ERASER) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Color palette
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(BrushConfig.PRESET_COLORS) { color ->
                    ColorSwatch(
                        color = color,
                        isSelected = brushConfig.color == color,
                        onClick = { onColorSelected(color) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stroke width slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Size",
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.width(8.dp))
                Slider(
                    value = brushConfig.strokeWidth,
                    onValueChange = onStrokeWidthChanged,
                    valueRange = 1f..30f,
                    modifier = Modifier.weight(1f)
                )
            }

            // Undo/Redo/Clear
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onUndo, enabled = canUndo) {
                    Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo")
                }
                IconButton(onClick = onRedo, enabled = canRedo) {
                    Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo")
                }
                IconButton(onClick = onClearAll) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = "Clear All")
                }
            }
        }
    }
}

@Composable
private fun BrushTypeButton(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onClick)
            .then(
                if (isSelected) Modifier.background(
                    MaterialTheme.colorScheme.primaryContainer,
                    MaterialTheme.shapes.small
                ) else Modifier
            )
            .padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ColorSwatch(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(color, CircleShape)
            .then(
                if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                else Modifier.border(1.dp, Color.Gray.copy(alpha = 0.5f), CircleShape)
            )
            .clickable(onClick = onClick)
    )
}
