package com.autografr.app.usecase.marketplace

import com.autografr.app.domain.model.SignedPhoto
import com.autografr.app.domain.repository.MarketplaceRepository
import com.autografr.app.domain.util.Result
import javax.inject.Inject

class CreateListingUseCase @Inject constructor(
    private val marketplaceRepository: MarketplaceRepository
) {
    suspend operator fun invoke(
        photoId: String,
        price: Double,
        title: String,
        description: String
    ): Result<SignedPhoto> {
        if (title.isBlank()) return Result.error("Title is required")
        if (price <= 0) return Result.error("Price must be greater than zero")
        return marketplaceRepository.createListing(photoId, price, title, description)
    }
}
