package com.autografr.app.ui.screen.share

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autografr.app.domain.model.SignedPhoto
import com.autografr.app.usecase.photo.GetPhotoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ShareUiState(
    val isLoading: Boolean = false,
    val photo: SignedPhoto? = null,
    val error: String? = null
)

@HiltViewModel
class ShareViewModel @Inject constructor(
    private val getPhotoUseCase: GetPhotoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShareUiState())
    val uiState: StateFlow<ShareUiState> = _uiState.asStateFlow()

    fun loadPhoto(photoId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getPhotoUseCase.observe(photoId).collect { photo ->
                _uiState.update { it.copy(isLoading = false, photo = photo) }
            }
        }
    }

    fun sharePhoto(context: Context) {
        val photo = _uiState.value.photo ?: return
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Check out this autograph on Autografr!")
            putExtra(
                Intent.EXTRA_TEXT,
                "Check out this autograph by ${photo.celebrityName} on Autografr! ${photo.signedPhotoUrl}"
            )
        }

        if (photo.signedPhotoUrl.isNotEmpty()) {
            try {
                val imageUri = Uri.parse(photo.signedPhotoUrl)
                shareIntent.type = "image/*"
                shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri)
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: Exception) {
                // Fall back to text sharing
            }
        }

        context.startActivity(Intent.createChooser(shareIntent, "Share Autograph"))
    }
}
