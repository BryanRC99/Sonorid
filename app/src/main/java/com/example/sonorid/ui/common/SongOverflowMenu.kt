// app/src/main/java/com/example/sonorid/ui/common/SongOverflowMenu.kt
package com.example.sonorid.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.PlaylistRemove
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sonorid.ui.theme.SonoridExtraShapes

@Composable
fun SongOverflowMenu(
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onAddToPlaylist: () -> Unit,
    modifier: Modifier = Modifier,
    // 🆕 Solo se pasa cuando la fila vive dentro de una playlist real (no en
    // Favoritos ni en pantallas de biblioteca general): agrega la opción
    // "Quitar de esta lista" al menú.
    onRemoveFromPlaylist: (() -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }
    val showToast = LocalToastHost.current

    Box(modifier = modifier) {
        IconButton(onClick = { expanded = true }, modifier = Modifier.size(36.dp)) {
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
                text = {
                    Text(
                        if (isFavorite) "Quitar de favoritos" else "Agregar a favoritos",
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
                onClick = {
                    expanded = false
                    onToggleFavorite()
                    showToast(if (isFavorite) "Quitado de favoritos" else "Agregado a favoritos")
                }
            )
            DropdownMenuItem(
                text = {
                    Text(
                        "Añadir a lista de reproducción",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.PlaylistAdd,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                onClick = {
                    expanded = false
                    onAddToPlaylist()
                }
            )
            if (onRemoveFromPlaylist != null) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                DropdownMenuItem(
                    text = { Text("Quitar de esta lista", color = MaterialTheme.colorScheme.error) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.PlaylistRemove,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    onClick = {
                        expanded = false
                        onRemoveFromPlaylist()
                    }
                )
            }
        }
    }
}