// app/src/main/java/com/example/sonorid/ui/playlists/AddToPlaylistSheet.kt
package com.example.sonorid.ui.playlists

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToPlaylistSheet(
    songId: Long,
    onDismiss: () -> Unit,
    viewModel: AddToPlaylistViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val playlists by viewModel.playlists.collectAsState()
    val membership by viewModel.membership.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(songId) { viewModel.loadMembership(songId) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            Text(
                "Agregar a lista",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            ListItem(
                headlineContent = { Text("Nueva lista") },
                leadingContent = { Icon(Icons.Default.Add, contentDescription = null) },
                modifier = Modifier.clickable { showCreateDialog = true }
            )

            LazyColumn {
                items(playlists, key = { it.id }) { playlist ->
                    val checked = playlist.id in membership
                    ListItem(
                        headlineContent = { Text(playlist.name) },
                        trailingContent = {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = { viewModel.toggle(playlist.id, songId) }
                            )
                        },
                        modifier = Modifier.clickable { viewModel.toggle(playlist.id, songId) }
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name ->
                viewModel.createPlaylistAndAdd(name, songId)
                showCreateDialog = false
            }
        )
    }
}