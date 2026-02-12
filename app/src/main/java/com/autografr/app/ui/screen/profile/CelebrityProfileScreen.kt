package com.autografr.app.ui.screen.profile

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.autografr.app.domain.model.VerificationStatus
import com.autografr.app.ui.component.LoadingIndicator
import com.autografr.app.ui.component.UserAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CelebrityProfileScreen(
    celebrityId: String,
    onNavigateToRequest: () -> Unit,
    onNavigateToPhoto: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(celebrityId) {
        viewModel.loadCelebrityProfile(celebrityId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.celebrity?.user?.displayName ?: "Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        val celebrity = uiState.celebrity
        if (celebrity == null && uiState.isLoading) {
            LoadingIndicator()
        } else if (celebrity != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                // Profile header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UserAvatar(
                        imageUrl = celebrity.user.profileImageUrl,
                        size = 80.dp
                    )

                    Column(modifier = Modifier.padding(start = 16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = celebrity.user.displayName,
                                style = MaterialTheme.typography.titleLarge
                            )
                            if (celebrity.verificationStatus == VerificationStatus.VERIFIED) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Verified",
                                    modifier = Modifier
                                        .padding(start = 4.dp)
                                        .size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Text(
                            text = celebrity.category,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${celebrity.totalAutographs} autographs",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (celebrity.user.bio.isNotEmpty()) {
                    Text(
                        text = celebrity.user.bio,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Button(
                    onClick = onNavigateToRequest,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Request Autograph - $${celebrity.autographPrice}")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Portfolio",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(uiState.portfolio, key = { it.id }) { photo ->
                        Card(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clickable { onNavigateToPhoto(photo.id) }
                        ) {
                            AsyncImage(
                                model = photo.thumbnailUrl.ifEmpty { photo.signedPhotoUrl },
                                contentDescription = photo.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        }
    }
}
