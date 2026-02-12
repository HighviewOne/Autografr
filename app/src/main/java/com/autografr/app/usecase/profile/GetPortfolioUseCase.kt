package com.autografr.app.usecase.profile

import com.autografr.app.domain.model.SignedPhoto
import com.autografr.app.domain.repository.PhotoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPortfolioUseCase @Inject constructor(
    private val photoRepository: PhotoRepository
) {
    fun getByCelebrity(celebrityId: String): Flow<List<SignedPhoto>> {
        return photoRepository.getPhotosByCelebrity(celebrityId)
    }

    fun getCollection(ownerId: String): Flow<List<SignedPhoto>> {
        return photoRepository.getPhotosByOwner(ownerId)
    }
}
