package com.autografr.app.usecase.request

import com.autografr.app.domain.model.AutographRequest
import com.autografr.app.domain.repository.RequestRepository
import com.autografr.app.domain.util.Result
import javax.inject.Inject

class CreateRequestUseCase @Inject constructor(
    private val requestRepository: RequestRepository
) {
    suspend operator fun invoke(
        fanId: String,
        fanName: String,
        celebrityId: String,
        celebrityName: String,
        photoUrl: String,
        message: String,
        price: Double
    ): Result<AutographRequest> {
        if (photoUrl.isBlank()) return Result.error("Photo is required")
        val request = AutographRequest(
            fanId = fanId,
            fanName = fanName,
            celebrityId = celebrityId,
            celebrityName = celebrityName,
            photoUrl = photoUrl,
            message = message,
            price = price
        )
        return requestRepository.createRequest(request)
    }
}
