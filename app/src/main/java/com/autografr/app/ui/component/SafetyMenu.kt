package com.autografr.app.ui.component

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.autografr.app.domain.model.ReportReason
import com.autografr.app.domain.model.ReportTargetType
import com.autografr.app.ui.screen.safety.SafetyViewModel

/** Something the user can report: a photo, a user, or a request. [userId] is who posted it. */
data class ReportTarget(
    val type: ReportTargetType,
    val id: String,
    val userId: String,
    val menuLabel: String
)

/** Someone the user can block from this screen. */
data class BlockTarget(
    val userId: String,
    val displayName: String,
    /** Shown instead of the name when it isn't known yet, e.g. "seller". */
    val fallbackName: String = "user"
)

/**
 * Top-bar overflow menu with Report and Block / Unblock. Hides itself when there's nothing
 * to act on (for example, the user's own photo or profile).
 */
@Composable
fun SafetyMenu(
    report: ReportTarget?,
    blockTargets: List<BlockTarget>,
    viewModel: SafetyViewModel = hiltViewModel()
) {
    val me = viewModel.currentUserId
    val blocked by viewModel.blockedUsers.collectAsStateWithLifecycle()
    val busy by viewModel.busy.collectAsStateWithLifecycle()
    val blockedIds = remember(blocked) { blocked.map { it.userId }.toSet() }

    val reportable = report?.takeIf { it.userId.isNotBlank() && it.userId != me }
    val blockable = blockTargets.filter { it.userId.isNotBlank() && it.userId != me }.distinctBy { it.userId }
    if (reportable == null && blockable.isEmpty()) return

    var menuOpen by remember { mutableStateOf(false) }
    var showReport by rememberSaveable { mutableStateOf(false) }
    var confirmBlock by remember { mutableStateOf<BlockTarget?>(null) }

    SafetyMessages(viewModel)

    IconButton(onClick = { menuOpen = true }) {
        Icon(Icons.Default.MoreVert, contentDescription = "More options")
    }
    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
        reportable?.let {
            DropdownMenuItem(
                text = { Text(it.menuLabel) },
                onClick = {
                    menuOpen = false
                    showReport = true
                }
            )
        }
        blockable.forEach { target ->
            val name = target.displayName.ifBlank { target.fallbackName }
            if (target.userId in blockedIds) {
                DropdownMenuItem(
                    text = { Text("Unblock $name") },
                    onClick = {
                        menuOpen = false
                        viewModel.unblock(target.userId, target.displayName)
                    }
                )
            } else {
                DropdownMenuItem(
                    text = { Text("Block $name", color = MaterialTheme.colorScheme.error) },
                    onClick = {
                        menuOpen = false
                        confirmBlock = target
                    }
                )
            }
        }
    }

    if (showReport && reportable != null) {
        ReportDialog(
            title = reportable.menuLabel,
            isSending = busy,
            onSend = { reason, details ->
                viewModel.report(reportable.type, reportable.id, reportable.userId, reason, details) {
                    showReport = false
                }
            },
            onDismiss = { showReport = false }
        )
    }

    confirmBlock?.let { target ->
        val name = target.displayName.ifBlank { "this ${target.fallbackName}" }
        AlertDialog(
            onDismissRequest = { confirmBlock = null },
            title = { Text("Block $name?") },
            text = {
                Text(
                    "You won't see their photos, listings or profile. If they're a fan, they can't " +
                        "send you autograph requests. They aren't told. You can unblock them from " +
                        "My Profile > Blocked users."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.block(target.userId, target.displayName)
                    confirmBlock = null
                }) { Text("Block", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirmBlock = null }) { Text("Cancel") } }
        )
    }
}

/** Shows the view model's one-off confirmation / error messages as toasts. */
@Composable
fun SafetyMessages(viewModel: SafetyViewModel) {
    val context = LocalContext.current
    val message by viewModel.message.collectAsStateWithLifecycle()
    LaunchedEffect(message) {
        message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.consumeMessage()
        }
    }
}

@Composable
private fun ReportDialog(
    title: String,
    isSending: Boolean,
    onSend: (ReportReason, String) -> Unit,
    onDismiss: () -> Unit
) {
    var reason by rememberSaveable { mutableStateOf<ReportReason?>(null) }
    var details by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { if (!isSending) onDismiss() },
        title = { Text(title) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text("Why are you reporting this?", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                ReportReason.entries.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = reason == option,
                                enabled = !isSending,
                                role = Role.RadioButton,
                                onClick = { reason = option }
                            )
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = reason == option, onClick = null, enabled = !isSending)
                        Text(option.label, modifier = Modifier.padding(start = 8.dp))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = details,
                    onValueChange = { if (it.length <= 500) details = it },
                    label = { Text("Details (optional)") },
                    supportingText = { Text("${details.length} / 500") },
                    enabled = !isSending,
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { reason?.let { onSend(it, details) } },
                enabled = reason != null && !isSending
            ) {
                if (isSending) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text("Send report")
                }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !isSending) { Text("Cancel") } }
    )
}

/** My Profile > Blocked users. */
@Composable
fun BlockedUsersDialog(
    onDismiss: () -> Unit,
    viewModel: SafetyViewModel = hiltViewModel()
) {
    val blocked by viewModel.blockedUsers.collectAsStateWithLifecycle()
    val busy by viewModel.busy.collectAsStateWithLifecycle()
    SafetyMessages(viewModel)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Blocked users") },
        text = {
            if (blocked.isEmpty()) {
                Text("You haven't blocked anyone.")
            } else {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    blocked.forEach { user ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                user.displayName.ifBlank { "Unknown user" },
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(
                                onClick = { viewModel.unblock(user.userId, user.displayName) },
                                enabled = !busy
                            ) { Text("Unblock") }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } }
    )
}
