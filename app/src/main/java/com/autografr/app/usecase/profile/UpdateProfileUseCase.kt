package com.autografr.app.usecase.profile

import com.autografr.app.domain.model.User
import com.autografr.app.domain.repository.UserRepository
import com.autografr.app.domain.util.Result
import javax.inject.Inject

class UpdateProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(user: User): Result<Unit> {
        if (user.displayName.isBlank()) return Result.error("Display name is required")
        return userRepository.updateUser(user)
    }

    suspend fun updateProfileImage(userId: String, imageUri: String): Result<String> {
        return userRepository.updateProfileImage(userId, imageUri)
    }
}
