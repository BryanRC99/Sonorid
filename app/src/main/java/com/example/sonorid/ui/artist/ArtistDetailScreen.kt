// app/src/main/java/com/example/sonorid/ui/artist/ArtistDetailScreen.kt
package com.example.sonorid.ui.artist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
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
import com.example.sonorid.ui.common.ArtistImage
import com.example.sonorid.ui.common.SongOverflowMenu
import com.example.sonorid.ui.common.colorForName
import com.example.sonorid.ui.common.rememberDominantColor
import com.example.sonorid.ui.playlists.AddToPlaylistSheet
import com.example.sonorid.ui.theme.SonoridSpacing

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

    val listState = rememberLazyListState()
    val showSolidTopBar by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                item {
                    ArtistHeader(
                        artistName = artistName,
                        imageUrl = artistInfo?.imageUrl,
                        genre = artistInfo?.genre,
                        songCount = songs.size,
                        onPlay = { if (songs.isNotEmpty()) onSongClick(songs, 0) },
                        onShuffle = { if (songs.isNotEmpty()) onSongClick(songs.shuffled(), 0) }
                    )
                }
                items(songs, key = { it.id }) { song ->
                    val index = songs.indexOf(song)
                    ArtistSongRow(
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
                    Text(artistName, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
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
private fun ArtistHeader(
    artistName: String,
    imageUrl: String?,
    genre: String?,
    songCount: Int,
    onPlay: () -> Unit,
    onShuffle: () -> Unit
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val nameBasedColor = remember(artistName) { colorForName(artistName) }
    val dominantColor = rememberDominantColor(model = imageUrl, fallback = nameBasedColor)
    val animatedScrim by androidx.compose.animation.animateColorAsState(
        targetValue = dominantColor,
        label = "artistScrimColor"
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
                .padding(top = 56.dp, start = SonoridSpacing.Lg, end = SonoridSpacing.Lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ArtistImage(
                artistName = artistName,
                imageUrl = imageUrl,
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .aspectRatio(1f)
                    .shadow(elevation = 20.dp, shape = CircleShape)
            )

            Spacer(Modifier.height(SonoridSpacing.Lg))

            Text(
                text = artistName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = buildString {
                    append("$songCount canciones")
                    if (!genre.isNullOrBlank()) append(" · $genre")
                },
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
                        .shadow(elevation = 8.dp, shape = CircleShape),
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

@Composable
private fun ArtistSongRow(
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
        Column(modifier = Modifier.weight(1f)) {
            Text(
                song.title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                song.album,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        SongOverflowMenu(
            isFavorite = isFavorite,
            onToggleFavorite = onToggleFavorite,
            onAddToPlaylist = onAddToPlaylist
        )
    }
}