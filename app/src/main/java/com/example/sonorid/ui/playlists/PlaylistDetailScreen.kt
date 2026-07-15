// app/src/main/java/com/example/sonorid/ui/playlists/PlaylistDetailScreen.kt
package com.example.sonorid.ui.playlists

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
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
import com.example.sonorid.ui.library.SongRow
import com.example.sonorid.ui.theme.SonoridExtraShapes
import com.example.sonorid.ui.theme.SonoridSpacing

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
    val playlistName by viewModel.playlistName.collectAsState()
    var sheetSongId by remember { mutableStateOf<Long?>(null) }

    val isFavorites = playlistId == null
    val displayName = if (isFavorites) "Favoritos" else (playlistName ?: title)

    LaunchedEffect(playlistId) {
        if (isFavorites) viewModel.loadFavorites() else viewModel.loadPlaylist(playlistId!!)
    }

    val listState = rememberLazyListState()
    val showSolidTopBar by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                item {
                    PlaylistHeader(
                        name = displayName,
                        isFavorites = isFavorites,
                        firstSongArt = songs.firstOrNull()?.albumArtUri,
                        songCount = songs.size,
                        onPlay = { if (songs.isNotEmpty()) onSongClick(songs, 0) },
                        onShuffle = { if (songs.isNotEmpty()) onSongClick(songs.shuffled(), 0) }
                    )
                }
                if (songs.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(SonoridSpacing.Xxl),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Sin canciones todavía",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
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
                item { Spacer(Modifier.height(SonoridSpacing.Xxl)) }
            }
        }

        TopAppBar(
            title = {
                AnimatedVisibility(visible = showSolidTopBar, enter = fadeIn(), exit = fadeOut()) {
                    Text(displayName, maxLines = 1, overflow = TextOverflow.Ellipsis)
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

/**
 * Header hero de lista de reproducción. Favoritos usa el mismo tratamiento
 * de gradiente coral + corazón que su fila en "Tus listas"; una playlist
 * normal usa la carátula de su primera canción como portada + scrim,
 * igual que Album/Artist.
 */
@Composable
private fun PlaylistHeader(
    name: String,
    isFavorites: Boolean,
    firstSongArt: android.net.Uri?,
    songCount: Int,
    onPlay: () -> Unit,
    onShuffle: () -> Unit
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val scrimColor = if (isFavorites) {
        MaterialTheme.colorScheme.tertiary
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(scrimColor.copy(alpha = 0.55f), backgroundColor),
                    startY = 0f,
                    endY = Offset.Infinite.y
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 56.dp, start = SonoridSpacing.Lg, end = SonoridSpacing.Lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isFavorites) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .aspectRatio(1f)
                        .shadow(elevation = 20.dp, shape = SonoridExtraShapes.albumArtLarge)
                        .clip(SonoridExtraShapes.albumArtLarge)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.tertiary,
                                    MaterialTheme.colorScheme.tertiaryContainer
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.fillMaxSize(0.4f)
                    )
                }
            } else if (firstSongArt != null) {
                com.example.sonorid.ui.common.AlbumArt(
                    artUri = firstSongArt,
                    shape = SonoridExtraShapes.albumArtLarge,
                    modifier = Modifier
                        .fillMaxWidth(0.62f)
                        .aspectRatio(1f)
                        .shadow(elevation = 20.dp, shape = SonoridExtraShapes.albumArtLarge)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.62f)
                        .aspectRatio(1f)
                        .clip(SonoridExtraShapes.albumArtLarge)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.PlaylistAdd,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxSize(0.4f)
                    )
                }
            }

            Spacer(Modifier.height(SonoridSpacing.Lg))

            Text(
                text = name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(SonoridSpacing.Xs))
            Text(
                text = "Lista de reproducción · $songCount canciones",
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
                    Icon(Icons.Default.PlayArrow, contentDescription = "Reproducir", modifier = Modifier.size(28.dp))
                }
            }

            Spacer(Modifier.height(SonoridSpacing.Sm))
        }
    }
}