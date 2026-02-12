package com.autografr.app.domain.model

enum class RequestStatus {
    PENDING,
    ACCEPTED,
    IN_PROGRESS,
    COMPLETED,
    REJECTED,
    CANCELLED,
    EXPIRED
}

data class AutographRequest(
    val id: String = "",
    val fanId: String = "",
    val fanName: String = "",
    val celebrityId: String = "",
    val celebrityName: String = "",
    val photoUrl: String = "",
    val message: String = "",
    val status: RequestStatus = RequestStatus.PENDING,
    val price: Double = 0.0,
    val signedPhotoId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val expiresAt: Long? = null
)
