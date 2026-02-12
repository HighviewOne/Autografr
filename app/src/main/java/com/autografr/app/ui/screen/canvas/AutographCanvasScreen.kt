package com.autografr.app.ui.screen.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.autografr.app.ui.component.ErrorDialog
import com.autografr.app.ui.component.LoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutographCanvasScreen(
    photoUri: String,
    requestId: String?,
    onSaveComplete: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: AutographCanvasViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val engine = viewModel.drawingEngine
    var currentPoints by remember { mutableStateOf(emptyList<com.autografr.app.domain.model.PathPoint>()) }

    LaunchedEffect(photoUri) {
        viewModel.loadBackgroundImage(context, photoUri)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sign Photo") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.saveAndExport(requestId, onSaveComplete)
                        },
                        enabled = !uiState.isSaving
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Drawing canvas area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    engine.startPath(offset.x, offset.y)
                                    currentPoints = engine.getCurrentPathPoints()
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    engine.addPoint(
                                        change.position.x,
                                        change.position.y
                                    )
                                    currentPoints = engine.getCurrentPathPoints()
                                },
                                onDragEnd = {
                                    engine.endPath()
                                    currentPoints = emptyList()
                                },
                                onDragCancel = {
                                    engine.endPath()
                                    currentPoints = emptyList()
                                }
                            )
                        }
                ) {
                    // Draw background image
                    uiState.backgroundBitmap?.let { bitmap ->
                        drawImage(
                            image = bitmap.asImageBitmap(),
                            dstSize = IntSize(size.width.toInt(), size.height.toInt())
                        )
                    }

                    // Draw completed paths
                    for (path in engine.paths) {
                        val composePath = path.toComposePath()
                        drawPath(
                            path = composePath,
                            color = path.color,
                            alpha = path.alpha,
                            style = path.toStroke(),
                            blendMode = path.getBlendMode()
                        )
                    }

                    // Draw current stroke being drawn
                    if (currentPoints.size >= 2) {
                        val currentPath = currentPoints.toComposePath()
                        drawPath(
                            path = currentPath,
                            color = engine.brushConfig.color,
                            alpha = engine.brushConfig.alpha,
                            style = Stroke(
                                width = engine.brushConfig.strokeWidth,
                                cap = androidx.compose.ui.graphics.StrokeCap.Round,
                                join = androidx.compose.ui.graphics.StrokeJoin.Round
                            )
                        )
                    }
                }
            }

            // Drawing toolbar
            DrawingToolbar(
                brushConfig = engine.brushConfig,
                canUndo = engine.canUndo,
                canRedo = engine.canRedo,
                onColorSelected = { engine.updateColor(it) },
                onStrokeWidthChanged = { engine.updateStrokeWidth(it) },
                onBrushTypeSelected = { engine.updateBrushType(it) },
                onUndo = { engine.undo() },
                onRedo = { engine.redo() },
                onClearAll = { engine.clearAll() }
            )
        }

        if (uiState.isSaving) {
            LoadingIndicator()
        }

        uiState.error?.let { error ->
            ErrorDialog(
                message = error,
                onDismiss = { viewModel.clearError() }
            )
        }
    }
}
