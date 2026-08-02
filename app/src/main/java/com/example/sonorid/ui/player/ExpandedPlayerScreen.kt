// app/src/main/java/com/example/sonorid/ui/player/ExpandedPlayerScreen.kt
package com.example.sonorid.ui.player

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SpeakerNotes
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.Player
import coil.compose.AsyncImage
import com.example.sonorid.domain.model.Lyrics
import com.example.sonorid.domain.model.Song
import com.example.sonorid.playback.PlaybackMetaState
import com.example.sonorid.playback.PlaybackProgress
import com.example.sonorid.ui.common.LocalToastHost
import com.example.sonorid.ui.theme.SonoridExtraShapes
import com.example.sonorid.ui.theme.SonoridSpacing

@Composable
fun ExpandedPlayerScreen(
    playerViewModel: PlayerViewModel,
    onCollapse: () -> Unit,
    lyricsViewModel: LyricsViewModel = hiltViewModel()
) {
    val state by playerViewModel.metaState.collectAsState()
    val progress by playerViewModel.progress.collectAsState()
    val favoriteIds by playerViewModel.favoriteIds.collectAsState()
    val pendingPermission by playerViewModel.pendingPermission.collectAsState()
    val showToast = LocalToastHost.current

    val song = state.currentSong ?: return
    val isFavorite = song.id in favoriteIds

    var lyricsMode by remember { mutableStateOf(false) }
    // Solo aplica en modo letras: alterna al tocar la pantalla (imita el
    // comportamiento "tap to hide/show controls" de la referencia).
    var controlsVisible by remember { mutableStateOf(true) }

    var showEditDialog by remember { mutableStateOf(false) }
    var showDetailsDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showPlaylistSheet by remember { mutableStateOf(false) }
    var showQueueSheet by remember { mutableStateOf(false) }

    val lyrics by lyricsViewModel.lyrics.collectAsState()
    val lyricsLoading by lyricsViewModel.isLoading.collectAsState()

    LaunchedEffect(song.id) { lyricsViewModel.loadIfNeeded(song) }
    LaunchedEffect(lyricsMode) { controlsVisible = true }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            playerViewModel.onPermissionGranted()
        } else {
            playerViewModel.onPermissionDenied()
        }
    }

    LaunchedEffect(pendingPermission) {
        pendingPermission?.let { intentSender ->
            permissionLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
        }
    }

    LaunchedEffect(Unit) {
        playerViewModel.events.collect { event ->
            when (event) {
                is SongActionEvent.Deleted -> {
                    showToast("Canción eliminada")
                    onCollapse()
                }
                is SongActionEvent.Updated -> showToast("Información actualizada")
                is SongActionEvent.Error -> showToast(event.message)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Fondo: negro sólido en modo portada; portada difuminada + scrim en modo letras.
        if (lyricsMode) {
            AsyncImage(
                model = song.albumArtUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(50.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.62f))
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (lyricsMode) {
                        Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { controlsVisible = !controlsVisible }
                    } else {
                        Modifier
                    }
                )
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = SonoridSpacing.Lg)
        ) {
            Spacer(modifier = Modifier.height(SonoridSpacing.Sm))

            if (lyricsMode) {
                LyricsHeader(
                    song = song,
                    isFavorite = isFavorite,
                    onCollapse = onCollapse,
                    onAddToQueue = { playerViewModel.addToQueue(song); showToast("Añadida a la cola") },
                    onAddToPlaylist = { showPlaylistSheet = true },
                    onToggleFavorite = { playerViewModel.toggleFavorite(song.id) },
                    onEditInfo = { showEditDialog = true },
                    onShowDetails = { showDetailsDialog = true },
                    onDelete = { showDeleteDialog = true }
                )
            } else {
                PlayerHeader(
                    onCollapse = onCollapse,
                    isFavorite = isFavorite,
                    onAddToQueue = { playerViewModel.addToQueue(song); showToast("Añadida a la cola") },
                    onAddToPlaylist = { showPlaylistSheet = true },
                    onToggleFavorite = { playerViewModel.toggleFavorite(song.id) },
                    onEditInfo = { showEditDialog = true },
                    onShowDetails = { showDetailsDialog = true },
                    onDelete = { showDeleteDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(SonoridSpacing.Lg))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = lyricsMode,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "playerContent"
                ) { showingLyrics ->
                    if (!showingLyrics) {
                        AsyncImage(
                            model = song.albumArtUri,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .shadow(elevation = 24.dp, shape = SonoridExtraShapes.albumArtLarge)
                                .clip(SonoridExtraShapes.albumArtLarge)
                        )
                    } else {
                        LyricsPane(
                            lyrics = lyrics,
                            isLoading = lyricsLoading,
                            currentPositionMs = progress.positionMs,
                            onSeek = { playerViewModel.seekTo(it) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(SonoridSpacing.Lg))

            // En modo letras el título/artista ya vive en el header de arriba.
            if (!lyricsMode) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            song.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(SonoridSpacing.Xxs))
                        Text(
                            song.artist,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(SonoridSpacing.Sm))
                    IconButton(
                        onClick = { playerViewModel.toggleFavorite(song.id) },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isFavorite) "Quitar de favoritos" else "Agregar a favoritos",
                            tint = if (isFavorite) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(SonoridSpacing.Md))
            }

            AnimatedVisibility(
                visible = !lyricsMode || controlsVisible,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column {
                    PlayerSeekBar(progress = progress, onSeek = { playerViewModel.seekTo(it) })
                    Spacer(modifier = Modifier.height(SonoridSpacing.Md))
                    MainControlsRow(
                        isPlaying = state.isPlaying,
                        onSkipPrevious = { playerViewModel.skipPrevious() },
                        onTogglePlayPause = { playerViewModel.togglePlayPause() },
                        onSkipNext = { playerViewModel.skipNext() }
                    )
                    Spacer(modifier = Modifier.height(SonoridSpacing.Sm))
                    SecondaryControlsRow(
                        shuffleEnabled = state.shuffleEnabled,
                        repeatMode = state.repeatMode,
                        lyricsMode = lyricsMode,
                        onToggleShuffle = { playerViewModel.toggleShuffle() },
                        onCycleRepeat = { playerViewModel.cycleRepeat() },
                        onToggleLyrics = { lyricsMode = !lyricsMode },
                        onOpenQueue = { showQueueSheet = true }
                    )
                }
            }

            // Cuando los controles están ocultos en modo letras, mostramos el
            // crédito a LRCLIB (fuente de las letras) en su lugar.
            AnimatedVisibility(
                visible = lyricsMode && !controlsVisible,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                LrcLibAttribution()
            }

            Spacer(modifier = Modifier.height(SonoridSpacing.Md))
        }
    }

    if (showEditDialog) {
        EditSongInfoDialog(
            song = song,
            onDismiss = { showEditDialog = false },
            onConfirm = { title, artist, album ->
                showEditDialog = false
                playerViewModel.updateSongInfo(song, title, artist, album)
            }
        )
    }
    if (showDetailsDialog) {
        SongDetailsDialog(song = song, onDismiss = { showDetailsDialog = false })
    }
    if (showDeleteDialog) {
        DeleteSongConfirmDialog(
            song = song,
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                showDeleteDialog = false
                playerViewModel.deleteSong(song)
            }
        )
    }
    if (showPlaylistSheet) {
        com.example.sonorid.ui.playlists.AddToPlaylistSheet(
            songId = song.id,
            onDismiss = { showPlaylistSheet = false }
        )
    }
    if (showQueueSheet) {
        QueueSheet(
            queue = state.queue,
            currentSongId = song.id,
            onDismiss = { showQueueSheet = false },
            onSelect = { index -> playerViewModel.seekToQueueItem(index) },
            onMove = { from, to -> playerViewModel.moveQueueItem(from, to) }
        )
    }
}

