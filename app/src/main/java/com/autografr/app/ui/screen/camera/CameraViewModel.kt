package com.autografr.app.ui.screen.camera

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.autografr.app.usecase.photo.CapturePhotoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class CameraUiState(
    val capturedPhotoUri: Uri? = null,
    val isCapturing: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val capturePhotoUseCase: CapturePhotoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    fun createPhotoUri(): Uri {
        return capturePhotoUseCase.createTempPhotoUri()
    }

    fun onPhotoCaptured(uri: Uri) {
        _uiState.update { it.copy(capturedPhotoUri = uri) }
    }

    fun onPhotoSelected(uri: Uri?) {
        uri?.let { selectedUri ->
            _uiState.update { it.copy(capturedPhotoUri = selectedUri) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
