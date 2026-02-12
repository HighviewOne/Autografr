package com.autografr.app.ui.screen.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autografr.app.domain.model.Celebrity
import com.autografr.app.domain.model.SignedPhoto
import com.autografr.app.domain.model.User
import com.autografr.app.domain.model.UserRole
import com.autografr.app.usecase.auth.GetCurrentUserUseCase
import com.autografr.app.usecase.marketplace.GetTrendingUseCase
import com.autografr.app.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FeedUiState(
    val isLoading: Boolean = false,
    val currentUser: User? = null,
    val trending: List<SignedPhoto> = emptyList(),
    val featuredCelebrities: List<Celebrity> = emptyList()
)

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getTrendingUseCase: GetTrendingUseCase,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { user ->
                _uiState.update { it.copy(currentUser = user) }
            }
        }
        loadFeed()
    }

    private fun loadFeed() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            launch {
                getTrendingUseCase(10).collect { photos ->
                    _uiState.update { it.copy(trending = photos) }
                }
            }
            launch {
                userRepository.getVerifiedCelebrities().collect { celebrities ->
                    _uiState.update { it.copy(featuredCelebrities = celebrities, isLoading = false) }
                }
            }
        }
    }

    val isCelebrity: Boolean
        get() = _uiState.value.currentUser?.role == UserRole.CELEBRITY
}
