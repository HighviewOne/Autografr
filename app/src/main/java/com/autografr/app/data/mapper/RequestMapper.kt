package com.autografr.app.data.mapper

import com.autografr.app.data.local.entity.RequestEntity
import com.autografr.app.data.remote.dto.RequestDto
import com.autografr.app.domain.model.AutographRequest
import com.autografr.app.domain.model.RequestStatus

object RequestMapper {

    fun dtoToDomain(dto: RequestDto): AutographRequest = AutographRequest(
        id = dto.id,
        fanId = dto.fanId,
        fanName = dto.fanName,
        celebrityId = dto.celebrityId,
        celebrityName = dto.celebrityName,
        photoUrl = dto.photoUrl,
        message = dto.message,
        status = RequestStatus.valueOf(dto.status),
        price = dto.price,
        signedPhotoId = dto.signedPhotoId,
        createdAt = dto.createdAt,
        updatedAt = dto.updatedAt,
        expiresAt = dto.expiresAt
    )

    fun domainToDto(request: AutographRequest): RequestDto = RequestDto(
        id = request.id,
        fanId = request.fanId,
        fanName = request.fanName,
        celebrityId = request.celebrityId,
        celebrityName = request.celebrityName,
        photoUrl = request.photoUrl,
        message = request.message,
        status = request.status.name,
        price = request.price,
        signedPhotoId = request.signedPhotoId,
        createdAt = request.createdAt,
        updatedAt = request.updatedAt,
        expiresAt = request.expiresAt
    )

    fun entityToDomain(entity: RequestEntity): AutographRequest = AutographRequest(
        id = entity.id,
        fanId = entity.fanId,
        fanName = entity.fanName,
        celebrityId = entity.celebrityId,
        celebrityName = entity.celebrityName,
        photoUrl = entity.photoUrl,
        message = entity.message,
        status = RequestStatus.valueOf(entity.status),
        price = entity.price,
        signedPhotoId = entity.signedPhotoId,
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt,
        expiresAt = entity.expiresAt
    )

    fun domainToEntity(request: AutographRequest): RequestEntity = RequestEntity(
        id = request.id,
        fanId = request.fanId,
        fanName = request.fanName,
        celebrityId = request.celebrityId,
        celebrityName = request.celebrityName,
        photoUrl = request.photoUrl,
        message = request.message,
        status = request.status.name,
        price = request.price,
        signedPhotoId = request.signedPhotoId,
        createdAt = request.createdAt,
        updatedAt = request.updatedAt,
        expiresAt = request.expiresAt
    )

    fun dtoToEntity(dto: RequestDto): RequestEntity = RequestEntity(
        id = dto.id,
        fanId = dto.fanId,
        fanName = dto.fanName,
        celebrityId = dto.celebrityId,
        celebrityName = dto.celebrityName,
        photoUrl = dto.photoUrl,
        message = dto.message,
        status = dto.status,
        price = dto.price,
        signedPhotoId = dto.signedPhotoId,
        createdAt = dto.createdAt,
        updatedAt = dto.updatedAt,
        expiresAt = dto.expiresAt
    )
}
