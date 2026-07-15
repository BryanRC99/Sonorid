package com.example.sonorid.ui.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.unit.dp
import com.example.sonorid.ui.common.AlbumArt
import com.example.sonorid.ui.common.ArtistImage
import com.example.sonorid.ui.theme.SonoridExtraShapes
import com.example.sonorid.ui.theme.SonoridSpacing

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
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Album,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(Modifier.height(SonoridSpacing.Sm))
                Text(
                    "No se encontraron álbumes",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(SonoridSpacing.Md),
            horizontalArrangement = Arrangement.spacedBy(SonoridSpacing.Md),
            verticalArrangement = Arrangement.spacedBy(SonoridSpacing.Lg),
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
        AlbumArt(
            artUri = album.artUri,
            shape = SonoridExtraShapes.albumArt,
            modifier = Modifier.fillMaxWidth().aspectRatio(1f)
        )
        Spacer(Modifier.height(SonoridSpacing.Sm))
        Text(
            album.title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(2.dp))
        Text(
            album.artist,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Grid circular estilo Spotify (sección "Artistas que sigues"): fotos
 * redondas, nombre centrado debajo, sin texto secundario ni bordes.
 */
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

    if (artists.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(Modifier.height(SonoridSpacing.Sm))
                Text(
                    "No se encontraron artistas",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(SonoridSpacing.Md),
            horizontalArrangement = Arrangement.spacedBy(SonoridSpacing.Sm),
            verticalArrangement = Arrangement.spacedBy(SonoridSpacing.Lg),
            modifier = Modifier.fillMaxSize()
        ) {
            items(artists, key = { (artist, _) -> artist }) { (artist, _) ->
                LaunchedEffect(artist) { infoViewModel.request(artist) }
                val imageUrl = infoMap[artist]?.imageUrl

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onArtistClick(artist) },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ArtistImage(
                        artistName = artist,
                        imageUrl = imageUrl,
                        modifier = Modifier.fillMaxWidth(0.8f).aspectRatio(1f)
                    )
                    Spacer(Modifier.height(SonoridSpacing.Xs))
                    Text(
                        text = artist,
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
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
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                            Icons.Default.LibraryMusic,
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