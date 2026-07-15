// app/src/main/java/com/example/sonorid/ui/player/ExpandedPlayerScreen.kt
package com.example.sonorid.ui.player

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.Player
import coil.compose.AsyncImage
import com.example.sonorid.domain.model.Lyrics
import com.example.sonorid.playback.PlaybackMetaState
import com.example.sonorid.playback.PlaybackProgress
import com.example.sonorid.ui.theme.SonoridExtraShapes
import com.example.sonorid.ui.theme.SonoridSizes
import com.example.sonorid.ui.theme.SonoridSpacing

@Composable
fun ExpandedPlayerScreen(
    state: PlaybackMetaState,
    progress: PlaybackProgress,
    onCollapse: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    lyricsViewModel: LyricsViewModel = hiltViewModel()
) {
    val song = state.currentSong ?: return

    var lyricsMode by remember { mutableStateOf(false) }
    val lyrics by lyricsViewModel.lyrics.collectAsState()
    val lyricsLoading by lyricsViewModel.isLoading.collectAsState()

    LaunchedEffect(song.id) { lyricsViewModel.loadIfNeeded(song) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            // 👈 antes usaba colorScheme.surface (más claro que el fondo del resto
            // de la app); con .background aquí el reproductor queda del mismo
            // negro que el resto y no se percibe una "costura" al expandirlo.
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = SonoridSpacing.Lg)
    ) {
        Spacer(modifier = Modifier.height(SonoridSpacing.Sm))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCollapse, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Cerrar")
            }
            Text(
                text = if (lyricsMode) "LETRAS" else "REPRODUCIENDO",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            IconButton(onClick = { lyricsMode = !lyricsMode }, modifier = Modifier.size(40.dp)) {
                Icon(
                    Icons.Default.QueueMusic,
                    contentDescription = "Letras",
                    tint = if (lyricsMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
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
                        onSeek = onSeek
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(SonoridSpacing.Lg))

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

        Spacer(modifier = Modifier.height(SonoridSpacing.Md))

        PlayerSeekBar(progress = progress, onSeek = onSeek)

        Spacer(modifier = Modifier.height(SonoridSpacing.Sm))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onToggleShuffle) {
                Icon(
                    Icons.Default.Shuffle,
                    contentDescription = "Aleatorio",
                    tint = if (state.shuffleEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onSkipPrevious) {
                Icon(
                    Icons.Default.SkipPrevious,
                    contentDescription = "Anterior",
                    modifier = Modifier.size(SonoridSizes.PlayerControlIcon)
                )
            }
            FilledIconButton(
                onClick = onTogglePlayPause,
                modifier = Modifier
                    .size(SonoridSizes.PlayPauseButton)
                    .shadow(elevation = 8.dp, shape = CircleShape)
            ) {
                Crossfade(targetState = state.isPlaying, label = "expandedPlayPause") { playing ->
                    Icon(
                        if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Reproducir/Pausar",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            IconButton(onClick = onSkipNext) {
                Icon(
                    Icons.Default.SkipNext,
                    contentDescription = "Siguiente",
                    modifier = Modifier.size(SonoridSizes.PlayerControlIcon)
                )
            }
            IconButton(onClick = onCycleRepeat) {
                Icon(
                    when (state.repeatMode) {
                        Player.REPEAT_MODE_ONE -> Icons.Default.RepeatOne
                        else -> Icons.Default.Repeat
                    },
                    contentDescription = "Repetir",
                    tint = if (state.repeatMode != Player.REPEAT_MODE_OFF) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(SonoridSpacing.Md))
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