package com.autografr.app.ui.screen.feed

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Queue
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.autografr.app.domain.model.Celebrity
import com.autografr.app.domain.model.SignedPhoto
import com.autografr.app.ui.component.UserAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    onNavigateToCamera: () -> Unit,
    onNavigateToGallery: () -> Unit,
    onNavigateToMarketplace: () -> Unit,
    onNavigateToQueue: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToPhoto: (String) -> Unit,
    onNavigateToCelebrity: (String) -> Unit,
    viewModel: FeedViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Autografr",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                actions = {
                    if (viewModel.isCelebrity) {
                        IconButton(onClick = onNavigateToCamera) {
                            Icon(Icons.Default.CameraAlt, contentDescription = "Camera")
                        }
                        IconButton(onClick = onNavigateToGallery) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = "Gallery")
                        }
                        IconButton(onClick = onNavigateToQueue) {
                            Icon(Icons.Default.Queue, contentDescription = "Queue")
                        }
                    }
                    IconButton(onClick = onNavigateToMarketplace) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Marketplace")
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Welcome
            item {
                Text(
                    text = "Welcome, ${uiState.currentUser?.displayName ?: ""}",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            // Featured Celebrities
            if (uiState.featuredCelebrities.isNotEmpty()) {
                item {
                    Text(
                        text = "Featured Celebrities",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.featuredCelebrities, key = { it.user.id }) { celebrity ->
                            CelebrityChip(
                                celebrity = celebrity,
                                onClick = { onNavigateToCelebrity(celebrity.user.id) }
                            )
                        }
                    }
                }
            }

            // Trending Autographs
            if (uiState.trending.isNotEmpty()) {
                item {
                    Text(
                        text = "Trending Autographs",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                items(uiState.trending, key = { it.id }) { photo ->
                    TrendingPhotoCard(
                        photo = photo,
                        onClick = { onNavigateToPhoto(photo.id) }
                    )
                }
            }

            // Empty state
            if (uiState.trending.isEmpty() && uiState.featuredCelebrities.isEmpty() && !uiState.isLoading) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(48.dp))
                        Text(
                            text = "No content yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Check the marketplace for autographs!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CelebrityChip(
    celebrity: Celebrity,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(80.dp)
            .clickable(onClick = onClick)
    ) {
        UserAvatar(
            imageUrl = celebrity.user.profileImageUrl,
            size = 64.dp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = celebrity.user.displayName,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun TrendingPhotoCard(
    photo: SignedPhoto,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            AsyncImage(
                model = photo.thumbnailUrl.ifEmpty { photo.signedPhotoUrl },
                contentDescription = photo.title,
                modifier = Modifier
                    .width(100.dp)
                    .aspectRatio(1f),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .weight(1f)
            ) {
                Text(
                    text = photo.title.ifEmpty { "Autographed Photo" },
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "by ${photo.celebrityName.ifEmpty { "Celebrity" }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${photo.likeCount} likes",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
