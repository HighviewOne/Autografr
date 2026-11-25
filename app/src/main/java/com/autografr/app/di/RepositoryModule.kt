package com.autografr.app.di

import com.autografr.app.data.ai.GeminiListingAssistant
import com.autografr.app.data.repository.AuthRepositoryImpl
import com.autografr.app.data.repository.MarketplaceRepositoryImpl
import com.autografr.app.data.repository.PhotoRepositoryImpl
import com.autografr.app.data.repository.RequestRepositoryImpl
import com.autografr.app.data.repository.SafetyRepositoryImpl
import com.autografr.app.data.repository.UserRepositoryImpl
import com.autografr.app.domain.repository.AuthRepository
import com.autografr.app.domain.repository.ListingAssistant
import com.autografr.app.domain.repository.MarketplaceRepository
import com.autografr.app.domain.repository.PhotoRepository
import com.autografr.app.domain.repository.RequestRepository
import com.autografr.app.domain.repository.SafetyRepository
import com.autografr.app.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindPhotoRepository(impl: PhotoRepositoryImpl): PhotoRepository

    @Binds
    @Singleton
    abstract fun bindRequestRepository(impl: RequestRepositoryImpl): RequestRepository

    @Binds
    @Singleton
    abstract fun bindMarketplaceRepository(impl: MarketplaceRepositoryImpl): MarketplaceRepository

    @Binds
    @Singleton
    abstract fun bindSafetyRepository(impl: SafetyRepositoryImpl): SafetyRepository

    @Binds
    @Singleton
    abstract fun bindListingAssistant(impl: GeminiListingAssistant): ListingAssistant
}
