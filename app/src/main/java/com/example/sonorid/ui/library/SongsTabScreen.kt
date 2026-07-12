// app/src/main/java/com/example/sonorid/ui/library/SongsTabScreen.kt
package com.example.sonorid.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed // 👈 Cambiado por itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.sonorid.domain.model.Song

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongsTabScreen(
    onOpenFolders: () -> Unit,
    onOpenSearch: () -> Unit,
    onSongClick: (List<Song>, Int) -> Unit,
    modifier: Modifier = Modifier, // 👈 Agregado por buenas prácticas
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val songs by viewModel.songs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var sheetSongId by remember { mutableStateOf<Long?>(null) }
    val favoriteIds by viewModel.favoriteIds.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadSongs()
    }

    sheetSongId?.let { songId ->
        com.example.sonorid.ui.playlists.AddToPlaylistSheet(
            songId = songId,
            onDismiss = { sheetSongId = null }
        )
    }

    Scaffold(
        modifier = modifier, // 👈 Se aplica el modifier aquí
        topBar = {
            TopAppBar(
                title = { Text("Sonorid") },
                actions = {
                    IconButton(onClick = onOpenSearch) {
                        Icon(Icons.Default.Search, contentDescription = "Buscar")
                    }
                    IconButton(onClick = onOpenFolders) {
                        Icon(Icons.Default.Folder, contentDescription = "Elegir carpetas")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (songs.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No se encontraron canciones")
                    TextButton(onClick = onOpenFolders) {
                        Text("Elegir carpetas")
                    }
                }
            } else {
                // 🚀 OPTIMIZACIÓN: LazyColumn usando itemsIndexed
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    itemsIndexed(
                        items = songs,
                        key = { _, song -> song.id } // Usamos el ID único de la canción como clave
                    ) { index, song -> // El índice ya viene calculado directamente de forma eficiente
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
}

