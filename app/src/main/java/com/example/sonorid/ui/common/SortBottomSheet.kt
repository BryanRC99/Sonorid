package com.example.sonorid.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sonorid.ui.theme.SonoridExtraShapes
import com.example.sonorid.ui.theme.SonoridSpacing

/** Botón de "ordenar", mismo tamaño/tono que los demás íconos de acción de la app. */
@Composable
fun SortIconButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(onClick = onClick, modifier = modifier.size(36.dp)) {
        Icon(
            Icons.Default.Sort,
            contentDescription = "Ordenar",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Hoja de selección de orden: mismo patrón visual que las filas de
 * AddToPlaylistSheet (texto + check a la derecha cuando está seleccionada). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SortBottomSheet(
    options: List<T>,
    selected: T,
    labelFor: (T) -> String,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        shape = SonoridExtraShapes.bottomSheetTop
    ) {
        Column(modifier = Modifier.padding(bottom = SonoridSpacing.Lg)) {
            Text(
                "Ordenar por",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = SonoridSpacing.Md, vertical = SonoridSpacing.Sm)
            )
            options.forEach { option ->
                val isSelected = option == selected
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onSelect(option)
                            onDismiss()
                        }
                        .padding(horizontal = SonoridSpacing.Md, vertical = SonoridSpacing.Sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        labelFor(option),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    if (isSelected) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Seleccionado",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}