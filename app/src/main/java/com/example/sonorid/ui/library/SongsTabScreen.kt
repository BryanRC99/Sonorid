// app/src/main/java/com/example/sonorid/ui/library/SongsTabScreen.kt
package com.example.sonorid.ui.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sonorid.domain.model.Song
import com.example.sonorid.ui.theme.SonoridSpacing

@Composable
fun SongsTabScreen(
    onOpenSettings: () -> Unit,
    onSongClick: (List<Song>, Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val songs by viewModel.songs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var sheetSongId by remember { mutableStateOf<Long?>(null) }
    val favoriteIds by viewModel.favoriteIds.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadSongs() }

    sheetSongId?.let { songId ->
        com.example.sonorid.ui.playlists.AddToPlaylistSheet(
            songId = songId,
            onDismiss = { sheetSongId = null }
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (songs.isEmpty()) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("No se encontraron canciones")
                TextButton(onClick = onOpenSettings) {
                    Text("Elegir carpetas")
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(
                    items = songs,
                    key = { _, song -> song.id }
                ) { index, song ->
                    SongRow(
                        song = song,
                        isFavorite = song.id in favoriteIds,
                        onClick = { onSongClick(songs, index) },
                        onToggleFavorite = { viewModel.toggleFavorite(song.id) },
                        onAddToPlaylist = { sheetSongId = song.id }
                    )
                }
                item { Spacer(Modifier.height(SonoridSpacing.Xxxl)) }
            }
        }

        if (songs.isNotEmpty()) {
            ExtendedFloatingActionButton(
                onClick = { onSongClick(songs.shuffled(), 0) },
                icon = { Icon(Icons.Default.Shuffle, contentDescription = null) },
                text = { Text("Aleatorio") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(SonoridSpacing.Md)
                    .shadow(elevation = 8.dp, shape = MaterialTheme.shapes.large)
            )
        }
    }
}