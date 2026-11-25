package com.autografr.app.usecase.marketplace

import com.autografr.app.domain.model.SignedPhoto
import com.autografr.app.domain.repository.MarketplaceRepository
import com.autografr.app.domain.repository.SafetyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetTrendingUseCase @Inject constructor(
    private val marketplaceRepository: MarketplaceRepository,
    private val safetyRepository: SafetyRepository
) {
    operator fun invoke(limit: Int = 20): Flow<List<SignedPhoto>> {
        // Hide anything sold or signed by someone the user blocked.
        return combine(marketplaceRepository.getTrendingListings(limit), safetyRepository.observeBlockedIds()) { photos, blocked ->
            photos.filterNot { it.ownerId in blocked || it.celebrityId in blocked }
        }
    }
}
