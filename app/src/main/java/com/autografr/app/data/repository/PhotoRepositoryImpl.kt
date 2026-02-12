package com.autografr.app.data.repository

import com.autografr.app.data.local.dao.SignedPhotoDao
import com.autografr.app.data.mapper.PhotoMapper
import com.autografr.app.data.remote.datasource.FirebaseStorageDataSource
import com.autografr.app.data.remote.datasource.FirestoreDataSource
import com.autografr.app.domain.model.PhotoStatus
import com.autografr.app.domain.model.SignedPhoto
import com.autografr.app.domain.repository.PhotoRepository
import com.autografr.app.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PhotoRepositoryImpl @Inject constructor(
    private val firestoreDataSource: FirestoreDataSource,
    private val storageDataSource: FirebaseStorageDataSource,
    private val signedPhotoDao: SignedPhotoDao
) : PhotoRepository {

    override fun observePhoto(photoId: String): Flow<SignedPhoto?> {
        return firestoreDataSource.getPhotoById(photoId).map { dto ->
            dto?.let {
                signedPhotoDao.insertPhoto(PhotoMapper.dtoToEntity(it))
                PhotoMapper.dtoToDomain(it)
            }
        }
    }

    override fun getPhotosByCelebrity(celebrityId: String): Flow<List<SignedPhoto>> {
        return firestoreDataSource.getPhotosByCelebrity(celebrityId).map { dtos ->
            val entities = dtos.map { PhotoMapper.dtoToEntity(it) }
            signedPhotoDao.insertPhotos(entities)
            dtos.map { PhotoMapper.dtoToDomain(it) }
        }
    }

    override fun getPhotosByOwner(ownerId: String): Flow<List<SignedPhoto>> {
        return signedPhotoDao.getPhotosByOwner(ownerId).map { entities ->
            entities.map { PhotoMapper.entityToDomain(it) }
        }
    }

    override suspend fun saveSignedPhoto(
        photo: SignedPhoto,
        imageBytes: ByteArray
    ): Result<SignedPhoto> {
        return try {
            val signedUrl = storageDataSource.uploadSignedPhoto(photo.id, imageBytes)
            val thumbnailUrl = storageDataSource.uploadThumbnail(photo.id, imageBytes)

            val updatedPhoto = photo.copy(
                signedPhotoUrl = signedUrl,
                thumbnailUrl = thumbnailUrl,
                status = PhotoStatus.SIGNED,
                signedAt = System.currentTimeMillis()
            )

            val dto = PhotoMapper.domainToDto(updatedPhoto)
            firestoreDataSource.savePhoto(dto)
            signedPhotoDao.insertPhoto(PhotoMapper.domainToEntity(updatedPhoto))

            Result.success(updatedPhoto)
        } catch (e: Exception) {
            Result.error(e.message ?: "Failed to save signed photo", e)
        }
    }

    override suspend fun getPhoto(photoId: String): Result<SignedPhoto> {
        return try {
            val dto = firestoreDataSource.getPhotoById(photoId).first()
                ?: return Result.error("Photo not found")
            Result.success(PhotoMapper.dtoToDomain(dto))
        } catch (e: Exception) {
            Result.error(e.message ?: "Failed to get photo", e)
        }
    }

    override suspend fun deletePhoto(photoId: String): Result<Unit> {
        return try {
            signedPhotoDao.deletePhoto(photoId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(e.message ?: "Failed to delete photo", e)
        }
    }
}
