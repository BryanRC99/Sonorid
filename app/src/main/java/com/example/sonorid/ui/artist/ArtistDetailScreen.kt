// app/src/main/java/com/example/sonorid/ui/artist/ArtistDetailScreen.kt
package com.example.sonorid.ui.artist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sonorid.domain.model.Song
import com.example.sonorid.ui.common.ArtistImage
import com.example.sonorid.ui.library.SongRow
import com.example.sonorid.ui.playlists.AddToPlaylistSheet

// ArtistDetailScreen.kt — dentro del @Composable, agrega el nuevo ViewModel
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistDetailScreen(
    artistName: String,
    onBack: () -> Unit,
    onSongClick: (List<Song>, Int) -> Unit,
    viewModel: ArtistDetailViewModel = hiltViewModel(),
    infoViewModel: ArtistInfoViewModel = hiltViewModel()
) {
    val songs by viewModel.songs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val infoMap by infoViewModel.infoMap.collectAsState()
    var sheetSongId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(artistName) {
        viewModel.load(artistName)
        infoViewModel.request(artistName)
    }

    val artistInfo = infoMap[artistName]

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(artistName) },
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
            } else {
                LazyColumn {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            ArtistImage(artistName = artistName, imageUrl = artistInfo?.imageUrl, size = 120.dp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(artistName, style = MaterialTheme.typography.headlineSmall)
                            Text(
                                "${songs.size} canciones" + (artistInfo?.genre?.let { " · $it" } ?: ""),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
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