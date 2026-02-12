package com.autografr.app.usecase.photo

import com.autografr.app.domain.model.SignedPhoto
import com.autografr.app.domain.repository.PhotoRepository
import com.autografr.app.domain.util.Result
import java.util.UUID
import javax.inject.Inject

class SaveSignedPhotoUseCase @Inject constructor(
    private val photoRepository: PhotoRepository
) {
    suspend operator fun invoke(
        celebrityId: String,
        celebrityName: String,
        originalPhotoUrl: String,
        imageBytes: ByteArray,
        requestId: String? = null
    ): Result<SignedPhoto> {
        val photo = SignedPhoto(
            id = UUID.randomUUID().toString(),
            celebrityId = celebrityId,
            celebrityName = celebrityName,
            originalPhotoUrl = originalPhotoUrl,
            ownerId = celebrityId,
            requestId = requestId
        )
        return photoRepository.saveSignedPhoto(photo, imageBytes)
    }
}
