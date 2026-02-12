package com.autografr.app.usecase.marketplace

import com.autografr.app.domain.model.Transaction
import com.autografr.app.domain.repository.MarketplaceRepository
import com.autografr.app.domain.util.Result
import javax.inject.Inject

class PurchaseListingUseCase @Inject constructor(
    private val marketplaceRepository: MarketplaceRepository
) {
    suspend operator fun invoke(photoId: String, buyerId: String): Result<Transaction> {
        return marketplaceRepository.purchaseListing(photoId, buyerId)
    }
}
