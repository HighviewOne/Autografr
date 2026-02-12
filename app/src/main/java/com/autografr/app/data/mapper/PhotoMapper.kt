package com.autografr.app.data.mapper

import com.autografr.app.data.local.entity.SignedPhotoEntity
import com.autografr.app.data.remote.dto.SignedPhotoDto
import com.autografr.app.domain.model.PhotoStatus
import com.autografr.app.domain.model.SignedPhoto
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object PhotoMapper {

    private val json = Json { ignoreUnknownKeys = true }

    fun dtoToDomain(dto: SignedPhotoDto): SignedPhoto = SignedPhoto(
        id = dto.id,
        celebrityId = dto.celebrityId,
        celebrityName = dto.celebrityName,
        originalPhotoUrl = dto.originalPhotoUrl,
        signedPhotoUrl = dto.signedPhotoUrl,
        thumbnailUrl = dto.thumbnailUrl,
        title = dto.title,
        description = dto.description,
        status = PhotoStatus.valueOf(dto.status),
        requestId = dto.requestId,
        ownerId = dto.ownerId,
        createdAt = dto.createdAt,
        signedAt = dto.signedAt,
        tags = dto.tags,
        likeCount = dto.likeCount,
        viewCount = dto.viewCount
    )

    fun domainToDto(photo: SignedPhoto): SignedPhotoDto = SignedPhotoDto(
        id = photo.id,
        celebrityId = photo.celebrityId,
        celebrityName = photo.celebrityName,
        originalPhotoUrl = photo.originalPhotoUrl,
        signedPhotoUrl = photo.signedPhotoUrl,
        thumbnailUrl = photo.thumbnailUrl,
        title = photo.title,
        description = photo.description,
        status = photo.status.name,
        requestId = photo.requestId,
        ownerId = photo.ownerId,
        createdAt = photo.createdAt,
        signedAt = photo.signedAt,
        tags = photo.tags,
        likeCount = photo.likeCount,
        viewCount = photo.viewCount
    )

    fun entityToDomain(entity: SignedPhotoEntity): SignedPhoto = SignedPhoto(
        id = entity.id,
        celebrityId = entity.celebrityId,
        celebrityName = entity.celebrityName,
        originalPhotoUrl = entity.originalPhotoUrl,
        signedPhotoUrl = entity.signedPhotoUrl,
        thumbnailUrl = entity.thumbnailUrl,
        title = entity.title,
        description = entity.description,
        status = PhotoStatus.valueOf(entity.status),
        requestId = entity.requestId,
        ownerId = entity.ownerId,
        createdAt = entity.createdAt,
        signedAt = entity.signedAt,
        tags = json.decodeFromString(entity.tags),
        likeCount = entity.likeCount,
        viewCount = entity.viewCount
    )

    fun domainToEntity(photo: SignedPhoto): SignedPhotoEntity = SignedPhotoEntity(
        id = photo.id,
        celebrityId = photo.celebrityId,
        celebrityName = photo.celebrityName,
        originalPhotoUrl = photo.originalPhotoUrl,
        signedPhotoUrl = photo.signedPhotoUrl,
        thumbnailUrl = photo.thumbnailUrl,
        title = photo.title,
        description = photo.description,
        status = photo.status.name,
        requestId = photo.requestId,
        ownerId = photo.ownerId,
        createdAt = photo.createdAt,
        signedAt = photo.signedAt,
        tags = json.encodeToString(photo.tags),
        likeCount = photo.likeCount,
        viewCount = photo.viewCount
    )

    fun dtoToEntity(dto: SignedPhotoDto): SignedPhotoEntity = SignedPhotoEntity(
        id = dto.id,
        celebrityId = dto.celebrityId,
        celebrityName = dto.celebrityName,
        originalPhotoUrl = dto.originalPhotoUrl,
        signedPhotoUrl = dto.signedPhotoUrl,
        thumbnailUrl = dto.thumbnailUrl,
        title = dto.title,
        description = dto.description,
        status = dto.status,
        requestId = dto.requestId,
        ownerId = dto.ownerId,
        createdAt = dto.createdAt,
        signedAt = dto.signedAt,
        tags = json.encodeToString(dto.tags),
        likeCount = dto.likeCount,
        viewCount = dto.viewCount
    )
}
