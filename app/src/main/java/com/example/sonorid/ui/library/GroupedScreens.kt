package com.example.sonorid.ui.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
@Composable
fun AlbumsScreen(viewModel: LibraryViewModel = hiltViewModel()) {
    val songs by viewModel.songs.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadSongs() }
    val albums = songs.groupBy { it.albumId to it.album }

    LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.fillMaxSize().padding(8.dp)) {
        items(albums.entries.toList()) { (key, tracks) ->
            Column(modifier = Modifier.padding(8.dp)) {
                AsyncImage(
                    model = tracks.first().albumArtUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(8.dp))
                )
                Text(key.second, maxLines = 1, style = MaterialTheme.typography.bodyMedium)
                Text("${tracks.size} canciones", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

// GroupedScreens.kt — reemplaza ArtistsScreen completa
@Composable
fun ArtistsScreen(
    onArtistClick: (String) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel(),
    infoViewModel: com.example.sonorid.ui.artist.ArtistInfoViewModel = hiltViewModel()
) {
    val songs by viewModel.songs.collectAsState()
    val infoMap by infoViewModel.infoMap.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadSongs() }
    val artists = songs.groupBy { it.artist }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(artists.entries.toList()) { (artist, tracks) ->
            LaunchedEffect(artist) { infoViewModel.request(artist) }
            val imageUrl = infoMap[artist]?.imageUrl

            ListItem(
                leadingContent = {
                    com.example.sonorid.ui.common.ArtistImage(artistName = artist, imageUrl = imageUrl)
                },
                headlineContent = { Text(artist) },
                supportingContent = { Text("${tracks.size} canciones") },
                modifier = Modifier.clickable { onArtistClick(artist) }
            )
        }
    }
}

@Composable
fun GenresScreen() {
    // MediaStore.Audio.Genres requiere una query aparte; placeholder por ahora.
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
        Text("Géneros — próximamente")
    }
}
