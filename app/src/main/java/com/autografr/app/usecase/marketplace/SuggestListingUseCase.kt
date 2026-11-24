package com.autografr.app.usecase.marketplace

import com.autografr.app.domain.model.SignedPhoto
import com.autografr.app.domain.repository.ListingAssistant
import com.autografr.app.domain.repository.ListingSuggestion
import com.autografr.app.domain.util.Result
import javax.inject.Inject

class SuggestListingUseCase @Inject constructor(
    private val listingAssistant: ListingAssistant
) {
    suspend operator fun invoke(photo: SignedPhoto): Result<ListingSuggestion> =
        listingAssistant.suggestListing(photo)
}
