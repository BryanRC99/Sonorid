// MainActivity.kt
package com.example.sonorid

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.sonorid.ui.main.MainScreen
import com.example.sonorid.ui.theme.SonoridSpacing
import com.example.sonorid.ui.theme.SonoridTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SonoridTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current
                    val audioPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Manifest.permission.READ_MEDIA_AUDIO
                    } else {
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    }

                    fun isGranted(permission: String): Boolean = ContextCompat.checkSelfPermission(
                        context, permission
                    ) == PackageManager.PERMISSION_GRANTED

                    var audioGranted by remember { mutableStateOf(isGranted(audioPermission)) }
                    var notifGranted by remember {
                        mutableStateOf(
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                isGranted(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                true // No aplica antes de Android 13
                            }
                        )
                    }
                    var requestedOnce by remember { mutableStateOf(false) }

                    // 🛠️ FIX: si el permiso de audio YA estaba concedido (sesión anterior),
                    // arrancamos directo mostrando la biblioteca, sin pasar por el onboarding.
                    // Antes showLibrary siempre arrancaba en false, así que la pantalla de
                    // permisos se veía en cada apertura de la app aunque ya estuviera todo ok.
                    var showLibrary by remember { mutableStateOf(audioGranted) }

                    val audioLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.RequestPermission()
                    ) { granted -> audioGranted = granted }

                    val notifLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.RequestPermission()
                    ) { granted -> notifGranted = granted }

                    // Se pide el permiso esencial (audio) automáticamente una sola vez al entrar,
                    // solo si todavía no está concedido.
                    LaunchedEffect(audioGranted) {
                        if (!audioGranted && !requestedOnce) {
                            requestedOnce = true
                            audioLauncher.launch(audioPermission)
                        }
                    }

                    if (showLibrary) {
                        MainScreen()
                    } else {
                        PermissionOnboardingScreen(
                            audioGranted = audioGranted,
                            notifGranted = notifGranted,
                            showNotifPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU,
                            onGrantAudio = { audioLauncher.launch(audioPermission) },
                            onGrantNotif = { notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                            onStart = { if (audioGranted) showLibrary = true }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionOnboardingScreen(
    audioGranted: Boolean,
    notifGranted: Boolean,
    showNotifPermission: Boolean,
    onGrantAudio: () -> Unit,
    onGrantNotif: () -> Unit,
    onStart: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = SonoridSpacing.Lg, vertical = SonoridSpacing.Md)
            ) {
                Button(
                    onClick = onStart,
                    enabled = audioGranted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(50)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(SonoridSpacing.Sm))
                    Text(
                        text = if (audioGranted) "COMENZAR" else "CONCEDE LOS PERMISOS PARA CONTINUAR",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = SonoridSpacing.Lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(SonoridSpacing.Xxl))

            // 🛠️ Ícono menos llamativo: sin gradiente, un contenedor sutil
            // en surfaceContainer con una simple nota musical en el color primario.
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(Modifier.height(SonoridSpacing.Lg))

            Text(
                text = "SONORID",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Tu música, sin límites",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(SonoridSpacing.Xl))

            InfoCard()

            Spacer(Modifier.height(SonoridSpacing.Lg))

            PermissionsCard(
                audioGranted = audioGranted,
                notifGranted = notifGranted,
                showNotifPermission = showNotifPermission,
                onGrantAudio = onGrantAudio,
                onGrantNotif = onGrantNotif
            )

            Spacer(Modifier.height(SonoridSpacing.Xxl))
        }
    }
}

@Composable
private fun InfoCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(SonoridSpacing.Md)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(SonoridSpacing.Xs))
                Text(
                    "ACERCA DE SONORID",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
            }
            Spacer(Modifier.height(SonoridSpacing.Sm))
            Text(
                "Sonorid reproduce la música guardada en tu dispositivo, con letras sincronizadas, " +
                        "portadas de álbum e información de artistas. Todo funciona sin conexión y sin cuentas: " +
                        "tu biblioteca y tus listas viven únicamente en tu teléfono.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PermissionsCard(
    audioGranted: Boolean,
    notifGranted: Boolean,
    showNotifPermission: Boolean,
    onGrantAudio: () -> Unit,
    onGrantNotif: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(SonoridSpacing.Md)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Shield,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(SonoridSpacing.Xs))
                Text(
                    "PERMISOS DEL SISTEMA",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
            }

            Spacer(Modifier.height(SonoridSpacing.Sm))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

            PermissionRow(
                icon = Icons.Default.MusicNote,
                title = "Música y audio",
                subtitle = "Necesario para encontrar y reproducir las canciones guardadas en tu dispositivo.",
                granted = audioGranted,
                onGrant = onGrantAudio
            )

            if (showNotifPermission) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                PermissionRow(
                    icon = Icons.Default.Notifications,
                    title = "Notificaciones",
                    subtitle = "Muestra los controles de reproducción en la barra de notificaciones.",
                    granted = notifGranted,
                    onGrant = onGrantNotif
                )
            }
        }
    }
}

@Composable
private fun PermissionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    granted: Boolean,
    onGrant: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = SonoridSpacing.Sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(
                    if (granted) MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f)
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (granted) Icons.Default.Check else icon,
                contentDescription = null,
                tint = if (granted) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(SonoridSpacing.Sm))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(2.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.width(SonoridSpacing.Xs))
        if (granted) {
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f)
            ) {
                Text(
                    "CONCEDIDO",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = SonoridSpacing.Sm, vertical = SonoridSpacing.Xs)
                )
            }
        } else {
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(onClick = onGrant)
            ) {
                Text(
                    "CONCEDER",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = SonoridSpacing.Sm, vertical = SonoridSpacing.Xs)
                )
            }
        }
    }
}