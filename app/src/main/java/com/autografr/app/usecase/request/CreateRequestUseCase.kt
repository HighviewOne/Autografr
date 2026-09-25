package com.autografr.app.usecase.request

import com.autografr.app.domain.model.AutographRequest
import com.autografr.app.domain.repository.PhotoRepository
import com.autografr.app.domain.repository.RequestRepository
import com.autografr.app.domain.util.Result
import java.util.UUID
import javax.inject.Inject

class CreateRequestUseCase @Inject constructor(
    private val requestRepository: RequestRepository,
    private val photoRepository: PhotoRepository
) {
    /** Uploads the fan's local photo ([photoUri]) to Storage, then creates the request. */
    suspend operator fun invoke(
        fanId: String,
        fanName: String,
        celebrityId: String,
        celebrityName: String,
        photoUri: String,
        message: String,
        price: Double
    ): Result<AutographRequest> {
        if (photoUri.isBlank()) return Result.error("Photo is required")
        val photoUrl = when (val upload = photoRepository.uploadOriginalPhotoFromUri(
            UUID.randomUUID().toString(), photoUri
        )) {
            is Result.Success -> upload.data
            is Result.Error -> return Result.error("Couldn't upload your photo: ${upload.message}")
            is Result.Loading -> return Result.error("Photo upload didn't finish")
        }
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
