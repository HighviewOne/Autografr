package com.autografr.app.usecase.auth

import com.autografr.app.domain.repository.AuthRepository
import com.autografr.app.domain.util.Result
import javax.inject.Inject

class SendPasswordResetUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String): Result<Unit> {
        val trimmed = email.trim()
        if (trimmed.isEmpty()) return Result.error("Enter your email address.")
        return authRepository.sendPasswordReset(trimmed)
    }
}
