package com.autografr.app.ui.screen.canvas

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autografr.app.di.IoDispatcher
import com.autografr.app.domain.util.Result
import com.autografr.app.usecase.auth.GetCurrentUserUseCase
import com.autografr.app.usecase.photo.ExportSignedPhotoUseCase
import com.autografr.app.usecase.photo.SaveSignedPhotoUseCase
import com.autografr.app.usecase.photo.UploadOriginalPhotoUseCase
import com.autografr.app.usecase.request.CompleteRequestUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject

data class CanvasUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val savedPhotoId: String? = null,
    val backgroundBitmap: Bitmap? = null,
    val originalPhotoUri: String = "",
    val celebrityName: String = ""
)

@HiltViewModel
class AutographCanvasViewModel @Inject constructor(
    private val saveSignedPhotoUseCase: SaveSignedPhotoUseCase,
    private val uploadOriginalPhotoUseCase: UploadOriginalPhotoUseCase,
    private val exportSignedPhotoUseCase: ExportSignedPhotoUseCase,
    private val completeRequestUseCase: CompleteRequestUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow(CanvasUiState())
    val uiState: StateFlow<CanvasUiState> = _uiState.asStateFlow()

    val drawingEngine = DrawingEngine()

    init {
        viewModelScope.launch {
            val user = getCurrentUserUseCase().filterNotNull().first()
            _uiState.update { it.copy(celebrityName = user.displayName) }
        }
    }

    fun loadBackgroundImage(context: android.content.Context, photoUri: String) {
        _uiState.update { it.copy(originalPhotoUri = photoUri) }
        viewModelScope.launch {
            try {
                val bitmap = withContext(ioDispatcher) {
                    val uri = Uri.parse(photoUri)
                    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        BitmapFactory.decodeStream(stream, null, bounds)
                    }
                    val opts = BitmapFactory.Options().apply {
                        inSampleSize = calculateInSampleSize(bounds, MAX_BITMAP_DIMENSION, MAX_BITMAP_DIMENSION)
                    }
                    context.contentResolver.openInputStream(uri).use { stream ->
                        BitmapFactory.decodeStream(stream, null, opts)
                    }
                }
                _uiState.update { it.copy(backgroundBitmap = bitmap) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to load image: ${e.message}") }
            }
        }
    }

    fun saveAndExport(requestId: String?, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            val state = _uiState.value
            val bitmap = state.backgroundBitmap ?: return@launch
            _uiState.update { it.copy(isSaving = true, error = null) }

            val userId = getCurrentUserUseCase.getCurrentUserId() ?: run {
                _uiState.update { it.copy(isSaving = false, error = "Not logged in") }
                return@launch
            }

            // Upload original photo to Firebase Storage so the URL is remotely accessible
            val originalBytes = withContext(ioDispatcher) {
                ByteArrayOutputStream().also { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
                }.toByteArray()
            }
            val originalPhotoUrl = when (val r = uploadOriginalPhotoUseCase(originalBytes)) {
                is Result.Success -> r.data
                is Result.Error -> {
                    Log.w(TAG, "Original photo upload failed, falling back to local URI: ${r.message}")
                    state.originalPhotoUri
                }
                else -> state.originalPhotoUri
            }

            val imageBytes = withContext(ioDispatcher) {
                drawingEngine.composeToBytes(bitmap)
            }

            when (val result = saveSignedPhotoUseCase(
                celebrityId = userId,
                celebrityName = state.celebrityName,
                originalPhotoUrl = originalPhotoUrl,
                imageBytes = imageBytes,
                requestId = requestId
            )) {
                is Result.Success -> {
                    val photo = result.data

                    if (requestId != null) {
                        val completeResult = completeRequestUseCase(requestId, photo.id)
                        if (completeResult is Result.Error) {
                            Log.e(TAG, "Photo saved but failed to complete request $requestId: ${completeResult.message}")
                            // Photo is saved — navigate forward but surface the request error
                            _uiState.update { it.copy(error = "Autograph saved, but the request status couldn't be updated. Please check your connection.") }
                        }
                    }

                    val composedBitmap = withContext(ioDispatcher) {
                        drawingEngine.composeToBitmap(bitmap)
                    }
                    exportSignedPhotoUseCase(composedBitmap, "autografr_${photo.id}")

                    _uiState.update { it.copy(isSaving = false, savedPhotoId = photo.id) }
                    onComplete(photo.id)
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isSaving = false, error = result.message) }
                }
                is Result.Loading -> {}
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    companion object {
        private const val TAG = "CanvasViewModel"
        private const val MAX_BITMAP_DIMENSION = 2048
    }
}
