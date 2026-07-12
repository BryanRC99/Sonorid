// app/src/main/java/com/example/sonorid/ui/playlists/PlaylistDetailScreen.kt
package com.example.sonorid.ui.playlists

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sonorid.domain.model.Song
import com.example.sonorid.ui.library.SongRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    playlistId: Long?, // null = pantalla de Favoritos
    title: String,
    onBack: () -> Unit,
    onSongClick: (List<Song>, Int) -> Unit,
    viewModel: PlaylistDetailViewModel = hiltViewModel()
) {
    val songs by viewModel.songs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    var sheetSongId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(playlistId) {
        if (playlistId == null) viewModel.loadFavorites() else viewModel.loadPlaylist(playlistId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (songs.isEmpty()) {
                Text("Sin canciones todavía", modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn {
                    items(songs, key = { it.id }) { song ->
                        val index = songs.indexOf(song)
                        SongRow(
                            song = song,
                            isFavorite = song.id in favoriteIds,
                            onClick = { onSongClick(songs, index) },
                            onToggleFavorite = { viewModel.toggleFavorite(song.id) },
                            onAddToPlaylist = { sheetSongId = song.id }
                        )
                    }
                }
            }
        }
    }

    sheetSongId?.let { songId ->
        AddToPlaylistSheet(songId = songId, onDismiss = { sheetSongId = null })
    }
}