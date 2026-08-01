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
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.work.WorkInfo
import com.example.sonorid.ui.theme.SonoridSpacing
import com.example.sonorid.worker.LyricsDownloadWorker

private enum class UiPhase { IDLE, RUNNING, FINISHED, CANCELLED, NO_INTERNET }

private data class UiProgress(
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
fun BulkLyricsScreen(
    onBack: () -> Unit,
    viewModel: BulkLyricsViewModel = hiltViewModel()
) {
    val info by viewModel.workInfo.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()

    LaunchedEffect(Unit) { viewModel.refreshConnectivity() }

    val phase = remember(info) {
        when (info?.state) {
            null -> UiPhase.IDLE
            WorkInfo.State.ENQUEUED, WorkInfo.State.RUNNING -> UiPhase.RUNNING
            WorkInfo.State.SUCCEEDED -> UiPhase.FINISHED
            WorkInfo.State.CANCELLED -> UiPhase.CANCELLED
            WorkInfo.State.FAILED -> {
                if (info?.outputData?.getBoolean(LyricsDownloadWorker.KEY_NO_INTERNET, false) == true) {
                    UiPhase.NO_INTERNET
                } else {
                    UiPhase.CANCELLED
                }
            }
            WorkInfo.State.BLOCKED -> UiPhase.RUNNING
        }
    }

    val progress = remember(info) {
        val data = when (info?.state) {
            WorkInfo.State.RUNNING, WorkInfo.State.ENQUEUED -> info?.progress
            WorkInfo.State.SUCCEEDED, WorkInfo.State.FAILED -> info?.outputData
            else -> null
        }
        UiProgress(
            index = data?.getInt(LyricsDownloadWorker.KEY_INDEX, 0) ?: 0,
            total = data?.getInt(LyricsDownloadWorker.KEY_TOTAL, 0) ?: 0,
            title = data?.getString(LyricsDownloadWorker.KEY_TITLE) ?: "",
            found = data?.getInt(LyricsDownloadWorker.KEY_FOUND, 0) ?: 0,
            notFound = data?.getInt(LyricsDownloadWorker.KEY_NOT_FOUND, 0) ?: 0
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Descargar letras") },
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
                UiPhase.RUNNING -> RunningState(progress)
                UiPhase.FINISHED -> FinishedState(progress)
                UiPhase.CANCELLED -> CancelledState()
                UiPhase.NO_INTERNET -> NoInternetState(onRetry = { viewModel.refreshConnectivity() })
                UiPhase.IDLE -> IdleState(isConnected = isConnected)
            }

            Spacer(Modifier.weight(1f))

            when (phase) {
                UiPhase.IDLE, UiPhase.FINISHED, UiPhase.CANCELLED, UiPhase.NO_INTERNET -> {
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
                                phase == UiPhase.FINISHED || phase == UiPhase.CANCELLED -> "Volver a buscar"
                                else -> "Iniciar descarga"
                            }
                        )
                    }
                }
                UiPhase.RUNNING -> {
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
    IconBadge(icon = Icons.Default.Lyrics)
    Spacer(Modifier.height(SonoridSpacing.Md))
    Text(
        "Buscar letras para toda tu biblioteca",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(SonoridSpacing.Sm))
    Text(
        "Sonorid revisará cada canción de tu biblioteca y guardará su letra sincronizada localmente. " +
                "Las canciones que ya tienen letra guardada se omiten automáticamente.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(SonoridSpacing.Md))

    InfoStrip(
        icon = Icons.Default.Notifications,
        text = "La descarga continúa en segundo plano aunque salgas de esta pantalla. " +
                "Te avisaremos con una notificación cuando termine."
    )

    Spacer(Modifier.height(SonoridSpacing.Sm))

    if (!isConnected) {
        InfoStrip(
            icon = Icons.Default.WifiOff,
            text = "Esta función necesita conexión a internet. Conéctate y vuelve a intentarlo.",
            isError = true
        )
    }
}

@Composable
private fun RunningState(progress: UiProgress) {
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
        if (progress.total > 0) "${progress.index} de ${progress.total} canciones" else "Preparando…",
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
        StatColumn(label = "Encontradas", value = progress.found.toString())
        StatColumn(label = "Sin letra", value = progress.notFound.toString())
    }
    Spacer(Modifier.height(SonoridSpacing.Lg))
    InfoStrip(
        icon = Icons.Default.Notifications,
        text = "Puedes salir de esta pantalla; la descarga sigue en segundo plano."
    )
}

@Composable
private fun FinishedState(progress: UiProgress) {
    IconBadge(icon = Icons.Default.CheckCircle)
    Spacer(Modifier.height(SonoridSpacing.Md))
    Text("Descarga completada", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(SonoridSpacing.Sm))
    Text(
        "Se revisaron ${progress.total} canciones.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(SonoridSpacing.Lg))
    Row(horizontalArrangement = Arrangement.spacedBy(SonoridSpacing.Xl)) {
        StatColumn(label = "Encontradas", value = progress.found.toString())
        StatColumn(label = "Sin letra", value = progress.notFound.toString())
    }
}

@Composable
private fun CancelledState() {
    IconBadge(icon = Icons.Default.CloudOff)
    Spacer(Modifier.height(SonoridSpacing.Md))
    Text("Descarga cancelada", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(SonoridSpacing.Sm))
    Text(
        "Lo que ya se descargó quedó guardado. Puedes retomarlo cuando quieras: las canciones que " +
                "ya tienen letra se omiten automáticamente.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun NoInternetState(onRetry: () -> Unit) {
    IconBadge(icon = Icons.Default.WifiOff)
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
private fun InfoStrip(icon: ImageVector, text: String, isError: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(SonoridSpacing.Md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(SonoridSpacing.Sm))
        Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun IconBadge(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(32.dp))
    }
}

@Composable
private fun StatColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}