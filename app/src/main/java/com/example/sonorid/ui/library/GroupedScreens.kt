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
import androidx.compose.foundation.clickable
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.example.sonorid.ui.common.AlbumArt
import androidx.compose.material.icons.filled.LibraryMusic
@Composable
fun AlbumsScreen(
    onAlbumClick: (Long) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val songs by viewModel.songs.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadSongs() }

    val albums = remember(songs) {
        songs.groupBy { it.albumId }
            .map { (albumId, tracks) ->
                AlbumSummary(
                    albumId = albumId,
                    title = tracks.first().album,
                    artist = tracks.first().artist,
                    artUri = tracks.first().albumArtUri,
                    songCount = tracks.size
                )
            }
            .sortedBy { it.title.lowercase() }
    }

    if (albums.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            Text("No se encontraron álbumes")
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(albums, key = { it.albumId }) { album ->
                AlbumCard(album = album, onClick = { onAlbumClick(album.albumId) })
            }
        }
    }
}

private data class AlbumSummary(
    val albumId: Long,
    val title: String,
    val artist: String,
    val artUri: android.net.Uri,
    val songCount: Int
)

@Composable
private fun AlbumCard(album: AlbumSummary, onClick: () -> Unit) {
    Column(modifier = Modifier.clickable(onClick = onClick)) {
        Card(
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            AlbumArt(
                artUri = album.artUri,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().aspectRatio(1f)
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            album.title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            album.artist,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            "${album.songCount} canciones",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )
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
    val artists = remember(songs) {
        songs.groupBy { it.artist }
            .toList()
            .sortedBy { (artist, _) -> artist.lowercase() }
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(artists, key = { (artist, _) -> artist }) { (artist, tracks) ->
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
fun GenresScreen(
    onGenreClick: (String) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val songs by viewModel.songs.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadSongs() }

    val genres = remember(songs) {
        songs.groupBy { it.genre ?: "Sin género" }
            .map { (genre, tracks) -> genre to tracks.size }
            .sortedBy { it.first.lowercase() }
    }

    if (genres.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            Text("No se encontraron géneros")
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(genres, key = { it.first }) { (genre, count) ->
                ListItem(
                    headlineContent = { Text(genre) },
                    supportingContent = { Text("$count canciones") },
                    leadingContent = {
                        Icon(
                            androidx.compose.material.icons.Icons.Default.LibraryMusic,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.clickable { onGenreClick(genre) }
                )
                HorizontalDivider()
            }
        }
    }
}
