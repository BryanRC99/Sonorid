// app/src/main/java/com/example/sonorid/ui/settings/AboutScreen.kt
package com.example.sonorid.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sonorid.ui.common.icons.GitHubIcon
import com.example.sonorid.ui.theme.SonoridSpacing

private const val GITHUB_URL = "https://github.com/" // 🔧 reemplazar por la URL real del repo

/** Rediseño minimalista: sin tarjetas, sin color morado. Todo plano sobre
 * negro, con la misma lógica de secciones + filas que Ajustes. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    fun openUrl(url: String) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Acerca de") },
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
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(SonoridSpacing.Lg))

            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = SonoridSpacing.Lg),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(30.dp)
                    )
                }
                Spacer(Modifier.height(SonoridSpacing.Md))
                Text(
                    "SONORID",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "Versión 1.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(SonoridSpacing.Md))
                Text(
                    "Reproductor de música local con letras sincronizadas, portadas e información de " +
                            "artistas. Funciona sin conexión y sin cuentas: tu biblioteca vive solo en tu teléfono.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(SonoridSpacing.Xl))

            AboutSectionLabel("Créditos")
            SettingsLikeGroup {
                AboutRow(
                    icon = Icons.Default.Lyrics,
                    title = "LRCLIB",
                    subtitle = "Letras sincronizadas de las canciones",
                    onClick = { openUrl("https://lrclib.net/") }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.background)
                AboutRow(
                    icon = Icons.Default.Person,
                    title = "TheAudioDB",
                    subtitle = "Fotos, géneros y biografías de artistas",
                    onClick = { openUrl("https://www.theaudiodb.com/") }
                )
            }

            Spacer(Modifier.height(SonoridSpacing.Lg))

            AboutSectionLabel("Código abierto")
            Text(
                "Sonorid es un proyecto de código abierto. Si tienes ideas, encontraste un error o " +
                        "quieres contribuir, eres bienvenido a hacerlo en el repositorio de GitHub.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = SonoridSpacing.Lg)
            )
            Spacer(Modifier.height(SonoridSpacing.Sm))
            SettingsLikeGroup {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { openUrl(GITHUB_URL) }
                        .padding(horizontal = SonoridSpacing.Lg, vertical = SonoridSpacing.Md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = GitHubIcon,
                        contentDescription = "GitHub",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(SonoridSpacing.Md))
                    Text(
                        "Ver repositorio en GitHub",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(Modifier.height(SonoridSpacing.Xxl))

            Text(
                "Hecho por Bryan Pineda",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(SonoridSpacing.Xxl))
        }
    }
}

@Composable
private fun AboutSectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 0.8.sp,
        modifier = Modifier.padding(horizontal = SonoridSpacing.Lg, vertical = SonoridSpacing.Sm)
    )
}

@Composable
private fun SettingsLikeGroup(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer),
        content = content
    )
}

@Composable
private fun AboutRow(
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
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(SonoridSpacing.Md))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(18.dp)
        )
    }
}