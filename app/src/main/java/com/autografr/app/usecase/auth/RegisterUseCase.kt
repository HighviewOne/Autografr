package com.autografr.app.usecase.auth

import android.util.Patterns
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
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) return Result.error("Invalid email address")
        if (displayName.isBlank()) return Result.error("Display name is required")
        if (password.length < 8) return Result.error("Password must be at least 8 characters")
        if (!password.any { it.isUpperCase() }) return Result.error("Password must contain at least one uppercase letter")
        if (!password.any { it.isDigit() }) return Result.error("Password must contain at least one number")
        return authRepository.register(email, password, displayName)
    }
}
