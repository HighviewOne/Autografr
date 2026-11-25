package com.autografr.app.usecase.marketplace

import com.autografr.app.domain.model.SignedPhoto
import com.autografr.app.domain.repository.MarketplaceRepository
import com.autografr.app.domain.repository.SafetyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetListingsUseCase @Inject constructor(
    private val marketplaceRepository: MarketplaceRepository,
    private val safetyRepository: SafetyRepository
) {
    operator fun invoke(): Flow<List<SignedPhoto>> {
        // Hide anything sold or signed by someone the user blocked.
        return combine(marketplaceRepository.getListings(), safetyRepository.observeBlockedIds()) { photos, blocked ->
            photos.filterNot { it.ownerId in blocked || it.celebrityId in blocked }
        }
    }
}
