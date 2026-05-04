package com.autografr.app.usecase.photo

import com.autografr.app.domain.repository.PhotoRepository
import com.autografr.app.domain.util.Result
import java.util.UUID
import javax.inject.Inject

class UploadOriginalPhotoUseCase @Inject constructor(
    private val photoRepository: PhotoRepository
) {
    suspend operator fun invoke(imageBytes: ByteArray): Result<String> {
        val photoId = UUID.randomUUID().toString()
        return photoRepository.uploadOriginalPhoto(photoId, imageBytes)
    }
}
