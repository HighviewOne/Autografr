package com.autografr.app.usecase.marketplace

import com.autografr.app.domain.model.SignedPhoto
import com.autografr.app.domain.repository.MarketplaceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTrendingUseCase @Inject constructor(
    private val marketplaceRepository: MarketplaceRepository
) {
    operator fun invoke(limit: Int = 20): Flow<List<SignedPhoto>> {
        return marketplaceRepository.getTrendingListings(limit)
    }
}
