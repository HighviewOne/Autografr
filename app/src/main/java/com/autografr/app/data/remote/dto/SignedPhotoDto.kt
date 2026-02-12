package com.autografr.app.data.remote.dto

data class SignedPhotoDto(
    val id: String = "",
    val celebrityId: String = "",
    val celebrityName: String = "",
    val originalPhotoUrl: String = "",
    val signedPhotoUrl: String = "",
    val thumbnailUrl: String = "",
    val title: String = "",
    val description: String = "",
    val status: String = "DRAFT",
    val requestId: String? = null,
    val ownerId: String = "",
    val createdAt: Long = 0L,
    val signedAt: Long? = null,
    val tags: List<String> = emptyList(),
    val likeCount: Int = 0,
    val viewCount: Int = 0
)
