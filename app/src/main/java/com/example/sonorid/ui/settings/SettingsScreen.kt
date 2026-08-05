// app/src/main/java/com/example/sonorid/ui/settings/SettingsScreen.kt
package com.example.sonorid.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sonorid.ui.theme.SonoridSpacing

/**
 * Ajustes: lista plana sin fondos ni tarjetas por fila, coherente con el
 * resto de la app (negro puro + acentos en blanco/gris). Las secciones
 * se distinguen solo por su etiqueta y un divisor fino entre filas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenFolders: () -> Unit,
    onOpenBulkLyrics: () -> Unit,
    onOpenBulkMetadata: () -> Unit,
    onOpenBackup: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenAudioEffects: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajustes") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Spacer(Modifier.height(SonoridSpacing.Sm))

            SettingsSectionLabel("Biblioteca")
            SettingsRow(
                icon = Icons.Default.Folder,
                title = "Carpetas de música",
                subtitle = "Elige desde dónde se cargan tus canciones",
                onClick = onOpenFolders
            )

            Spacer(Modifier.height(SonoridSpacing.Lg))

            SettingsSectionLabel("Sonido")
            SettingsRow(
                icon = Icons.Default.GraphicEq,
                title = "Efectos de audio",
                subtitle = "Ecualizador, amplificador de graves y virtualizador",
                onClick = onOpenAudioEffects
            )

            Spacer(Modifier.height(SonoridSpacing.Lg))

            SettingsSectionLabel("Letras y artistas")
            SettingsRow(
                icon = Icons.Default.Download,
                title = "Descargar letras faltantes",
                subtitle = "Busca y guarda letras para toda tu biblioteca · requiere internet",
                onClick = onOpenBulkLyrics
            )
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(start = 52.dp)
            )
            SettingsRow(
                icon = Icons.Default.Person,
                title = "Descargar metadatos de artistas",
                subtitle = "Imágenes, biografía y géneros desde MusicBrainz, Fanart.tv y TheAudioDB · requiere internet",
                onClick = onOpenBulkMetadata
            )

            Spacer(Modifier.height(SonoridSpacing.Lg))

            SettingsSectionLabel("Datos")
            SettingsRow(
                icon = Icons.Default.SdStorage,
                title = "Copia de seguridad",
                subtitle = "Exporta o restaura tus listas y favoritos",
                onClick = onOpenBackup
            )

            Spacer(Modifier.height(SonoridSpacing.Lg))

            SettingsSectionLabel("Aplicación")
            SettingsRow(
                icon = Icons.Default.Info,
                title = "Acerca de",
                subtitle = "Créditos, código abierto y versión",
                onClick = onOpenAbout
            )

            Spacer(Modifier.height(SonoridSpacing.Lg))
        }
    }
}

@Composable
private fun SettingsSectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 0.8.sp,
        modifier = Modifier.padding(
            horizontal = SonoridSpacing.Lg,
            vertical = SonoridSpacing.Sm
        )
    )
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = SonoridSpacing.Lg, vertical = SonoridSpacing.Md),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(SonoridSpacing.Md))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(2.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.width(SonoridSpacing.Xs))
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(18.dp)
        )
    }
}