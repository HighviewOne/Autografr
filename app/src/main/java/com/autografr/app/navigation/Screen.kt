package com.autografr.app.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {
    // Auth
    @Serializable data object Login : Screen
    @Serializable data object Register : Screen
    @Serializable data object ProfileSetup : Screen

    // Camera / Gallery
    @Serializable data object Camera : Screen
    @Serializable data object GalleryPicker : Screen

    // Canvas
    @Serializable data class AutographCanvas(
        val photoUri: String,
        val requestId: String? = null
    ) : Screen

    // Requests
    @Serializable data object FanRequest : Screen
    @Serializable data object CelebrityQueue : Screen
    @Serializable data class RequestDetail(val requestId: String) : Screen

    // Profiles
    @Serializable data class CelebrityProfile(val celebrityId: String) : Screen
    @Serializable data object FanProfile : Screen
    @Serializable data object EditProfile : Screen

    // Marketplace
    @Serializable data object Marketplace : Screen
    @Serializable data class ListingDetail(val photoId: String) : Screen
    @Serializable data class CreateListing(val photoId: String) : Screen

    // Feed
    @Serializable data object Feed : Screen

    // Share
    @Serializable data class Share(val photoId: String) : Screen
}
