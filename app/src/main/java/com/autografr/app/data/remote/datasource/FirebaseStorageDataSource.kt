package com.autografr.app.data.remote.datasource

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseStorageDataSource @Inject constructor(
    private val storage: FirebaseStorage
) {
    companion object {
        const val PROFILE_IMAGES_PATH = "profile_images"
        const val ORIGINAL_PHOTOS_PATH = "original_photos"
        const val SIGNED_PHOTOS_PATH = "signed_photos"
        const val THUMBNAILS_PATH = "thumbnails"
    }

    suspend fun uploadProfileImage(userId: String, imageUri: Uri): String {
        val ref = storage.reference.child("$PROFILE_IMAGES_PATH/$userId.jpg")
        ref.putFile(imageUri).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun uploadOriginalPhoto(photoId: String, imageUri: Uri): String {
        val ref = storage.reference.child("$ORIGINAL_PHOTOS_PATH/$photoId.jpg")
        ref.putFile(imageUri).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun uploadSignedPhoto(photoId: String, imageBytes: ByteArray): String {
        val ref = storage.reference.child("$SIGNED_PHOTOS_PATH/$photoId.jpg")
        ref.putBytes(imageBytes).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun uploadThumbnail(photoId: String, imageBytes: ByteArray): String {
        val ref = storage.reference.child("$THUMBNAILS_PATH/$photoId.jpg")
        ref.putBytes(imageBytes).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun deleteFile(path: String) {
        storage.reference.child(path).delete().await()
    }
}
