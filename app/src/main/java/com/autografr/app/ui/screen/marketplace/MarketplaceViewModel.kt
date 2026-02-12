package com.autografr.app.ui.screen.marketplace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autografr.app.domain.model.SignedPhoto
import com.autografr.app.domain.util.Result
import com.autografr.app.usecase.auth.GetCurrentUserUseCase
import com.autografr.app.usecase.marketplace.CreateListingUseCase
import com.autografr.app.usecase.marketplace.GetListingsUseCase
import com.autografr.app.usecase.marketplace.GetTrendingUseCase
import com.autografr.app.usecase.marketplace.PurchaseListingUseCase
import com.autografr.app.usecase.photo.GetPhotoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MarketplaceUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val listings: List<SignedPhoto> = emptyList(),
    val trending: List<SignedPhoto> = emptyList(),
    val selectedPhoto: SignedPhoto? = null,
    val purchaseSuccess: Boolean = false
)

@HiltViewModel
class MarketplaceViewModel @Inject constructor(
    private val getListingsUseCase: GetListingsUseCase,
    private val getTrendingUseCase: GetTrendingUseCase,
    private val createListingUseCase: CreateListingUseCase,
    private val purchaseListingUseCase: PurchaseListingUseCase,
    private val getPhotoUseCase: GetPhotoUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MarketplaceUiState())
    val uiState: StateFlow<MarketplaceUiState> = _uiState.asStateFlow()

    fun loadListings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getListingsUseCase().collect { listings ->
                _uiState.update { it.copy(listings = listings, isLoading = false) }
            }
        }
    }

    fun loadTrending() {
        viewModelScope.launch {
            getTrendingUseCase().collect { trending ->
                _uiState.update { it.copy(trending = trending) }
            }
        }
    }

    fun loadPhotoDetail(photoId: String) {
        viewModelScope.launch {
            getPhotoUseCase.observe(photoId).collect { photo ->
                _uiState.update { it.copy(selectedPhoto = photo) }
            }
        }
    }

    fun createListing(
        photoId: String,
        price: Double,
        title: String,
        description: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = createListingUseCase(photoId, price, title, description)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess()
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Result.Loading -> {}
            }
        }
    }

    fun purchaseListing(photoId: String) {
        val userId = getCurrentUserUseCase.getCurrentUserId() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = purchaseListingUseCase(photoId, userId)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, purchaseSuccess = true) }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Result.Loading -> {}
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
