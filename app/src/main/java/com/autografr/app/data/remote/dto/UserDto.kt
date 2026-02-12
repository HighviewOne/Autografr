package com.autografr.app.data.remote.dto

data class UserDto(
    val id: String = "",
    val email: String = "",
    val displayName: String = "",
    val profileImageUrl: String? = null,
    val role: String = "FAN",
    val bio: String = "",
    val createdAt: Long = 0L,
    val verificationStatus: String? = null,
    val category: String? = null,
    val autographPrice: Double? = null,
    val isAcceptingRequests: Boolean? = null,
    val totalAutographs: Int? = null,
    val rating: Float? = null,
    val socialLinks: Map<String, String>? = null,
    val favoriteCategories: List<String>? = null,
    val totalPurchases: Int? = null,
    val collectionCount: Int? = null
)
