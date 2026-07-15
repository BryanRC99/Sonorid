// app/src/main/java/com/example/sonorid/ui/playlists/PlaylistsScreen.kt
package com.example.sonorid.ui.playlists

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sonorid.data.repository.PlaylistPreview
import com.example.sonorid.ui.common.AlbumArt
import com.example.sonorid.ui.theme.SonoridExtraShapes
import com.example.sonorid.ui.theme.SonoridSpacing

@Composable
fun PlaylistsScreen(
    onOpenFavorites: () -> Unit,
    onOpenPlaylist: (Long) -> Unit,
    viewModel: PlaylistsViewModel = hiltViewModel()
) {
    val playlists by viewModel.playlists.collectAsState()
    val previews by viewModel.previews.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item { LikedSongsRow(onClick = onOpenFavorites) }
            items(playlists, key = { it.id }) { playlist ->
                val preview = previews[playlist.id]
                PlaylistRow(
                    name = playlist.name,
                    preview = preview,
                    onClick = { onOpenPlaylist(playlist.id) }
                )
            }
            item { Spacer(Modifier.height(SonoridSpacing.Xxxl)) }
        }

        FloatingActionButton(
            onClick = { showCreateDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(SonoridSpacing.Md)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Nueva lista de reproducción")
        }
    }

    if (showCreateDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name ->
                viewModel.createPlaylist(name)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun LikedSongsRow(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = SonoridSpacing.Md, vertical = SonoridSpacing.Sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.tertiary,
                            MaterialTheme.colorScheme.tertiaryContainer
                        ),
                        start = Offset.Zero,
                        end = Offset.Infinite
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Favorite,
                contentDescription = null,
                tint = androidx.compose.ui.graphics.Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(Modifier.width(SonoridSpacing.Sm))
        Column {
            Text("Favoritos", style = MaterialTheme.typography.titleMedium)
            Text(
                "Lista de reproducción",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PlaylistRow(
    name: String,
    preview: PlaylistPreview?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = SonoridSpacing.Md, vertical = SonoridSpacing.Sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PlaylistCoverCollage(
            artUris = preview?.previewArt.orEmpty(),
            modifier = Modifier.size(56.dp)
        )
        Spacer(Modifier.width(SonoridSpacing.Sm))
        Column {
            Text(name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                text = "Lista de reproducción" + (preview?.let { " · ${it.songCount} canciones" } ?: ""),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun PlaylistCoverCollage(
    artUris: List<Uri>,
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = SonoridExtraShapes.albumArt
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        when {
            artUris.isEmpty() -> Icon(
                Icons.Default.QueueMusic,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.Center).fillMaxSize(0.5f)
            )
            artUris.size == 1 -> AlbumArt(
                artUri = artUris[0],
                shape = RectangleShape,
                modifier = Modifier.fillMaxSize()
            )
            else -> Column(modifier = Modifier.fillMaxSize()) {
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    CollageCell(artUris.getOrNull(0), Modifier.weight(1f).fillMaxHeight())
                    CollageCell(artUris.getOrNull(1), Modifier.weight(1f).fillMaxHeight())
                }
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    CollageCell(artUris.getOrNull(2), Modifier.weight(1f).fillMaxHeight())
                    CollageCell(artUris.getOrNull(3), Modifier.weight(1f).fillMaxHeight())
                }
            }
        }
    }
}

@Composable
private fun CollageCell(uri: Uri?, modifier: Modifier) {
    if (uri != null) {
        AlbumArt(artUri = uri, shape = RectangleShape, modifier = modifier)
    } else {
        Box(modifier = modifier.background(MaterialTheme.colorScheme.surface))
    }
}