/** Header modo portada: chevron para colapsar, "REPRODUCIENDO" + badge centrados, menú a la derecha. */
@Composable
private fun PlayerHeader(
    onCollapse: () -> Unit,
    isFavorite: Boolean,
    onAddToQueue: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onToggleFavorite: () -> Unit,
    onEditInfo: () -> Unit,
    onShowDetails: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onCollapse, modifier = Modifier.size(40.dp)) {
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Cerrar")
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "REPRODUCIENDO",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(4.dp))
            FormatBadge()
        }
        SongOverflowButton(
            isFavorite = isFavorite,
            onAddToQueue = onAddToQueue,
            onAddToPlaylist = onAddToPlaylist,
            onToggleFavorite = onToggleFavorite,
            onEditInfo = onEditInfo,
            onShowDetails = onShowDetails,
            onDelete = onDelete
        )
    }
}

/** Header modo letras: chevron + título/artista alineados a la izquierda + menú. */
@Composable
private fun LyricsHeader(
    song: Song,
    isFavorite: Boolean,
    onCollapse: () -> Unit,
    onAddToQueue: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onToggleFavorite: () -> Unit,
    onEditInfo: () -> Unit,
    onShowDetails: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onCollapse, modifier = Modifier.size(40.dp)) {
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Cerrar")
        }
        Spacer(modifier = Modifier.width(SonoridSpacing.Xs))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                song.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                song.artist,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        SongOverflowButton(
            isFavorite = isFavorite,
            onAddToQueue = onAddToQueue,
            onAddToPlaylist = onAddToPlaylist,
            onToggleFavorite = onToggleFavorite,
            onEditInfo = onEditInfo,
            onShowDetails = onShowDetails,
            onDelete = onDelete
        )
    }
}

