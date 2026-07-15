// app/src/main/java/com/example/sonorid/ui/common/ToastController.kt
package com.example.sonorid.ui.common

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.sonorid.ui.theme.SonoridSpacing

/**
 * Función para disparar un toast desde cualquier pantalla, sin acoplarla
 * a MainScreen. Se provee una implementación real en MainScreen y un
 * no-op por defecto (para previews o si algún composable se usa fuera
 * del árbol de navegación principal).
 */
val LocalToastHost = staticCompositionLocalOf<(String) -> Unit> { {} }

/**
 * Visual del toast: píldora del mismo tono que el fondo de la app (casi
 * negro), con un borde sutil + sombra para que siga siendo distinguible
 * sin dejar de sentirse "parte de la app" en vez de un elemento genérico
 * de sistema.
 */
@Composable
fun SonoridToast(data: SnackbarData) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shadowElevation = 10.dp,
        modifier = Modifier
            .padding(horizontal = SonoridSpacing.Lg, vertical = SonoridSpacing.Sm)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(50)
            )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = SonoridSpacing.Md, vertical = SonoridSpacing.Sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(SonoridSpacing.Xs))
            Text(
                text = data.visuals.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}