package com.autografr.app.usecase.photo

import com.autografr.app.domain.model.SignedPhoto
import com.autografr.app.domain.repository.PhotoRepository
import com.autografr.app.domain.util.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPhotoUseCase @Inject constructor(
    private val photoRepository: PhotoRepository
) {
    suspend operator fun invoke(photoId: String): Result<SignedPhoto> {
        return photoRepository.getPhoto(photoId)
    }

    fun observe(photoId: String): Flow<SignedPhoto?> {
        return photoRepository.observePhoto(photoId)
    }

    fun getByCelebrity(celebrityId: String): Flow<List<SignedPhoto>> {
        return photoRepository.getPhotosByCelebrity(celebrityId)
    }

    fun getByOwner(ownerId: String): Flow<List<SignedPhoto>> {
        return photoRepository.getPhotosByOwner(ownerId)
    }
}
