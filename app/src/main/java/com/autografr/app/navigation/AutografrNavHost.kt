package com.autografr.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.autografr.app.ui.screen.auth.LoginScreen
import com.autografr.app.ui.screen.auth.ProfileSetupScreen
import com.autografr.app.ui.screen.auth.RegisterScreen
import com.autografr.app.ui.screen.camera.CameraScreen
import com.autografr.app.ui.screen.camera.GalleryPickerScreen
import com.autografr.app.ui.screen.canvas.AutographCanvasScreen
import com.autografr.app.ui.screen.feed.FeedScreen
import com.autografr.app.ui.screen.marketplace.CreateListingScreen
import com.autografr.app.ui.screen.marketplace.ListingDetailScreen
import com.autografr.app.ui.screen.marketplace.MarketplaceScreen
import com.autografr.app.ui.screen.profile.CelebrityProfileScreen
import com.autografr.app.ui.screen.profile.EditProfileScreen
import com.autografr.app.ui.screen.profile.FanProfileScreen
import com.autografr.app.ui.screen.request.CelebrityQueueScreen
import com.autografr.app.ui.screen.request.FanRequestScreen
import com.autografr.app.ui.screen.request.RequestDetailScreen
import com.autografr.app.ui.screen.share.ShareScreen

@Composable
fun AutografrNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: Screen = Screen.Login
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Auth
        composable<Screen.Login> {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Screen.Register) },
                onLoginSuccess = {
                    navController.navigate(Screen.Feed) {
                        popUpTo(Screen.Login) { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.Register> {
            RegisterScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(Screen.ProfileSetup) {
                        popUpTo(Screen.Register) { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.ProfileSetup> {
            ProfileSetupScreen(
                onSetupComplete = {
                    navController.navigate(Screen.Feed) {
                        popUpTo(Screen.ProfileSetup) { inclusive = true }
                    }
                }
            )
        }

        // Camera / Gallery
        composable<Screen.Camera> {
            CameraScreen(
                onPhotoCaptured = { uri ->
                    navController.navigate(Screen.AutographCanvas(photoUri = uri))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Screen.GalleryPicker> {
            GalleryPickerScreen(
                onPhotoSelected = { uri ->
                    navController.navigate(Screen.AutographCanvas(photoUri = uri))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Canvas
        composable<Screen.AutographCanvas> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.AutographCanvas>()
            AutographCanvasScreen(
                photoUri = route.photoUri,
                requestId = route.requestId,
                onSaveComplete = { photoId ->
                    navController.navigate(Screen.Share(photoId = photoId)) {
                        popUpTo(Screen.Feed)
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Requests
        composable<Screen.FanRequest> {
            FanRequestScreen(
                onNavigateToCelebrity = { id ->
                    navController.navigate(Screen.CelebrityProfile(celebrityId = id))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Screen.CelebrityQueue> {
            CelebrityQueueScreen(
                onNavigateToDetail = { id ->
                    navController.navigate(Screen.RequestDetail(requestId = id))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Screen.RequestDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.RequestDetail>()
            RequestDetailScreen(
                requestId = route.requestId,
                onAcceptAndSign = { photoUri, requestId ->
                    navController.navigate(
                        Screen.AutographCanvas(photoUri = photoUri, requestId = requestId)
                    )
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Profiles
        composable<Screen.CelebrityProfile> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.CelebrityProfile>()
            CelebrityProfileScreen(
                celebrityId = route.celebrityId,
                onNavigateToRequest = { navController.navigate(Screen.FanRequest) },
                onNavigateToPhoto = { id ->
                    navController.navigate(Screen.ListingDetail(photoId = id))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Screen.FanProfile> {
            FanProfileScreen(
                onNavigateToEdit = { navController.navigate(Screen.EditProfile) },
                onNavigateToPhoto = { id ->
                    navController.navigate(Screen.ListingDetail(photoId = id))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Screen.EditProfile> {
            EditProfileScreen(
                onSaveComplete = { navController.popBackStack() },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Marketplace
        composable<Screen.Marketplace> {
            MarketplaceScreen(
                onNavigateToListing = { id ->
                    navController.navigate(Screen.ListingDetail(photoId = id))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Screen.ListingDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.ListingDetail>()
            ListingDetailScreen(
                photoId = route.photoId,
                onNavigateToCelebrity = { id ->
                    navController.navigate(Screen.CelebrityProfile(celebrityId = id))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Screen.CreateListing> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.CreateListing>()
            CreateListingScreen(
                photoId = route.photoId,
                onListingCreated = {
                    navController.navigate(Screen.Marketplace) {
                        popUpTo(Screen.Feed)
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Feed
        composable<Screen.Feed> {
            FeedScreen(
                onNavigateToCamera = { navController.navigate(Screen.Camera) },
                onNavigateToGallery = { navController.navigate(Screen.GalleryPicker) },
                onNavigateToMarketplace = { navController.navigate(Screen.Marketplace) },
                onNavigateToQueue = { navController.navigate(Screen.CelebrityQueue) },
                onNavigateToProfile = { navController.navigate(Screen.FanProfile) },
                onNavigateToPhoto = { id ->
                    navController.navigate(Screen.ListingDetail(photoId = id))
                },
                onNavigateToCelebrity = { id ->
                    navController.navigate(Screen.CelebrityProfile(celebrityId = id))
                }
            )
        }

        // Share
        composable<Screen.Share> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.Share>()
            ShareScreen(
                photoId = route.photoId,
                onNavigateToFeed = {
                    navController.navigate(Screen.Feed) {
                        popUpTo(Screen.Feed) { inclusive = true }
                    }
                },
                onNavigateToCreateListing = {
                    navController.navigate(Screen.CreateListing(photoId = route.photoId))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
