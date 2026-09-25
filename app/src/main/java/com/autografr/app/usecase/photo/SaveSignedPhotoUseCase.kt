package com.autografr.app.usecase.photo

import com.autografr.app.domain.model.SignedPhoto
import com.autografr.app.domain.repository.PhotoRepository
import com.autografr.app.domain.repository.RequestRepository
import com.autografr.app.domain.util.Result
import java.util.UUID
import javax.inject.Inject

class SaveSignedPhotoUseCase @Inject constructor(
    private val photoRepository: PhotoRepository,
    private val requestRepository: RequestRepository
) {
    suspend operator fun invoke(
        celebrityId: String,
        celebrityName: String,
        originalPhotoUrl: String,
        imageBytes: ByteArray,
        requestId: String? = null
    ): Result<SignedPhoto> {
        // An autograph signed for a request belongs to the fan who asked for it
        val ownerId = if (requestId == null) {
            celebrityId
        } else {
            when (val request = requestRepository.getRequest(requestId)) {
                is Result.Success -> request.data.fanId
                is Result.Error -> return Result.error(request.message)
                is Result.Loading -> return Result.error("Request not loaded")
            }
        }
        val photo = SignedPhoto(
            id = UUID.randomUUID().toString(),
            celebrityId = celebrityId,
            celebrityName = celebrityName,
            originalPhotoUrl = originalPhotoUrl,
            ownerId = ownerId,
            requestId = requestId
        )
        return photoRepository.saveSignedPhoto(photo, imageBytes)
    }
}
