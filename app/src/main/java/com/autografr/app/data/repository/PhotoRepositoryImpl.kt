package com.autografr.app.data.repository

import android.net.Uri
import android.util.Log
import com.autografr.app.data.local.dao.SignedPhotoDao
import com.autografr.app.data.mapper.PhotoMapper
import com.autografr.app.data.remote.datasource.FirebaseStorageDataSource
import com.autografr.app.data.remote.datasource.FirestoreDataSource
import com.autografr.app.domain.model.PhotoStatus
import com.autografr.app.domain.model.SignedPhoto
import com.autografr.app.domain.repository.PhotoRepository
import com.autografr.app.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
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

    override fun getPhotosByOwner(ownerId: String): Flow<List<SignedPhoto>> = channelFlow {
        // Sync from Firestore so photos received from others (signed requests,
        // purchases) show up — Room alone only knows what this device wrote.
        launch {
            firestoreDataSource.getPhotosByOwner(ownerId).collect { dtos ->
                signedPhotoDao.insertPhotos(dtos.map { PhotoMapper.dtoToEntity(it) })
            }
        }
        signedPhotoDao.getPhotosByOwner(ownerId).collect { entities ->
            send(entities.map { PhotoMapper.entityToDomain(it) })
        }
    }

    override suspend fun saveSignedPhoto(
        photo: SignedPhoto,
        imageBytes: ByteArray
    ): Result<SignedPhoto> {
        var uploadedSigned = false
        var uploadedThumbnail = false
        var savedToFirestore = false
        return try {
            val signedUrl = storageDataSource.uploadSignedPhoto(photo.id, imageBytes)
            uploadedSigned = true
            val thumbnailUrl = storageDataSource.uploadThumbnail(photo.id, imageBytes)
            uploadedThumbnail = true

            val updatedPhoto = photo.copy(
                signedPhotoUrl = signedUrl,
                thumbnailUrl = thumbnailUrl,
                status = PhotoStatus.SIGNED,
                signedAt = System.currentTimeMillis()
            )

            val dto = PhotoMapper.domainToDto(updatedPhoto)
            firestoreDataSource.savePhoto(dto)
            savedToFirestore = true
            signedPhotoDao.insertPhoto(PhotoMapper.domainToEntity(updatedPhoto))

            Result.success(updatedPhoto)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save signed photo ${photo.id}", e)
            // Don't leave orphaned Storage objects behind a save that never reached Firestore
            if (!savedToFirestore) {
                if (uploadedSigned) deleteQuietly(FirebaseStorageDataSource.SIGNED_PHOTOS_PATH, photo.id)
                if (uploadedThumbnail) deleteQuietly(FirebaseStorageDataSource.THUMBNAILS_PATH, photo.id)
            }
            Result.error(e.message ?: "Failed to save signed photo", e)
        }
    }

    private suspend fun deleteQuietly(folder: String, photoId: String) {
        try {
            storageDataSource.deletePhotoFile(folder, photoId)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to clean up $folder/$photoId", e)
        }
    }

    override suspend fun uploadOriginalPhoto(photoId: String, imageBytes: ByteArray): Result<String> {
        return try {
            val url = storageDataSource.uploadOriginalPhotoBytes(photoId, imageBytes)
            Result.success(url)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload original photo $photoId", e)
            Result.error(e.message ?: "Failed to upload original photo", e)
        }
    }

    override suspend fun uploadOriginalPhotoFromUri(photoId: String, imageUri: String): Result<String> {
        return try {
            val url = storageDataSource.uploadOriginalPhotoFromUri(photoId, Uri.parse(imageUri))
            Result.success(url)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload original photo $photoId", e)
            Result.error(e.message ?: "Failed to upload photo", e)
        }
    }

    override suspend fun getPhoto(photoId: String): Result<SignedPhoto> {
        return try {
            val dto = firestoreDataSource.getPhoto(photoId)
                ?: return Result.error("Photo not found")
            Result.success(PhotoMapper.dtoToDomain(dto))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get photo $photoId", e)
            Result.error(e.message ?: "Failed to get photo", e)
        }
    }

    override suspend fun deletePhoto(photoId: String): Result<Unit> {
        return try {
            signedPhotoDao.deletePhoto(photoId)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete photo $photoId", e)
            Result.error(e.message ?: "Failed to delete photo", e)
        }
    }

    companion object {
        private const val TAG = "PhotoRepository"
    }
}
