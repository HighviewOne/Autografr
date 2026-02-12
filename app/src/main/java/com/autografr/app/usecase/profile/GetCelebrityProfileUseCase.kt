package com.autografr.app.usecase.profile

import com.autografr.app.domain.model.Celebrity
import com.autografr.app.domain.repository.UserRepository
import com.autografr.app.domain.util.Result
import javax.inject.Inject

class GetCelebrityProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(celebrityId: String): Result<Celebrity> {
        return userRepository.getCelebrity(celebrityId)
    }
}
