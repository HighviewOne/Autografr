package com.autografr.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val buyerId: String,
    val sellerId: String,
    val type: String,
    val status: String,
    val amount: Double,
    val currency: String,
    val relatedItemId: String,
    val description: String,
    val createdAt: Long,
    val completedAt: Long?
)
