package com.autografr.app.usecase.marketplace

import com.autografr.app.domain.model.SignedPhoto
import com.autografr.app.domain.repository.MarketplaceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetListingsUseCase @Inject constructor(
    private val marketplaceRepository: MarketplaceRepository
) {
    operator fun invoke(): Flow<List<SignedPhoto>> {
        return marketplaceRepository.getListings()
    }
}
