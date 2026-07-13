// app/src/main/java/com/example/sonorid/ui/player/ExpandedPlayerScreen.kt
package com.example.sonorid.ui.player

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.Player
import coil.compose.AsyncImage
import com.example.sonorid.domain.model.Lyrics
import com.example.sonorid.playback.PlaybackMetaState
import com.example.sonorid.playback.PlaybackProgress // 👈 IMPORTADO: El progreso real de tu MusicController

@Composable
fun ExpandedPlayerScreen(
    state: PlaybackMetaState,
    progress: PlaybackProgress, // 👈 SOLUCIÓN: Agregamos el parámetro de progreso independiente
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
            .background(MaterialTheme.colorScheme.surface)
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCollapse) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Cerrar")
            }
            Text("Reproduciendo", style = MaterialTheme.typography.labelMedium)
            IconButton(onClick = { lyricsMode = !lyricsMode }) {
                Icon(
                    Icons.Default.QueueMusic,
                    contentDescription = "Letras",
                    tint = if (lyricsMode) MaterialTheme.colorScheme.primary else LocalContentColor.current
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Crossfade(targetState = lyricsMode, label = "playerContent") { showingLyrics ->
                if (!showingLyrics) {
                    AsyncImage(
                        model = song.albumArtUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .align(Alignment.Center)
                            .clip(RoundedCornerShape(16.dp))
                    )
                } else {
                    LyricsPane(
                        lyrics = lyrics,
                        isLoading = lyricsLoading,
                        currentPositionMs = progress.positionMs, // 👈 CORREGIDO: Viene de progress
                        onSeek = onSeek
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(song.title, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Start)
        Text(song.artist, style = MaterialTheme.typography.bodyLarge, color = Color.Gray)

        Spacer(modifier = Modifier.height(16.dp))

        // 🛠️ CORREGIDO: Se cambian las referencias de state por las de progress
        var sliderPosition by remember(progress.positionMs) { mutableFloatStateOf(progress.positionMs.toFloat()) }
        Slider(
            value = sliderPosition,
            onValueChange = { sliderPosition = it },
            onValueChangeFinished = { onSeek(sliderPosition.toLong()) },
            valueRange = 0f..(progress.durationMs.coerceAtLeast(1L)).toFloat()
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(formatMs(progress.positionMs), style = MaterialTheme.typography.labelSmall)
            Text(formatMs(progress.durationMs), style = MaterialTheme.typography.labelSmall)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onToggleShuffle) {
                Icon(
                    Icons.Default.Shuffle,
                    contentDescription = "Aleatorio",
                    tint = if (state.shuffleEnabled) MaterialTheme.colorScheme.primary else LocalContentColor.current
                )
            }
            IconButton(onClick = onSkipPrevious) {
                Icon(Icons.Default.SkipPrevious, contentDescription = "Anterior", modifier = Modifier.size(36.dp))
            }
            FilledIconButton(onClick = onTogglePlayPause, modifier = Modifier.size(64.dp)) {
                Icon(
                    if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Reproducir/Pausar",
                    modifier = Modifier.size(32.dp)
                )
            }
            IconButton(onClick = onSkipNext) {
                Icon(Icons.Default.SkipNext, contentDescription = "Siguiente", modifier = Modifier.size(36.dp))
            }
            IconButton(onClick = onCycleRepeat) {
                Icon(
                    when (state.repeatMode) {
                        Player.REPEAT_MODE_ONE -> Icons.Default.RepeatOne
                        else -> Icons.Default.Repeat
                    },
                    contentDescription = "Repetir",
                    tint = if (state.repeatMode != Player.REPEAT_MODE_OFF) MaterialTheme.colorScheme.primary else LocalContentColor.current
                )
            }
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
            CircularProgressIndicator()
        }
        lyrics == null -> EmptyLyricsMessage()
        lyrics.synced.isNotEmpty() -> SyncedLyricsPlayerList(
            lines = lyrics.synced.map { it.timeMs to it.text },
            currentPositionMs = currentPositionMs,
            onSeek = onSeek
        )
        !lyrics.plainText.isNullOrBlank() -> LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Text(
                    text = lyrics.plainText,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(8.dp)
                )
            }
        }
        else -> EmptyLyricsMessage()
    }
}

@Composable
private fun EmptyLyricsMessage() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            "Letras no disponibles",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
        contentPadding = PaddingValues(vertical = 32.dp)
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
                    .padding(vertical = 10.dp, horizontal = 16.dp)
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