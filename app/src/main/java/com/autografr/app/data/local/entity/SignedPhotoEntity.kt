package com.autografr.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "signed_photos",
    indices = [
        Index("celebrityId"),
        Index("ownerId"),
        Index("status")
    ]
)
data class SignedPhotoEntity(
    @PrimaryKey val id: String,
    val celebrityId: String,
    val celebrityName: String,
    val originalPhotoUrl: String,
    val signedPhotoUrl: String,
    val thumbnailUrl: String,
    val title: String,
    val description: String,
    val price: Double,
    val status: String,
    val requestId: String?,
    val ownerId: String,
    val createdAt: Long,
    val signedAt: Long?,
    val tags: String, // JSON serialized list
    val likeCount: Int,
    val viewCount: Int
)
