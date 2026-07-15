// app/src/main/java/com/example/sonorid/ui/player/MiniPlayer.kt
package com.example.sonorid.ui.player

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.sonorid.playback.PlaybackMetaState
import com.example.sonorid.playback.PlaybackProgress
import com.example.sonorid.ui.theme.SonoridExtraShapes
import com.example.sonorid.ui.theme.SonoridSizes
import com.example.sonorid.ui.theme.SonoridSpacing

/** Forma "acoplada": esquinas redondeadas solo arriba, para que se sienta
 *  una sola pieza junto con el navbar de abajo (no un elemento flotante). */
private val DockedShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)

@Composable
fun MiniPlayer(
    state: PlaybackMetaState,
    progress: PlaybackProgress,
    onExpand: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val song = state.currentSong ?: return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 8.dp, shape = DockedShape)
            .clip(DockedShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable { onExpand() }
    ) {
        val progressFraction = if (progress.durationMs > 0) {
            (progress.positionMs.toFloat() / progress.durationMs.toFloat()).coerceIn(0f, 1f)
        } else 0f

        LinearProgressIndicator(
            progress = { progressFraction },
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth().height(2.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SonoridSpacing.Sm, vertical = SonoridSpacing.Xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = song.albumArtUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(SonoridSizes.MiniPlayerArt)
                    .clip(SonoridExtraShapes.albumArt)
            )
            Spacer(modifier = Modifier.width(SonoridSpacing.Sm))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    song.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    song.artist,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(SonoridSpacing.Xs))
            IconButton(onClick = onTogglePlayPause, modifier = Modifier.size(38.dp)) {
                Crossfade(targetState = state.isPlaying, label = "miniPlayPause") { playing ->
                    Icon(
                        if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Reproducir/Pausar"
                    )
                }
            }
            IconButton(onClick = onSkipNext, modifier = Modifier.size(38.dp)) {
                Icon(Icons.Default.SkipNext, contentDescription = "Siguiente")
            }
        }
    }
}
