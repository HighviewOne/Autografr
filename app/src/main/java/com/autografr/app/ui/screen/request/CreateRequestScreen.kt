package com.autografr.app.ui.screen.request

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.autografr.app.ui.component.ErrorDialog
import com.autografr.app.ui.component.LoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRequestScreen(
    celebrityId: String,
    onRequestSent: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: RequestViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var photoUri by rememberSaveable { mutableStateOf<String?>(null) }
    var message by rememberSaveable { mutableStateOf("") }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let { photoUri = it.toString() } }

    LaunchedEffect(celebrityId) {
        viewModel.loadCelebrity(celebrityId)
    }

    val celebrity = uiState.celebrity

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Request Autograph") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            if (celebrity != null) {
                Text(
                    text = celebrity.user.displayName,
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "$${celebrity.autographPrice}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            photoUri?.let { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = "Photo to be signed",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 3f)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            OutlinedButton(
                onClick = {
                    photoPicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (photoUri == null) "Choose a photo to sign" else "Choose a different photo")
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text("Message (optional)") },
                placeholder = { Text("Who should it be made out to?") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(32.dp))

            val acceptingRequests = celebrity?.isAcceptingRequests != false
            if (!acceptingRequests) {
                Text(
                    text = "Not accepting requests right now",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    photoUri?.let { viewModel.createRequest(it, message.trim(), onRequestSent) }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading && celebrity != null && photoUri != null && acceptingRequests
            ) {
                Text("Send Request")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        if (uiState.isLoading) {
            LoadingIndicator()
        }

        uiState.error?.let { error ->
            ErrorDialog(
                message = error,
                onDismiss = { viewModel.clearError() }
            )
        }
    }
}
