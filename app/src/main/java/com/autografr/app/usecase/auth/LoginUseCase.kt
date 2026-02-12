package com.autografr.app.usecase.auth

import com.autografr.app.domain.model.User
import com.autografr.app.domain.repository.AuthRepository
import com.autografr.app.domain.util.Result
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        if (email.isBlank()) return Result.error("Email is required")
        if (password.isBlank()) return Result.error("Password is required")
        return authRepository.login(email, password)
    }
}
