package com.autografr.app.usecase.request

import com.autografr.app.domain.repository.RequestRepository
import com.autografr.app.domain.util.Result
import javax.inject.Inject

class CompleteRequestUseCase @Inject constructor(
    private val requestRepository: RequestRepository
) {
    suspend operator fun invoke(requestId: String, signedPhotoId: String): Result<Unit> {
        return requestRepository.completeRequest(requestId, signedPhotoId)
    }
}
