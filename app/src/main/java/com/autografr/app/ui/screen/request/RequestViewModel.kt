package com.autografr.app.ui.screen.request

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autografr.app.domain.model.AutographRequest
import com.autografr.app.domain.util.Result
import com.autografr.app.usecase.auth.GetCurrentUserUseCase
import com.autografr.app.usecase.request.AcceptRequestUseCase
import com.autografr.app.usecase.request.CreateRequestUseCase
import com.autografr.app.usecase.request.GetQueueUseCase
import com.autografr.app.usecase.request.RejectRequestUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RequestUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val queue: List<AutographRequest> = emptyList(),
    val fanRequests: List<AutographRequest> = emptyList(),
    val selectedRequest: AutographRequest? = null
)

@HiltViewModel
class RequestViewModel @Inject constructor(
    private val createRequestUseCase: CreateRequestUseCase,
    private val getQueueUseCase: GetQueueUseCase,
    private val acceptRequestUseCase: AcceptRequestUseCase,
    private val rejectRequestUseCase: RejectRequestUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
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

    fun createRequest(
        celebrityId: String,
        celebrityName: String,
        photoUrl: String,
        message: String,
        price: Double,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val userId = getCurrentUserUseCase.getCurrentUserId() ?: return@launch

            when (val result = createRequestUseCase(
                fanId = userId,
                fanName = "",
                celebrityId = celebrityId,
                celebrityName = celebrityName,
                photoUrl = photoUrl,
                message = message,
                price = price
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
