package com.autografr.app.domain.repository

import com.autografr.app.domain.model.SignedPhoto
import com.autografr.app.domain.util.Result
import kotlinx.coroutines.flow.Flow

interface PhotoRepository {
    fun observePhoto(photoId: String): Flow<SignedPhoto?>
    fun getPhotosByCelebrity(celebrityId: String): Flow<List<SignedPhoto>>
    fun getPhotosByOwner(ownerId: String): Flow<List<SignedPhoto>>
    suspend fun saveSignedPhoto(photo: SignedPhoto, imageBytes: ByteArray): Result<SignedPhoto>
    suspend fun getPhoto(photoId: String): Result<SignedPhoto>
    suspend fun deletePhoto(photoId: String): Result<Unit>
}
