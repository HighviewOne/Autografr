package com.autografr.app.usecase.auth

import com.autografr.app.domain.model.User
import com.autografr.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<User?> = authRepository.observeAuthState()

    fun getCurrentUserId(): String? = authRepository.currentUserId

    fun isLoggedIn(): Boolean = authRepository.isLoggedIn
}
