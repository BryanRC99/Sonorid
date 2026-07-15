// app/src/main/java/com/example/sonorid/ui/album/AlbumDetailScreen.kt
package com.example.sonorid.ui.album

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sonorid.domain.model.Song
import com.example.sonorid.ui.common.AlbumArt
import com.example.sonorid.ui.common.SongOverflowMenu
import com.example.sonorid.ui.common.rememberDominantColor
import com.example.sonorid.ui.playlists.AddToPlaylistSheet
import com.example.sonorid.ui.theme.SonoridExtraShapes
import com.example.sonorid.ui.theme.SonoridSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumDetailScreen(
    albumId: Long,
    onBack: () -> Unit,
    onSongClick: (List<Song>, Int) -> Unit,
    viewModel: AlbumDetailViewModel = hiltViewModel()
) {
    val songs by viewModel.songs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    var sheetSongId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(albumId) { viewModel.load(albumId) }

    val listState = rememberLazyListState()
    val showSolidTopBar by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (songs.isEmpty()) {
            Text("Álbum vacío", modifier = Modifier.align(Alignment.Center))
        } else {
            val first = songs.first()

            LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                item {
                    AlbumHeader(
                        title = first.album,
                        artist = first.artist,
                        artUri = first.albumArtUri,
                        songCount = songs.size,
                        onPlay = { onSongClick(songs, 0) },
                        onShuffle = { onSongClick(songs.shuffled(), 0) }
                    )
                }
                itemsIndexed(songs, key = { _, song -> song.id }) { index, song ->
                    AlbumTrackRow(
                        trackNumber = index + 1,
                        song = song,
                        isFavorite = song.id in favoriteIds,
                        onClick = { onSongClick(songs, index) },
                        onToggleFavorite = { viewModel.toggleFavorite(song.id) },
                        onAddToPlaylist = { sheetSongId = song.id }
                    )
                }
                item { Spacer(Modifier.height(SonoridSpacing.Xxl)) }
            }
        }

        TopAppBar(
            title = {
                AnimatedVisibility(visible = showSolidTopBar, enter = fadeIn(), exit = fadeOut()) {
                    Text(
                        text = songs.firstOrNull()?.album.orEmpty(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(
                                if (showSolidTopBar) Color.Transparent
                                else MaterialTheme.colorScheme.background.copy(alpha = 0.55f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = if (showSolidTopBar) MaterialTheme.colorScheme.background else Color.Transparent
            )
        )
    }

    sheetSongId?.let { songId ->
        AddToPlaylistSheet(songId = songId, onDismiss = { sheetSongId = null })
    }
}

@Composable
private fun AlbumHeader(
    title: String,
    artist: String,
    artUri: android.net.Uri,
    songCount: Int,
    onPlay: () -> Unit,
    onShuffle: () -> Unit
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val fallbackScrim = MaterialTheme.colorScheme.primaryContainer
    val dominantColor = rememberDominantColor(artUri = artUri, fallback = fallbackScrim)
    val animatedScrim by androidx.compose.animation.animateColorAsState(
        targetValue = dominantColor,
        label = "albumScrimColor"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(animatedScrim.copy(alpha = 0.55f), backgroundColor),
                    startY = 0f,
                    endY = Offset.Infinite.y
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 56.dp, start = SonoridSpacing.Lg, end = SonoridSpacing.Lg)
        ) {
            AlbumArt(
                artUri = artUri,
                shape = SonoridExtraShapes.albumArtLarge,
                modifier = Modifier
                    .fillMaxWidth(0.62f)
                    .aspectRatio(1f)
                    .align(Alignment.CenterHorizontally)
                    .shadow(elevation = 20.dp, shape = SonoridExtraShapes.albumArtLarge)
            )

            Spacer(Modifier.height(SonoridSpacing.Lg))

            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(SonoridSpacing.Xs))
            Text(
                text = artist,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Álbum · $songCount canciones",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(SonoridSpacing.Md))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onShuffle) {
                    Icon(
                        Icons.Default.Shuffle,
                        contentDescription = "Aleatorio",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                FilledIconButton(
                    onClick = onPlay,
                    modifier = Modifier
                        .size(56.dp)
                        .shadow(elevation = 8.dp, shape = androidx.compose.foundation.shape.CircleShape),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Reproducir álbum", modifier = Modifier.size(28.dp))
                }
            }

            Spacer(Modifier.height(SonoridSpacing.Sm))
        }
    }
}

@Composable
private fun AlbumTrackRow(
    trackNumber: Int,
    song: Song,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAddToPlaylist: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = SonoridSpacing.Lg, vertical = SonoridSpacing.Sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = trackNumber.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(28.dp),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.width(SonoridSpacing.Sm))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                song.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isFavorite) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                formatDuration(song.duration),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        SongOverflowMenu(
            isFavorite = isFavorite,
            onToggleFavorite = onToggleFavorite,
            onAddToPlaylist = onAddToPlaylist
        )
    }
}

private fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}