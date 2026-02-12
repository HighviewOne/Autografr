package com.autografr.app.usecase.request

import com.autografr.app.domain.repository.RequestRepository
import com.autografr.app.domain.util.Result
import javax.inject.Inject

class AcceptRequestUseCase @Inject constructor(
    private val requestRepository: RequestRepository
) {
    suspend operator fun invoke(requestId: String): Result<Unit> {
        return requestRepository.acceptRequest(requestId)
    }
}
