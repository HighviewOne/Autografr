package com.autografr.app.usecase.auth

import com.autografr.app.domain.model.User
import com.autografr.app.domain.repository.AuthRepository
import com.autografr.app.domain.util.Result
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        displayName: String
    ): Result<User> {
        if (email.isBlank()) return Result.error("Email is required")
        if (password.length < 6) return Result.error("Password must be at least 6 characters")
        if (displayName.isBlank()) return Result.error("Display name is required")
        return authRepository.register(email, password, displayName)
    }
}
