package com.autografr.app.ui.screen.canvas

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autografr.app.domain.util.Result
import com.autografr.app.usecase.auth.GetCurrentUserUseCase
import com.autografr.app.usecase.photo.ExportSignedPhotoUseCase
import com.autografr.app.usecase.photo.SaveSignedPhotoUseCase
import com.autografr.app.usecase.request.CompleteRequestUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CanvasUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val savedPhotoId: String? = null,
    val backgroundBitmap: Bitmap? = null
)

@HiltViewModel
class AutographCanvasViewModel @Inject constructor(
    private val saveSignedPhotoUseCase: SaveSignedPhotoUseCase,
    private val exportSignedPhotoUseCase: ExportSignedPhotoUseCase,
    private val completeRequestUseCase: CompleteRequestUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CanvasUiState())
    val uiState: StateFlow<CanvasUiState> = _uiState.asStateFlow()

    val drawingEngine = DrawingEngine()

    fun loadBackgroundImage(context: android.content.Context, photoUri: String) {
        viewModelScope.launch {
            try {
                val uri = Uri.parse(photoUri)
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                _uiState.update { it.copy(backgroundBitmap = bitmap) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to load image: ${e.message}") }
            }
        }
    }

    fun saveAndExport(requestId: String?, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            val bitmap = _uiState.value.backgroundBitmap ?: return@launch
            _uiState.update { it.copy(isSaving = true, error = null) }

            val userId = getCurrentUserUseCase.getCurrentUserId() ?: run {
                _uiState.update { it.copy(isSaving = false, error = "Not logged in") }
                return@launch
            }

            val imageBytes = drawingEngine.composeToBytes(bitmap)

            when (val result = saveSignedPhotoUseCase(
                celebrityId = userId,
                celebrityName = "",
                originalPhotoUrl = "",
                imageBytes = imageBytes,
                requestId = requestId
            )) {
                is Result.Success -> {
                    val photo = result.data
                    if (requestId != null) {
                        completeRequestUseCase(requestId, photo.id)
                    }

                    // Also export to device gallery
                    val composedBitmap = drawingEngine.composeToBitmap(bitmap)
                    exportSignedPhotoUseCase(composedBitmap, "autografr_${photo.id}")

                    _uiState.update {
                        it.copy(isSaving = false, savedPhotoId = photo.id)
                    }
                    onComplete(photo.id)
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(isSaving = false, error = result.message)
                    }
                }
                is Result.Loading -> {}
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
