package com.autografr.app.data.mapper

import com.autografr.app.data.local.entity.UserEntity
import com.autografr.app.data.remote.dto.UserDto
import com.autografr.app.domain.model.Celebrity
import com.autografr.app.domain.model.Fan
import com.autografr.app.domain.model.User
import com.autografr.app.domain.model.UserRole
import com.autografr.app.domain.model.VerificationStatus

object UserMapper {

    fun dtoToDomain(dto: UserDto): User = User(
        id = dto.id,
        email = dto.email,
        displayName = dto.displayName,
        profileImageUrl = dto.profileImageUrl,
        role = UserRole.valueOf(dto.role),
        bio = dto.bio,
        createdAt = dto.createdAt
    )

    fun dtoToCelebrity(dto: UserDto): Celebrity = Celebrity(
        user = dtoToDomain(dto),
        verificationStatus = dto.verificationStatus?.let { VerificationStatus.valueOf(it) }
            ?: VerificationStatus.UNVERIFIED,
        category = dto.category ?: "",
        autographPrice = dto.autographPrice ?: 0.0,
        isAcceptingRequests = dto.isAcceptingRequests ?: true,
        totalAutographs = dto.totalAutographs ?: 0,
        rating = dto.rating ?: 0f,
        socialLinks = dto.socialLinks ?: emptyMap()
    )

    fun dtoToFan(dto: UserDto): Fan = Fan(
        user = dtoToDomain(dto),
        favoriteCategories = dto.favoriteCategories ?: emptyList(),
        totalPurchases = dto.totalPurchases ?: 0,
        collectionCount = dto.collectionCount ?: 0
    )

    fun domainToDto(user: User): UserDto = UserDto(
        id = user.id,
        email = user.email,
        displayName = user.displayName,
        profileImageUrl = user.profileImageUrl,
        role = user.role.name,
        bio = user.bio,
        createdAt = user.createdAt
    )

    fun celebrityToDto(celebrity: Celebrity): UserDto = UserDto(
        id = celebrity.user.id,
        email = celebrity.user.email,
        displayName = celebrity.user.displayName,
        profileImageUrl = celebrity.user.profileImageUrl,
        role = celebrity.user.role.name,
        bio = celebrity.user.bio,
        createdAt = celebrity.user.createdAt,
        verificationStatus = celebrity.verificationStatus.name,
        category = celebrity.category,
        autographPrice = celebrity.autographPrice,
        isAcceptingRequests = celebrity.isAcceptingRequests,
        totalAutographs = celebrity.totalAutographs,
        rating = celebrity.rating,
        socialLinks = celebrity.socialLinks
    )

    fun entityToDomain(entity: UserEntity): User = User(
        id = entity.id,
        email = entity.email,
        displayName = entity.displayName,
        profileImageUrl = entity.profileImageUrl,
        role = UserRole.valueOf(entity.role),
        bio = entity.bio,
        createdAt = entity.createdAt
    )

    fun domainToEntity(user: User): UserEntity = UserEntity(
        id = user.id,
        email = user.email,
        displayName = user.displayName,
        profileImageUrl = user.profileImageUrl,
        role = user.role.name,
        bio = user.bio,
        createdAt = user.createdAt
    )

    fun dtoToEntity(dto: UserDto): UserEntity = UserEntity(
        id = dto.id,
        email = dto.email,
        displayName = dto.displayName,
        profileImageUrl = dto.profileImageUrl,
        role = dto.role,
        bio = dto.bio,
        createdAt = dto.createdAt,
        verificationStatus = dto.verificationStatus,
        category = dto.category,
        autographPrice = dto.autographPrice,
        isAcceptingRequests = dto.isAcceptingRequests,
        totalAutographs = dto.totalAutographs,
        rating = dto.rating
    )
}
