package com.autografr.app.ui.screen.safety

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autografr.app.domain.model.BlockedUser
import com.autografr.app.domain.model.ReportReason
import com.autografr.app.domain.model.ReportTargetType
import com.autografr.app.domain.repository.SafetyRepository
import com.autografr.app.domain.util.Result
import com.autografr.app.usecase.auth.GetCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Backs the Report / Block menu and the Blocked users list. Shared by any screen that shows them. */
@HiltViewModel
class SafetyViewModel @Inject constructor(
    private val safetyRepository: SafetyRepository,
    getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    val currentUserId: String? = getCurrentUserUseCase.getCurrentUserId()

    val blockedUsers: StateFlow<List<BlockedUser>> = safetyRepository.observeBlockedUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    /** One-off confirmation or error text; the UI shows it once, then calls [consumeMessage]. */
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun report(
        targetType: ReportTargetType,
        targetId: String,
        targetUserId: String,
        reason: ReportReason,
        details: String,
        onDone: () -> Unit
    ) = run(onDone, "Thanks. We'll review this report.") {
        safetyRepository.report(targetType, targetId, targetUserId, reason, details)
    }

    fun block(userId: String, displayName: String, onDone: () -> Unit = {}) =
        run(onDone, "Blocked ${displayName.ifBlank { "this user" }}.") {
            safetyRepository.blockUser(userId, displayName)
        }

    fun unblock(userId: String, displayName: String, onDone: () -> Unit = {}) =
        run(onDone, "Unblocked ${displayName.ifBlank { "this user" }}.") {
            safetyRepository.unblockUser(userId)
        }

    fun consumeMessage() {
        _message.value = null
    }

    private fun run(onDone: () -> Unit, success: String, action: suspend () -> Result<Unit>) {
        if (_busy.value) return
        viewModelScope.launch {
            _busy.value = true
            when (val result = action()) {
                is Result.Success -> {
                    _message.value = success
                    onDone()
                }
                is Result.Error -> _message.value = result.message
                is Result.Loading -> {}
            }
            _busy.value = false
        }
    }
}
