package com.example.sonorid.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.work.WorkInfo
import com.example.sonorid.ui.theme.SonoridSpacing
import com.example.sonorid.worker.ArtistMetadataDownloadWorker

private enum class MetadataUiPhase { IDLE, RUNNING, FINISHED, CANCELLED, NO_INTERNET, RATE_LIMITED }

private data class MetadataUiProgress(
    val index: Int = 0,
    val total: Int = 0,
    val title: String = "",
    val found: Int = 0,
    val notFound: Int = 0
) {
    val percent: Int get() = if (total == 0) 0 else ((index.toFloat() / total.toFloat()) * 100).toInt()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BulkMetadataScreen(
    onBack: () -> Unit,
    viewModel: BulkMetadataViewModel = hiltViewModel()
) {
    val info by viewModel.workInfo.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()

    LaunchedEffect(Unit) { viewModel.refreshConnectivity() }

    val phase = remember(info) {
        when (info?.state) {
            null -> MetadataUiPhase.IDLE
            WorkInfo.State.ENQUEUED, WorkInfo.State.RUNNING -> MetadataUiPhase.RUNNING
            WorkInfo.State.SUCCEEDED -> MetadataUiPhase.FINISHED
            WorkInfo.State.CANCELLED -> MetadataUiPhase.CANCELLED
            WorkInfo.State.FAILED -> {
                when {
                    info?.outputData?.getBoolean(ArtistMetadataDownloadWorker.KEY_NO_INTERNET, false) == true -> MetadataUiPhase.NO_INTERNET
                    info?.outputData?.getBoolean(ArtistMetadataDownloadWorker.KEY_RATE_LIMITED, false) == true -> MetadataUiPhase.RATE_LIMITED
                    else -> MetadataUiPhase.CANCELLED
                }
            }
            WorkInfo.State.BLOCKED -> MetadataUiPhase.RUNNING
        }
    }

    val progress = remember(info) {
        val data = when (info?.state) {
            WorkInfo.State.RUNNING, WorkInfo.State.ENQUEUED -> info?.progress
            WorkInfo.State.SUCCEEDED, WorkInfo.State.FAILED -> info?.outputData
            else -> null
        }
        MetadataUiProgress(
            index = data?.getInt(ArtistMetadataDownloadWorker.KEY_INDEX, 0) ?: 0,
            total = data?.getInt(ArtistMetadataDownloadWorker.KEY_TOTAL, 0) ?: 0,
            title = data?.getString(ArtistMetadataDownloadWorker.KEY_TITLE) ?: "",
            found = data?.getInt(ArtistMetadataDownloadWorker.KEY_FOUND, 0) ?: 0,
            notFound = data?.getInt(ArtistMetadataDownloadWorker.KEY_NOT_FOUND, 0) ?: 0
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Descargar metadatos") },
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

            when (phase) {
                MetadataUiPhase.RUNNING -> RunningState(progress)
                MetadataUiPhase.FINISHED -> FinishedState(progress)
                MetadataUiPhase.CANCELLED -> CancelledState()
                MetadataUiPhase.NO_INTERNET -> NoInternetState(onRetry = { viewModel.refreshConnectivity() })
                MetadataUiPhase.RATE_LIMITED -> RateLimitedState()
                MetadataUiPhase.IDLE -> IdleState(isConnected = isConnected)
            }

            Spacer(Modifier.weight(1f))

            when (phase) {
                MetadataUiPhase.IDLE, MetadataUiPhase.FINISHED, MetadataUiPhase.CANCELLED, MetadataUiPhase.NO_INTERNET, MetadataUiPhase.RATE_LIMITED -> {
                    Button(
                        onClick = { viewModel.start() },
                        enabled = isConnected,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(SonoridSpacing.Sm))
                        Text(
                            when {
                                !isConnected -> "Sin conexión a internet"
                                phase == MetadataUiPhase.FINISHED || phase == MetadataUiPhase.CANCELLED || phase == MetadataUiPhase.RATE_LIMITED -> "Volver a intentar"
                                else -> "Iniciar descarga"
                            }
                        )
                    }
                }
                MetadataUiPhase.RUNNING -> {
                    OutlinedButton(
                        onClick = { viewModel.cancel() },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Cancelar")
                    }
                }
            }

            Spacer(Modifier.height(SonoridSpacing.Lg))
        }
    }
}

