package com.autografr.app.ui.screen.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autografr.app.domain.model.Celebrity
import com.autografr.app.domain.model.SignedPhoto
import com.autografr.app.domain.model.User
import com.autografr.app.domain.util.Result
import com.autografr.app.usecase.auth.GetCurrentUserUseCase
import com.autografr.app.usecase.profile.GetCelebrityProfileUseCase
import com.autografr.app.usecase.profile.GetPortfolioUseCase
import com.autografr.app.usecase.profile.UpdateProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentUser: User? = null,
    val celebrity: Celebrity? = null,
    val portfolio: List<SignedPhoto> = emptyList(),
    val collection: List<SignedPhoto> = emptyList()
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getCelebrityProfileUseCase: GetCelebrityProfileUseCase,
    private val getPortfolioUseCase: GetPortfolioUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { user ->
                _uiState.update { it.copy(currentUser = user) }
            }
        }
    }

    fun loadCelebrityProfile(celebrityId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = getCelebrityProfileUseCase(celebrityId)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, celebrity = result.data) }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Result.Loading -> {}
            }

            getPortfolioUseCase.getByCelebrity(celebrityId).collect { photos ->
                _uiState.update { it.copy(portfolio = photos) }
            }
        }
    }

    fun loadCollection() {
        val userId = getCurrentUserUseCase.getCurrentUserId() ?: return
        viewModelScope.launch {
            getPortfolioUseCase.getCollection(userId).collect { photos ->
                _uiState.update { it.copy(collection = photos) }
            }
        }
    }

    fun updateProfile(user: User, onComplete: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = updateProfileUseCase(user)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    onComplete()
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
