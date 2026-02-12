package com.autografr.app.domain.model

enum class PhotoStatus {
    DRAFT,
    SIGNED,
    LISTED,
    SOLD,
    ARCHIVED
}

data class SignedPhoto(
    val id: String = "",
    val celebrityId: String = "",
    val celebrityName: String = "",
    val originalPhotoUrl: String = "",
    val signedPhotoUrl: String = "",
    val thumbnailUrl: String = "",
    val title: String = "",
    val description: String = "",
    val status: PhotoStatus = PhotoStatus.DRAFT,
    val requestId: String? = null,
    val ownerId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val signedAt: Long? = null,
    val tags: List<String> = emptyList(),
    val likeCount: Int = 0,
    val viewCount: Int = 0
)
