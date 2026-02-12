package com.autografr.app.data.mapper

import com.autografr.app.data.local.entity.TransactionEntity
import com.autografr.app.data.remote.dto.TransactionDto
import com.autografr.app.domain.model.Transaction
import com.autografr.app.domain.model.TransactionStatus
import com.autografr.app.domain.model.TransactionType

object TransactionMapper {

    fun dtoToDomain(dto: TransactionDto): Transaction = Transaction(
        id = dto.id,
        buyerId = dto.buyerId,
        sellerId = dto.sellerId,
        type = TransactionType.valueOf(dto.type),
        status = TransactionStatus.valueOf(dto.status),
        amount = dto.amount,
        currency = dto.currency,
        relatedItemId = dto.relatedItemId,
        description = dto.description,
        createdAt = dto.createdAt,
        completedAt = dto.completedAt
    )

    fun domainToDto(transaction: Transaction): TransactionDto = TransactionDto(
        id = transaction.id,
        buyerId = transaction.buyerId,
        sellerId = transaction.sellerId,
        type = transaction.type.name,
        status = transaction.status.name,
        amount = transaction.amount,
        currency = transaction.currency,
        relatedItemId = transaction.relatedItemId,
        description = transaction.description,
        createdAt = transaction.createdAt,
        completedAt = transaction.completedAt
    )

    fun entityToDomain(entity: TransactionEntity): Transaction = Transaction(
        id = entity.id,
        buyerId = entity.buyerId,
        sellerId = entity.sellerId,
        type = TransactionType.valueOf(entity.type),
        status = TransactionStatus.valueOf(entity.status),
        amount = entity.amount,
        currency = entity.currency,
        relatedItemId = entity.relatedItemId,
        description = entity.description,
        createdAt = entity.createdAt,
        completedAt = entity.completedAt
    )

    fun domainToEntity(transaction: Transaction): TransactionEntity = TransactionEntity(
        id = transaction.id,
        buyerId = transaction.buyerId,
        sellerId = transaction.sellerId,
        type = transaction.type.name,
        status = transaction.status.name,
        amount = transaction.amount,
        currency = transaction.currency,
        relatedItemId = transaction.relatedItemId,
        description = transaction.description,
        createdAt = transaction.createdAt,
        completedAt = transaction.completedAt
    )

    fun dtoToEntity(dto: TransactionDto): TransactionEntity = TransactionEntity(
        id = dto.id,
        buyerId = dto.buyerId,
        sellerId = dto.sellerId,
        type = dto.type,
        status = dto.status,
        amount = dto.amount,
        currency = dto.currency,
        relatedItemId = dto.relatedItemId,
        description = dto.description,
        createdAt = dto.createdAt,
        completedAt = dto.completedAt
    )
}