@Composable
private fun FormatBadge() {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Text(
            "Reproducción local",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = SonoridSpacing.Sm, vertical = 4.dp)
        )
    }
}

/** Fila principal: anterior / play-pause / siguiente, como botones "pastilla" grandes. */
@Composable
private fun MainControlsRow(
    isPlaying: Boolean,
    onSkipPrevious: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SonoridSpacing.Sm)
    ) {
        ControlPillButton(
            icon = Icons.Default.FastRewind,
            contentDescription = "Anterior",
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            iconColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f).height(64.dp),
            onClick = onSkipPrevious
        )
        Crossfade(targetState = isPlaying, modifier = Modifier.weight(1f).height(64.dp), label = "playPauseIcon") { playing ->
            ControlPillButton(
                icon = if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = "Reproducir/Pausar",
                containerColor = MaterialTheme.colorScheme.primary,
                iconColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.fillMaxSize(),
                onClick = onTogglePlayPause
            )
        }
        ControlPillButton(
            icon = Icons.Default.FastForward,
            contentDescription = "Siguiente",
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            iconColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f).height(64.dp),
            onClick = onSkipNext
        )
    }
}

@Composable
private fun ControlPillButton(
    icon: ImageVector,
    contentDescription: String,
    containerColor: Color,
    iconColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(containerColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = contentDescription, tint = iconColor, modifier = Modifier.size(26.dp))
    }
}

