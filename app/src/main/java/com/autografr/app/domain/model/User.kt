package com.autografr.app.domain.model

enum class UserRole {
    FAN,
    CELEBRITY,
    ADMIN
}

enum class VerificationStatus {
    UNVERIFIED,
    PENDING,
    VERIFIED,
    REJECTED
}

data class User(
    val id: String = "",
    val email: String = "",
    val displayName: String = "",
    val profileImageUrl: String? = null,
    val role: UserRole = UserRole.FAN,
    val bio: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class Celebrity(
    val user: User,
    val verificationStatus: VerificationStatus = VerificationStatus.UNVERIFIED,
    val category: String = "",
    val autographPrice: Double = 0.0,
    val isAcceptingRequests: Boolean = true,
    val totalAutographs: Int = 0,
    val rating: Float = 0f,
    val socialLinks: Map<String, String> = emptyMap()
)

data class Fan(
    val user: User,
    val favoriteCategories: List<String> = emptyList(),
    val totalPurchases: Int = 0,
    val collectionCount: Int = 0
)
