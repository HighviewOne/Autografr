package com.autografr.app.ui.screen.request

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autografr.app.domain.model.AutographRequest
import com.autografr.app.domain.model.Celebrity
import com.autografr.app.domain.util.Result
import com.autografr.app.usecase.auth.GetCurrentUserUseCase
import com.autografr.app.usecase.profile.GetCelebrityProfileUseCase
import com.autografr.app.usecase.request.AcceptRequestUseCase
import com.autografr.app.usecase.request.CreateRequestUseCase
import com.autografr.app.usecase.request.GetQueueUseCase
import com.autografr.app.usecase.request.RejectRequestUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RequestUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val queue: List<AutographRequest> = emptyList(),
    val fanRequests: List<AutographRequest> = emptyList(),
    val selectedRequest: AutographRequest? = null,
    val celebrity: Celebrity? = null
)

@HiltViewModel
class RequestViewModel @Inject constructor(
    private val createRequestUseCase: CreateRequestUseCase,
    private val getQueueUseCase: GetQueueUseCase,
    private val acceptRequestUseCase: AcceptRequestUseCase,
    private val rejectRequestUseCase: RejectRequestUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getCelebrityProfileUseCase: GetCelebrityProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RequestUiState())
    val uiState: StateFlow<RequestUiState> = _uiState.asStateFlow()

    fun loadCelebrityQueue() {
        val userId = getCurrentUserUseCase.getCurrentUserId() ?: return
        viewModelScope.launch {
            getQueueUseCase.getCelebrityQueue(userId).collect { queue ->
                _uiState.update { it.copy(queue = queue, isLoading = false) }
            }
        }
    }

    fun loadFanRequests() {
        val userId = getCurrentUserUseCase.getCurrentUserId() ?: return
        viewModelScope.launch {
            getQueueUseCase.getFanRequests(userId).collect { requests ->
                _uiState.update { it.copy(fanRequests = requests, isLoading = false) }
            }
        }
    }

    fun loadRequestDetail(requestId: String) {
        viewModelScope.launch {
            getQueueUseCase.observeRequest(requestId).collect { request ->
                _uiState.update { it.copy(selectedRequest = request) }
            }
        }
    }

    fun loadCelebrity(celebrityId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = getCelebrityProfileUseCase(celebrityId)) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, celebrity = result.data) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                is Result.Loading -> {}
            }
        }
    }

    /** Uploads [photoUri] and sends a request to the celebrity loaded by [loadCelebrity]. */
    fun createRequest(photoUri: String, message: String, onSuccess: () -> Unit) {
        val celebrity = _uiState.value.celebrity ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            // The current-user flow fetches the profile from Firestore, which can throw offline
            val fan = runCatching { getCurrentUserUseCase().first() }.getOrNull()
            if (fan == null) {
                _uiState.update { it.copy(isLoading = false, error = "Couldn't load your profile. Check your connection.") }
                return@launch
            }

            when (val result = createRequestUseCase(
                fanId = fan.id,
                fanName = fan.displayName,
                celebrityId = celebrity.user.id,
                celebrityName = celebrity.user.displayName,
                photoUri = photoUri,
                message = message,
                price = celebrity.autographPrice
            )) {
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

    fun acceptRequest(requestId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = acceptRequestUseCase(requestId)) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                is Result.Loading -> {}
            }
        }
    }

    fun rejectRequest(requestId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = rejectRequestUseCase(requestId)) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                is Result.Loading -> {}
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
