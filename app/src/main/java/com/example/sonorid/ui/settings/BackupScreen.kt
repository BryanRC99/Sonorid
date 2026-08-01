package com.example.sonorid.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sonorid.ui.theme.SonoridSpacing
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    onBack: () -> Unit,
    viewModel: BackupViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    val fileName = remember {
        val stamp = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        "sonorid_backup_$stamp.json"
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> uri?.let { viewModel.exportTo(it) } }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { viewModel.importFrom(it) } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Copia de seguridad") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = SonoridSpacing.Lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(SonoridSpacing.Xl))

            when (val current = state) {
                is BackupUiState.Working -> WorkingState()
                is BackupUiState.ExportSuccess -> ResultState(
                    icon = Icons.Default.CheckCircle,
                    title = "Copia exportada",
                    message = "Se guardó \"${current.fileName}\". Guárdalo en un lugar seguro (Drive, correo, etc.).",
                    onDismiss = viewModel::reset
                )
                is BackupUiState.ImportSuccess -> ResultState(
                    icon = Icons.Default.CheckCircle,
                    title = "Copia restaurada",
                    message = buildString {
                        append("${current.summary.favoritesRestored} favoritos y ")
                        append("${current.summary.playlistsRestored} listas restauradas.")
                        if (current.summary.favoritesSkipped > 0 || current.summary.songsSkipped > 0) {
                            append(
                                "\n\n${current.summary.favoritesSkipped + current.summary.songsSkipped} " +
                                        "canciones no se encontraron en este dispositivo y se omitieron."
                            )
                        }
                    },
                    onDismiss = viewModel::reset
                )
                is BackupUiState.Error -> ResultState(
                    icon = Icons.Default.ErrorOutline,
                    title = "Algo salió mal",
                    message = current.message,
                    isError = true,
                    onDismiss = viewModel::reset
                )
                BackupUiState.Idle -> IdleContent(
                    onExport = { exportLauncher.launch(fileName) },
                    onImport = { importLauncher.launch(arrayOf("application/json")) }
                )
            }

            Spacer(Modifier.height(SonoridSpacing.Xl))
        }
    }
}

@Composable
private fun IdleContent(onExport: () -> Unit, onImport: () -> Unit) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.SdStorage,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(30.dp)
        )
    }
    Spacer(Modifier.height(SonoridSpacing.Md))
    Text(
        "Tus listas y favoritos, a salvo",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(SonoridSpacing.Sm))
    Text(
        "Tus listas de reproducción y favoritos se guardan solo en este dispositivo. Exporta una copia " +
                "para no perderlos si cambias de teléfono, reinstalas la app o borras sus datos.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )

    Spacer(Modifier.height(SonoridSpacing.Xl))

    BackupActionRow(
        icon = Icons.Default.CloudUpload,
        title = "Exportar copia de seguridad",
        subtitle = "Guarda un archivo .json con tus listas y favoritos",
        onClick = onExport
    )
    Spacer(Modifier.height(SonoridSpacing.Sm))
    BackupActionRow(
        icon = Icons.Default.CloudDownload,
        title = "Restaurar copia de seguridad",
        subtitle = "Selecciona un archivo .json exportado anteriormente",
        onClick = onImport
    )

    Spacer(Modifier.height(SonoridSpacing.Lg))
    Text(
        "Nota: al restaurar, las canciones se buscan primero por su identificador y, si no coincide, " +
                "por título, artista y álbum. Las que no se encuentren en este dispositivo se omiten.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun BackupActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable(onClick = onClick)
            .padding(horizontal = SonoridSpacing.Md, vertical = SonoridSpacing.Md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(SonoridSpacing.Md))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun WorkingState() {
    Spacer(Modifier.height(SonoridSpacing.Xxl))
    CircularProgressIndicator(color = MaterialTheme.colorScheme.onSurface)
    Spacer(Modifier.height(SonoridSpacing.Md))
    Text("Procesando…", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun ResultState(
    icon: ImageVector,
    title: String,
    message: String,
    isError: Boolean = false,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(30.dp)
        )
    }
    Spacer(Modifier.height(SonoridSpacing.Md))
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(SonoridSpacing.Sm))
    Text(
        message,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(SonoridSpacing.Lg))
    TextButton(onClick = onDismiss) { Text("Aceptar") }
}