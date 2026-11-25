package com.autografr.app.usecase.auth

import com.autografr.app.domain.repository.AuthRepository
import com.autografr.app.domain.util.Result
import javax.inject.Inject

class DeleteAccountUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(password: String): Result<Unit> {
        if (password.isBlank()) return Result.error("Enter your password to confirm.")
        return authRepository.deleteAccount(password)
    }
}
