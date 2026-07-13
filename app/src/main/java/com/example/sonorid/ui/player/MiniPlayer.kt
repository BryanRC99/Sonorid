// app/src/main/java/com/example/sonorid/ui/player/MiniPlayer.kt
package com.example.sonorid.ui.player

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.sonorid.playback.PlaybackMetaState // 👈 CORREGIDO: Importamos el nuevo estado de metadatos
import com.example.sonorid.playback.PlaybackProgress   // 👈 CORREGIDO: Importamos el nuevo estado de progreso

@Composable
fun MiniPlayer(
    state: PlaybackMetaState,       // 👈 CORREGIDO: Cambiado de PlaybackUiState a PlaybackMetaState
    progressState: PlaybackProgress, // 👈 SOLUCIÓN: Agregamos el progreso independiente para pintar la barra
    onExpand: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val song = state.currentSong ?: return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onExpand() }
    ) {
        // 🛠️ CORREGIDO: Ahora calcula el progreso usando progressState en lugar de state
        val progress = if (progressState.durationMs > 0) {
            progressState.positionMs.toFloat() / progressState.durationMs.toFloat()
        } else 0f

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(2.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = song.albumArtUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(6.dp))
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(song.title, maxLines = 1, style = MaterialTheme.typography.bodyMedium)
                Text(song.artist, maxLines = 1, style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onTogglePlayPause) {
                Icon(
                    if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Reproducir/Pausar"
                )
            }
            IconButton(onClick = onSkipNext) {
                Icon(Icons.Default.SkipNext, contentDescription = "Siguiente")
            }
        }
    }
}