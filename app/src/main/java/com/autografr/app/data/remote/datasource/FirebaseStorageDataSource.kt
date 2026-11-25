package com.autografr.app.data.remote.datasource

import android.content.Context
import android.net.Uri
import com.autografr.app.data.image.ImageScaler
import com.autografr.app.di.IoDispatcher
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Photo objects live under `<folder>/<uploader uid>/<photoId>.jpg` so storage.rules
 * can tie every write and delete to the signed-in user without a Firestore lookup.
 */
@Singleton
class FirebaseStorageDataSource @Inject constructor(
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth,
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    companion object {
        const val PROFILE_IMAGES_PATH = "profile_images"
        const val ORIGINAL_PHOTOS_PATH = "original_photos"
        const val SIGNED_PHOTOS_PATH = "signed_photos"
        const val THUMBNAILS_PATH = "thumbnails"

        // Keep these well under the storage.rules caps (profile 5 MB, thumbnail 2 MB).
        private const val PROFILE_MAX_DIMENSION = 1024
        private const val THUMBNAIL_MAX_DIMENSION = 512
        private const val ORIGINAL_MAX_DIMENSION = 2048
        private const val SCALED_JPEG_QUALITY = 85

        // Without explicit metadata, putBytes uploads as application/octet-stream,
        // which storage.rules rejects.
        private val JPEG_METADATA: StorageMetadata =
            StorageMetadata.Builder().setContentType("image/jpeg").build()
    }

    suspend fun uploadProfileImage(userId: String, imageUri: Uri): String {
        val bytes = withContext(ioDispatcher) {
            ImageScaler.scaleUri(context, imageUri, PROFILE_MAX_DIMENSION, SCALED_JPEG_QUALITY)
        }
        return upload(storage.reference.child("$PROFILE_IMAGES_PATH/$userId.jpg"), bytes)
    }

    /** Deletes the user's profile image; having none is fine. */
    suspend fun deleteProfileImage(userId: String) {
        try {
            storage.reference.child("$PROFILE_IMAGES_PATH/$userId.jpg").delete().await()
        } catch (e: StorageException) {
            if (e.errorCode != StorageException.ERROR_OBJECT_NOT_FOUND) throw e
        }
    }

    suspend fun uploadOriginalPhotoBytes(photoId: String, imageBytes: ByteArray): String =
        upload(photoRef(ORIGINAL_PHOTOS_PATH, photoId), imageBytes)

    /** Downscales a local photo (a fan's request photo) and uploads it as an original. */
    suspend fun uploadOriginalPhotoFromUri(photoId: String, imageUri: Uri): String {
        val bytes = withContext(ioDispatcher) {
            ImageScaler.scaleUri(context, imageUri, ORIGINAL_MAX_DIMENSION, SCALED_JPEG_QUALITY)
        }
        return upload(photoRef(ORIGINAL_PHOTOS_PATH, photoId), bytes)
    }

    suspend fun uploadSignedPhoto(photoId: String, imageBytes: ByteArray): String =
        upload(photoRef(SIGNED_PHOTOS_PATH, photoId), imageBytes)

    /** Accepts the full-size composed JPEG and uploads a downscaled copy. */
    suspend fun uploadThumbnail(photoId: String, imageBytes: ByteArray): String {
        val thumbnail = withContext(ioDispatcher) {
            ImageScaler.scaleBytes(imageBytes, THUMBNAIL_MAX_DIMENSION, SCALED_JPEG_QUALITY)
        }
        return upload(photoRef(THUMBNAILS_PATH, photoId), thumbnail)
    }

    /** Deletes a photo object this user uploaded to [folder]; missing objects are ignored. */
    suspend fun deletePhotoFile(folder: String, photoId: String) {
        try {
            photoRef(folder, photoId).delete().await()
        } catch (e: StorageException) {
            if (e.errorCode != StorageException.ERROR_OBJECT_NOT_FOUND) throw e
        }
    }

    private fun photoRef(folder: String, photoId: String): StorageReference {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("Not signed in")
        return storage.reference.child("$folder/$uid/$photoId.jpg")
    }

    private suspend fun upload(ref: StorageReference, bytes: ByteArray): String {
        ref.putBytes(bytes, JPEG_METADATA).await()
        return ref.downloadUrl.await().toString()
    }
}