@Composable
private fun IdleState(isConnected: Boolean) {
    HeroIconBadge(icon = Icons.Default.Person)
    Spacer(Modifier.height(SonoridSpacing.Md))
    Text(
        "Descargar datos de tus artistas",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(SonoridSpacing.Sm))
    Text(
        "Sonorid consultará MusicBrainz, Fanart.tv y TheAudioDB para cada artista de tu biblioteca " +
                "y guardará su imagen, biografía, género y país. Los artistas que ya tienen datos guardados " +
                "se omiten automáticamente.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(SonoridSpacing.Lg))

    FlatInfoRow(
        icon = Icons.Default.Notifications,
        text = "La descarga continúa en segundo plano aunque salgas de esta pantalla. " +
                "Te avisaremos con una notificación cuando termine."
    )

    if (!isConnected) {
        FlatInfoRow(
            icon = Icons.Default.WifiOff,
            text = "Esta función necesita conexión a internet. Conéctate y vuelve a intentarlo.",
            isError = true
        )
    }
}

@Composable
private fun RunningState(progress: MetadataUiProgress) {
    Spacer(Modifier.height(SonoridSpacing.Xl))
    Box(contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress = { progress.percent / 100f },
            modifier = Modifier.size(104.dp),
            strokeWidth = 6.dp,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text("${progress.percent}%", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
    Spacer(Modifier.height(SonoridSpacing.Lg))
    Text(
        if (progress.total > 0) "${progress.index} de ${progress.total} artistas" else "Preparando…",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold
    )
    Spacer(Modifier.height(2.dp))
    Text(
        progress.title,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
    Spacer(Modifier.height(SonoridSpacing.Lg))
    Row(horizontalArrangement = Arrangement.spacedBy(SonoridSpacing.Xl)) {
        StatColumn(label = "Encontrados", value = progress.found.toString())
        StatColumn(label = "Sin datos", value = progress.notFound.toString())
    }
    Spacer(Modifier.height(SonoridSpacing.Lg))
    FlatInfoRow(
        icon = Icons.Default.Notifications,
        text = "Puedes salir de esta pantalla; la descarga sigue en segundo plano."
    )
}

@Composable
private fun FinishedState(progress: MetadataUiProgress) {
    HeroIconBadge(icon = Icons.Default.CheckCircle)
    Spacer(Modifier.height(SonoridSpacing.Md))
    Text("Descarga completada", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(SonoridSpacing.Sm))
    Text(
        "Se revisaron ${progress.total} artistas.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(SonoridSpacing.Lg))
    Row(horizontalArrangement = Arrangement.spacedBy(SonoridSpacing.Xl)) {
        StatColumn(label = "Encontrados", value = progress.found.toString())
        StatColumn(label = "Sin datos", value = progress.notFound.toString())
    }
}

@Composable
private fun CancelledState() {
    HeroIconBadge(icon = Icons.Default.CloudOff)
    Spacer(Modifier.height(SonoridSpacing.Md))
    Text("Descarga cancelada", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(SonoridSpacing.Sm))
    Text(
        "Lo que ya se descargó quedó guardado. Puedes retomarlo cuando quieras: los artistas que " +
                "ya tienen datos se omiten automáticamente.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun NoInternetState(onRetry: () -> Unit) {
    HeroIconBadge(icon = Icons.Default.WifiOff)
    Spacer(Modifier.height(SonoridSpacing.Md))
    Text("Se perdió la conexión", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(SonoridSpacing.Sm))
    Text(
        "La descarga se detuvo porque no hay internet. Lo ya descargado quedó guardado. " +
                "Conéctate de nuevo para continuar.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(SonoridSpacing.Lg))
    TextButton(onClick = onRetry) {
        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(SonoridSpacing.Xs))
        Text("Comprobar conexión")
    }
}

@Composable
private fun RateLimitedState() {
    HeroIconBadge(icon = Icons.Default.HourglassEmpty)
    Spacer(Modifier.height(SonoridSpacing.Md))
    Text("Demasiadas peticiones", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(SonoridSpacing.Sm))
    Text(
        "TheAudioDB limita cuántas veces se puede consultar por minuto y se alcanzó ese límite. " +
                "Lo ya descargado quedó guardado. Espera unos minutos y vuelve a intentarlo.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
}

/** Fila plana sin fondo: icono pequeño + texto, mismo lenguaje visual de las
 * filas de Ajustes/Acerca de. Sustituye al bloque de color usado antes. */
@Composable
private fun FlatInfoRow(icon: ImageVector, text: String, isError: Boolean = false) {
    val contentColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = SonoridSpacing.Sm),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(16.dp).padding(top = 2.dp)
        )
        Spacer(Modifier.width(SonoridSpacing.Sm))
        Text(text = text, style = MaterialTheme.typography.bodySmall, color = contentColor)
    }
}

/** Mismo badge circular que el ícono de cabecera de AboutScreen. */
@Composable
private fun HeroIconBadge(icon: ImageVector, tint: Color = MaterialTheme.colorScheme.onSurface) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainer),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(32.dp))
    }
}

@Composable
private fun StatColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}