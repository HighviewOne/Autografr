package com.autografr.app.data.remote.dto

data class RequestDto(
    val id: String = "",
    val fanId: String = "",
    val fanName: String = "",
    val celebrityId: String = "",
    val celebrityName: String = "",
    val photoUrl: String = "",
    val message: String = "",
    val status: String = "PENDING",
    val price: Double = 0.0,
    val signedPhotoId: String? = null,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val expiresAt: Long? = null
)
