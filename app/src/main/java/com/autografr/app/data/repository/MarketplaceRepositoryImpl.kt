package com.autografr.app.data.repository

import android.util.Log
import com.autografr.app.data.local.dao.SignedPhotoDao
import com.autografr.app.data.local.dao.TransactionDao
import com.autografr.app.data.mapper.PhotoMapper
import com.autografr.app.data.mapper.TransactionMapper
import com.autografr.app.data.remote.datasource.FirestoreDataSource
import com.autografr.app.data.remote.dto.TransactionDto
import com.autografr.app.domain.model.PhotoStatus
import com.autografr.app.domain.model.SignedPhoto
import com.autografr.app.domain.model.Transaction
import com.autografr.app.domain.model.TransactionStatus
import com.autografr.app.domain.model.TransactionType
import com.autografr.app.domain.repository.MarketplaceRepository
import com.autografr.app.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

class MarketplaceRepositoryImpl @Inject constructor(
    private val firestoreDataSource: FirestoreDataSource,
    private val signedPhotoDao: SignedPhotoDao,
    private val transactionDao: TransactionDao
) : MarketplaceRepository {

    override fun getListings(): Flow<List<SignedPhoto>> = channelFlow {
        launch {
            firestoreDataSource.getListedPhotos().collect { dtos ->
                signedPhotoDao.insertPhotos(dtos.map { PhotoMapper.dtoToEntity(it) })
            }
        }
        signedPhotoDao.getListedPhotos().collect { entities ->
            send(entities.map { PhotoMapper.entityToDomain(it) })
        }
    }

    override fun getTrendingListings(limit: Int): Flow<List<SignedPhoto>> = channelFlow {
        launch {
            firestoreDataSource.getTrendingPhotos(limit).collect { dtos ->
                signedPhotoDao.insertPhotos(dtos.map { PhotoMapper.dtoToEntity(it) })
            }
        }
        signedPhotoDao.getTrendingPhotos(limit).collect { entities ->
            send(entities.map { PhotoMapper.entityToDomain(it) })
        }
    }

    override suspend fun createListing(
        photoId: String,
        price: Double,
        title: String,
        description: String
    ): Result<SignedPhoto> {
        return try {
            val dto = firestoreDataSource.getPhoto(photoId)
                ?: return Result.error("Photo not found")
            val updatedDto = dto.copy(
                status = PhotoStatus.LISTED.name,
                price = price,
                title = title,
                description = description
            )
            firestoreDataSource.savePhoto(updatedDto)
            signedPhotoDao.insertPhoto(PhotoMapper.dtoToEntity(updatedDto))
            Result.success(PhotoMapper.dtoToDomain(updatedDto))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create listing for photo $photoId", e)
            Result.error(e.message ?: "Failed to create listing", e)
        }
    }

    override suspend fun purchaseListing(photoId: String, buyerId: String): Result<Transaction> {
        return try {
            val photoDto = firestoreDataSource.getPhoto(photoId)
                ?: return Result.error("Listing not found")

            val transaction = TransactionDto(
                id = UUID.randomUUID().toString(),
                buyerId = buyerId,
                sellerId = photoDto.ownerId,
                type = TransactionType.MARKETPLACE_PURCHASE.name,
                status = TransactionStatus.COMPLETED.name,
                amount = photoDto.price,
                relatedItemId = photoId,
                description = "Purchase of ${photoDto.title}",
                createdAt = System.currentTimeMillis(),
                completedAt = System.currentTimeMillis()
            )
            firestoreDataSource.saveTransaction(transaction)
            transactionDao.insertTransaction(TransactionMapper.dtoToEntity(transaction))

            val updatedPhoto = photoDto.copy(
                status = PhotoStatus.SOLD.name,
                ownerId = buyerId
            )
            firestoreDataSource.savePhoto(updatedPhoto)
            signedPhotoDao.insertPhoto(PhotoMapper.dtoToEntity(updatedPhoto))

            Result.success(TransactionMapper.dtoToDomain(transaction))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to purchase listing $photoId", e)
            Result.error(e.message ?: "Failed to purchase listing", e)
        }
    }

    override suspend fun removeListing(photoId: String): Result<Unit> {
        return try {
            val dto = firestoreDataSource.getPhoto(photoId)
                ?: return Result.error("Listing not found")
            val updatedDto = dto.copy(status = PhotoStatus.SIGNED.name)
            firestoreDataSource.savePhoto(updatedDto)
            signedPhotoDao.insertPhoto(PhotoMapper.dtoToEntity(updatedDto))
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to remove listing $photoId", e)
            Result.error(e.message ?: "Failed to remove listing", e)
        }
    }

    companion object {
        private const val TAG = "MarketplaceRepository"
    }
}
