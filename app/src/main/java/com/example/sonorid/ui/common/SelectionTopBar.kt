// SelectionTopBar.kt
package com.example.sonorid.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sonorid.ui.theme.SonoridSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionTopBar(
    selectedCount: Int,
    totalCount: Int,
    onClose: () -> Unit,
    onToggleSelectAll: () -> Unit,
    actions: @Composable () -> Unit
) {
    val allSelected = totalCount > 0 && selectedCount == totalCount

    Column {
        TopAppBar(
            title = {
                Text(
                    "$selectedCount seleccionadas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            navigationIcon = {
                IconButton(onClick = onClose) {
                    CircleIconBadge {
                        Icon(Icons.Default.Close, contentDescription = "Cancelar selección")
                    }
                }
            },
            actions = {
                IconButton(onClick = onToggleSelectAll) {
                    CircleIconBadge {
                        Icon(
                            if (allSelected) Icons.Default.ClearAll else Icons.Default.SelectAll,
                            contentDescription = if (allSelected) "Deseleccionar todas" else "Seleccionar todas"
                        )
                    }
                }
                actions()
                Spacer(Modifier.width(SonoridSpacing.Xs))
            },
            windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
    }
}

/** Mismo lenguaje visual que el botón "back" circular de Album/Artist/Playlist detail. */
@Composable
private fun CircleIconBadge(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}