package com.autografr.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "autograph_requests")
data class RequestEntity(
    @PrimaryKey val id: String,
    val fanId: String,
    val fanName: String,
    val celebrityId: String,
    val celebrityName: String,
    val photoUrl: String,
    val message: String,
    val status: String,
    val price: Double,
    val signedPhotoId: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val expiresAt: Long?
)
