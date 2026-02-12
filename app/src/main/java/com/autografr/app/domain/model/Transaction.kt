package com.autografr.app.domain.model

enum class TransactionType {
    AUTOGRAPH_REQUEST,
    MARKETPLACE_PURCHASE,
    PAYOUT,
    REFUND
}

enum class TransactionStatus {
    PENDING,
    COMPLETED,
    FAILED,
    REFUNDED
}

data class Transaction(
    val id: String = "",
    val buyerId: String = "",
    val sellerId: String = "",
    val type: TransactionType = TransactionType.AUTOGRAPH_REQUEST,
    val status: TransactionStatus = TransactionStatus.PENDING,
    val amount: Double = 0.0,
    val currency: String = "USD",
    val relatedItemId: String = "",
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)
