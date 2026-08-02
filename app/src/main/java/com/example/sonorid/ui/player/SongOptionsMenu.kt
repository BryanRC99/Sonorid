// app/src/main/java/com/example/sonorid/ui/player/SongOptionsMenu.kt
package com.example.sonorid.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.sonorid.domain.model.Song
import com.example.sonorid.ui.common.AlbumArt
import com.example.sonorid.ui.theme.SonoridExtraShapes
import com.example.sonorid.ui.theme.SonoridSpacing
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState


/** Botón de tres puntos + menú desplegable con las acciones de la canción. */
@Composable
fun SongOverflowButton(
    isFavorite: Boolean,
    onAddToQueue: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onToggleFavorite: () -> Unit,
    onEditInfo: () -> Unit,
    onShowDetails: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        IconButton(onClick = { expanded = true }, modifier = Modifier.size(40.dp)) {
            Icon(
                Icons.Default.MoreVert,
                contentDescription = "Más opciones",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = SonoridExtraShapes.menu,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 0.dp,
            shadowElevation = 6.dp
        ) {
            DropdownMenuItem(
                text = { Text("Añadir a la cola", color = MaterialTheme.colorScheme.onSurface) },
                leadingIcon = {
                    Icon(Icons.Default.QueueMusic, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                onClick = { expanded = false; onAddToQueue() }
            )
            DropdownMenuItem(
                text = { Text("Añadir a lista de reproducción", color = MaterialTheme.colorScheme.onSurface) },
                leadingIcon = {
                    Icon(Icons.Default.PlaylistAdd, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                onClick = { expanded = false; onAddToPlaylist() }
            )
            DropdownMenuItem(
                text = {
                    Text(
                        if (isFavorite) "Quitar de favoritos" else "Añadir a favoritos",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                leadingIcon = {
                    Icon(
                        if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                onClick = { expanded = false; onToggleFavorite() }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            DropdownMenuItem(
                text = { Text("Editar información", color = MaterialTheme.colorScheme.onSurface) },
                leadingIcon = {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                onClick = { expanded = false; onEditInfo() }
            )
            DropdownMenuItem(
                text = { Text("Detalles de la canción", color = MaterialTheme.colorScheme.onSurface) },
                leadingIcon = {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                onClick = { expanded = false; onShowDetails() }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            DropdownMenuItem(
                text = { Text("Eliminar archivo", color = MaterialTheme.colorScheme.error) },
                leadingIcon = {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                },
                onClick = { expanded = false; onDelete() }
            )
        }
    }
}

@Composable
fun EditSongInfoDialog(
    song: Song,
    onDismiss: () -> Unit,
    onConfirm: (title: String, artist: String, album: String) -> Unit
) {
    var title by remember { mutableStateOf(song.title) }
    var artist by remember { mutableStateOf(song.artist) }
    var album by remember { mutableStateOf(song.album) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar información") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(SonoridSpacing.Sm)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = artist,
                    onValueChange = { artist = it },
                    label = { Text("Artista") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = album,
                    onValueChange = { album = it },
                    label = { Text("Álbum") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(title.trim(), artist.trim(), album.trim()) },
                enabled = title.isNotBlank()
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun SongDetailsDialog(song: Song, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Detalles de la canción") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(SonoridSpacing.Xs)) {
                DetailRow("Título", song.title)
                DetailRow("Artista", song.artist)
                DetailRow("Álbum", song.album)
                DetailRow("Género", song.genre ?: "Desconocido")
                DetailRow("Pista", if (song.trackNumber > 0) song.trackNumber.toString() else "—")
                DetailRow("Duración", formatDetailDuration(song.duration))
                DetailRow("ID en el sistema", song.id.toString())
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.width(SonoridSpacing.Sm))
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun formatDetailDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

@Composable
fun DeleteSongConfirmDialog(
    song: Song,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
        title = { Text("¿Eliminar archivo?") },
        text = {
            Text("Se eliminará \"${song.title}\" permanentemente del dispositivo. Esta acción no se puede deshacer.")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Eliminar", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

/**
 * Hoja de la cola de reproducción: muestra carátula de cada canción y
 * permite reordenar arrastrando desde el handle de la derecha (Spotify-like),
 * usando la librería Reorderable para animación y auto-scroll.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueSheet(
    queue: List<Song>,
    currentSongId: Long?,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit,
    onMove: (from: Int, to: Int) -> Unit
) {
    val lazyListState = androidx.compose.foundation.lazy.rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        onMove(from.index, to.index)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        shape = SonoridExtraShapes.bottomSheetTop,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = SonoridSpacing.Sm)
                    .size(width = 32.dp, height = 4.dp)
                    .background(
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        RoundedCornerShape(50)
                    )
            )
        }
    ) {
        Column(
            modifier = Modifier.padding(horizontal = SonoridSpacing.Md, vertical = SonoridSpacing.Sm)
        ) {
            Text(
                "Cola de reproducción",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (queue.isNotEmpty()) {
                Text(
                    "${queue.size} canciones · mantén y arrastra para reordenar",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (queue.isEmpty()) {
            Text(
                "La cola está vacía",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(SonoridSpacing.Md)
            )
        }

        LazyColumn(state = lazyListState, modifier = Modifier.heightIn(max = 420.dp)) {
            itemsIndexed(queue, key = { _, song -> song.id }) { index, song ->
                ReorderableItem(reorderableState, key = song.id) { isDragging ->
                    val isCurrent = song.id == currentSongId
                    val rowShape = RoundedCornerShape(14.dp)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = SonoridSpacing.Sm, vertical = 3.dp)
                            .clip(rowShape)
                            .background(
                                if (isDragging) MaterialTheme.colorScheme.surfaceContainerHigh
                                else MaterialTheme.colorScheme.surfaceContainer
                            )
                            .then(
                                if (isDragging) {
                                    Modifier.border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                        shape = rowShape
                                    )
                                } else Modifier
                            )
                            .clickable { onSelect(index); onDismiss() }
                            .padding(horizontal = SonoridSpacing.Sm, vertical = SonoridSpacing.Xs),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            AlbumArt(
                                artUri = song.albumArtUri,
                                modifier = Modifier.size(44.dp)
                            )
                            if (isCurrent) {
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clip(SonoridExtraShapes.albumArt)
                                        .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.45f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Equalizer,
                                        contentDescription = "Reproduciendo",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.width(SonoridSpacing.Sm))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                song.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
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
                        Spacer(Modifier.width(SonoridSpacing.Xs))

                        Icon(
                            Icons.Default.DragHandle,
                            contentDescription = "Reordenar",
                            tint = if (isDragging) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(24.dp)
                                .draggableHandle()
                        )
                    }
                }
            }
            item { Spacer(Modifier.height(SonoridSpacing.Lg)) }
        }
    }
}