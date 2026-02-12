package com.autografr.app.domain.repository

import com.autografr.app.domain.model.SignedPhoto
import com.autografr.app.domain.model.Transaction
import com.autografr.app.domain.util.Result
import kotlinx.coroutines.flow.Flow

interface MarketplaceRepository {
    fun getListings(): Flow<List<SignedPhoto>>
    fun getTrendingListings(limit: Int = 20): Flow<List<SignedPhoto>>
    suspend fun createListing(photoId: String, price: Double, title: String, description: String): Result<SignedPhoto>
    suspend fun purchaseListing(photoId: String, buyerId: String): Result<Transaction>
    suspend fun removeListing(photoId: String): Result<Unit>
}
