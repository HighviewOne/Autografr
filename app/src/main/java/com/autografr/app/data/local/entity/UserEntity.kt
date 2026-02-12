package com.autografr.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val email: String,
    val displayName: String,
    val profileImageUrl: String?,
    val role: String,
    val bio: String,
    val createdAt: Long,
    // Celebrity-specific fields (null for fans)
    val verificationStatus: String? = null,
    val category: String? = null,
    val autographPrice: Double? = null,
    val isAcceptingRequests: Boolean? = null,
    val totalAutographs: Int? = null,
    val rating: Float? = null
)
