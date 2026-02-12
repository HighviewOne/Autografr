package com.autografr.app.ui.screen.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autografr.app.domain.model.User
import com.autografr.app.domain.model.UserRole
import com.autografr.app.domain.util.Result
import com.autografr.app.usecase.auth.GetCurrentUserUseCase
import com.autografr.app.usecase.auth.LoginUseCase
import com.autografr.app.usecase.auth.LogoutUseCase
import com.autografr.app.usecase.auth.RegisterUseCase
import com.autografr.app.usecase.profile.UpdateProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val user: User? = null,
    val isLoggedIn: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { user ->
                _uiState.update { it.copy(user = user, isLoggedIn = user != null) }
            }
        }
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = loginUseCase(email, password)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, user = result.data) }
                    onSuccess()
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Result.Loading -> {}
            }
        }
    }

    fun register(email: String, password: String, displayName: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = registerUseCase(email, password, displayName)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, user = result.data) }
                    onSuccess()
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Result.Loading -> {}
            }
        }
    }

    fun setupProfile(displayName: String, bio: String, role: UserRole, onComplete: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val currentUser = _uiState.value.user ?: return@launch
            val updatedUser = currentUser.copy(
                displayName = displayName,
                bio = bio,
                role = role
            )
            when (val result = updateProfileUseCase(updatedUser)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, user = updatedUser) }
                    onComplete()
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Result.Loading -> {}
            }
        }
    }

    fun logout() {
        logoutUseCase()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