/** Fila secundaria: aleatorio, repetir, letras, cola — 4 botones cuadrados pequeños. */
@Composable
private fun SecondaryControlsRow(
    shuffleEnabled: Boolean,
    repeatMode: Int,
    lyricsMode: Boolean,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onToggleLyrics: () -> Unit,
    onOpenQueue: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SonoridSpacing.Sm)
    ) {
        SecondaryIconButton(
            icon = Icons.Default.Shuffle,
            active = shuffleEnabled,
            contentDescription = "Aleatorio",
            modifier = Modifier.weight(1f),
            onClick = onToggleShuffle
        )
        SecondaryIconButton(
            icon = if (repeatMode == Player.REPEAT_MODE_ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
            active = repeatMode != Player.REPEAT_MODE_OFF,
            contentDescription = "Repetir",
            modifier = Modifier.weight(1f),
            onClick = onCycleRepeat
        )
        SecondaryIconButton(
            icon = Icons.Default.SpeakerNotes,
            active = lyricsMode,
            contentDescription = "Letras",
            modifier = Modifier.weight(1f),
            onClick = onToggleLyrics
        )
        SecondaryIconButton(
            icon = Icons.Default.PlaylistPlay,
            active = false,
            contentDescription = "Cola de reproducción",
            modifier = Modifier.weight(1f),
            onClick = onOpenQueue
        )
    }
}

@Composable
private fun SecondaryIconButton(
    icon: ImageVector,
    active: Boolean,
    contentDescription: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun LrcLibAttribution() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = SonoridSpacing.Sm),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.MusicNote,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(SonoridSpacing.Xs))
        Text(
            "Letras proporcionadas por LRCLIB",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Slider con estado de arrastre propio: mientras el usuario arrastra,
 * la posición real que llega por Flow (cada 500ms) NO pisa el valor
 * que se está arrastrando, evitando el "salto/tirón" que tenía antes
 * el remember(progress.positionMs).
 */
@Composable
private fun PlayerSeekBar(
    progress: PlaybackProgress,
    onSeek: (Long) -> Unit
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragPosition by remember { mutableFloatStateOf(0f) }

    val displayedPosition = if (isDragging) dragPosition else progress.positionMs.toFloat()

    Column {
        Slider(
            value = displayedPosition,
            onValueChange = {
                isDragging = true
                dragPosition = it
            },
            onValueChangeFinished = {
                onSeek(dragPosition.toLong())
                isDragging = false
            },
            valueRange = 0f..(progress.durationMs.coerceAtLeast(1L)).toFloat(),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                formatMs(displayedPosition.toLong()),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                formatMs(progress.durationMs),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LyricsPane(
    lyrics: Lyrics?,
    isLoading: Boolean,
    currentPositionMs: Long,
    onSeek: (Long) -> Unit
) {
    when {
        isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        lyrics == null -> EmptyLyricsMessage()
        lyrics.synced.isNotEmpty() -> SyncedLyricsPlayerList(
            lines = lyrics.synced.map { it.timeMs to it.text },
            currentPositionMs = currentPositionMs,
            onSeek = onSeek
        )
        !lyrics.plainText.isNullOrBlank() -> LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = SonoridSpacing.Xl)
        ) {
            item {
                Text(
                    text = lyrics.plainText,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = SonoridSpacing.Sm)
                )
            }
        }
        else -> EmptyLyricsMessage()
    }
}

@Composable
private fun EmptyLyricsMessage() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.MusicNote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(SonoridSpacing.Sm))
            Text(
                "Letras no disponibles",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SyncedLyricsPlayerList(
    lines: List<Pair<Long, String>>,
    currentPositionMs: Long,
    onSeek: (Long) -> Unit
) {
    val listState: LazyListState = rememberLazyListState()
    val activeIndex = remember(lines, currentPositionMs) {
        lines.indexOfLast { it.first <= currentPositionMs }
    }

    LaunchedEffect(activeIndex) {
        if (activeIndex >= 0) {
            listState.animateScrollToItem((activeIndex - 2).coerceAtLeast(0))
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = SonoridSpacing.Xxl)
    ) {
        items(lines.size, key = { it }) { index ->
            val (timeMs, text) = lines[index]
            val isActive = index == activeIndex
            Text(
                text = text.ifBlank { "♪" },
                style = if (isActive) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleMedium,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                color = if (isActive) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSeek(timeMs) }
                    .padding(vertical = SonoridSpacing.Sm, horizontal = SonoridSpacing.Md)
            )
        }
    }
}

private fun formatMs(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}