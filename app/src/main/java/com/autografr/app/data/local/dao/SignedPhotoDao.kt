package com.autografr.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.autografr.app.data.local.entity.SignedPhotoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SignedPhotoDao {
    @Query("SELECT * FROM signed_photos WHERE id = :photoId")
    fun getPhotoById(photoId: String): Flow<SignedPhotoEntity?>

    @Query("SELECT * FROM signed_photos WHERE celebrityId = :celebrityId ORDER BY createdAt DESC")
    fun getPhotosByCelebrity(celebrityId: String): Flow<List<SignedPhotoEntity>>

    @Query("SELECT * FROM signed_photos WHERE ownerId = :ownerId ORDER BY createdAt DESC")
    fun getPhotosByOwner(ownerId: String): Flow<List<SignedPhotoEntity>>

    @Query("SELECT * FROM signed_photos WHERE status = 'LISTED' ORDER BY createdAt DESC")
    fun getListedPhotos(): Flow<List<SignedPhotoEntity>>

    @Query("SELECT * FROM signed_photos WHERE status = 'LISTED' ORDER BY likeCount DESC LIMIT :limit")
    fun getTrendingPhotos(limit: Int = 20): Flow<List<SignedPhotoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: SignedPhotoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhotos(photos: List<SignedPhotoEntity>)

    @Query("DELETE FROM signed_photos WHERE id = :photoId")
    suspend fun deletePhoto(photoId: String)
}
