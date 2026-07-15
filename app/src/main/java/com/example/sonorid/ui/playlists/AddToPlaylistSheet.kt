// app/src/main/java/com/example/sonorid/ui/playlists/AddToPlaylistSheet.kt
package com.example.sonorid.ui.playlists

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.example.sonorid.ui.common.LocalToastHost
import com.example.sonorid.ui.theme.SonoridSpacing
import kotlinx.coroutines.launch

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
    val showToast = LocalToastHost.current

    // sheetState explícito: necesitamos esperar a que hide() TERMINE su
    // animación (es suspend) antes de disparar el toast. Si mostráramos
    // el toast al mismo tiempo que onDismiss(), la animación de cierre
    // del sheet (que vive en su propia superficie por encima del contenido)
    // tapa visualmente el toast durante esa transición.
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    fun closeAndToast(message: String) {
        scope.launch {
            sheetState.hide()
            onDismiss()
            showToast(message)
        }
    }

    LaunchedEffect(songId) { viewModel.loadMembership(songId) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.padding(bottom = SonoridSpacing.Lg)) {
            Text(
                "Añadir a lista de reproducción",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = SonoridSpacing.Md, vertical = SonoridSpacing.Sm)
            )

            NewPlaylistRow(onClick = { showCreateDialog = true })

            LazyColumn {
                items(playlists, key = { it.id }) { playlist ->
                    val checked = playlist.id in membership
                    PlaylistSelectRow(
                        name = playlist.name,
                        checked = checked,
                        onClick = {
                            val wasChecked = checked
                            viewModel.toggle(playlist.id, songId)
                            closeAndToast(
                                if (wasChecked) "Quitado de ${playlist.name}"
                                else "Agregado a ${playlist.name}"
                            )
                        }
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
                closeAndToast("Agregado a $name")
            }
        )
    }
}

@Composable
private fun NewPlaylistRow(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = SonoridSpacing.Md, vertical = SonoridSpacing.Sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.width(SonoridSpacing.Sm))
        Text("Nueva lista de reproducción", style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
private fun PlaylistSelectRow(
    name: String,
    checked: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = SonoridSpacing.Md, vertical = SonoridSpacing.Sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.QueueMusic,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(SonoridSpacing.Sm))
        Text(
            name,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        if (checked) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Ya agregado",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}