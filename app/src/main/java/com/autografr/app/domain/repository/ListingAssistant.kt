package com.autografr.app.domain.repository

import com.autografr.app.domain.model.SignedPhoto
import com.autografr.app.domain.util.Result

/** Draft marketplace copy the seller reviews and edits before listing. */
data class ListingSuggestion(
    val title: String,
    val description: String
)

interface ListingAssistant {
    suspend fun suggestListing(photo: SignedPhoto): Result<ListingSuggestion>
}
