package com.autografr.app.data.repository

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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class MarketplaceRepositoryImpl @Inject constructor(
    private val firestoreDataSource: FirestoreDataSource,
    private val signedPhotoDao: SignedPhotoDao,
    private val transactionDao: TransactionDao
) : MarketplaceRepository {

    override fun getListings(): Flow<List<SignedPhoto>> {
        return firestoreDataSource.getListedPhotos().map { dtos ->
            val entities = dtos.map { PhotoMapper.dtoToEntity(it) }
            signedPhotoDao.insertPhotos(entities)
            dtos.map { PhotoMapper.dtoToDomain(it) }
        }
    }

    override fun getTrendingListings(limit: Int): Flow<List<SignedPhoto>> {
        return firestoreDataSource.getTrendingPhotos(limit).map { dtos ->
            val entities = dtos.map { PhotoMapper.dtoToEntity(it) }
            signedPhotoDao.insertPhotos(entities)
            dtos.map { PhotoMapper.dtoToDomain(it) }
        }
    }

    override suspend fun createListing(
        photoId: String,
        price: Double,
        title: String,
        description: String
    ): Result<SignedPhoto> {
        return try {
            val dto = firestoreDataSource.getPhotoById(photoId).first()
                ?: return Result.error("Photo not found")
            val updatedDto = dto.copy(
                status = PhotoStatus.LISTED.name,
                title = title,
                description = description
            )
            firestoreDataSource.savePhoto(updatedDto)
            signedPhotoDao.insertPhoto(PhotoMapper.dtoToEntity(updatedDto))
            Result.success(PhotoMapper.dtoToDomain(updatedDto))
        } catch (e: Exception) {
            Result.error(e.message ?: "Failed to create listing", e)
        }
    }

    override suspend fun purchaseListing(photoId: String, buyerId: String): Result<Transaction> {
        return try {
            val photoDto = firestoreDataSource.getPhotoById(photoId).first()
                ?: return Result.error("Listing not found")

            val transaction = TransactionDto(
                id = UUID.randomUUID().toString(),
                buyerId = buyerId,
                sellerId = photoDto.ownerId,
                type = TransactionType.MARKETPLACE_PURCHASE.name,
                status = TransactionStatus.COMPLETED.name,
                amount = 0.0,
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
            Result.error(e.message ?: "Failed to purchase listing", e)
        }
    }

    override suspend fun removeListing(photoId: String): Result<Unit> {
        return try {
            val dto = firestoreDataSource.getPhotoById(photoId).first()
                ?: return Result.error("Listing not found")
            val updatedDto = dto.copy(status = PhotoStatus.SIGNED.name)
            firestoreDataSource.savePhoto(updatedDto)
            signedPhotoDao.insertPhoto(PhotoMapper.dtoToEntity(updatedDto))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(e.message ?: "Failed to remove listing", e)
        }
    }
}
