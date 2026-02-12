package com.autografr.app.data.remote.dto

data class TransactionDto(
    val id: String = "",
    val buyerId: String = "",
    val sellerId: String = "",
    val type: String = "AUTOGRAPH_REQUEST",
    val status: String = "PENDING",
    val amount: Double = 0.0,
    val currency: String = "USD",
    val relatedItemId: String = "",
    val description: String = "",
    val createdAt: Long = 0L,
    val completedAt: Long? = null
)
