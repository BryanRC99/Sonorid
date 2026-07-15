package com.example.sonorid.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val SonoridDarkColorScheme = darkColorScheme(
    primary = SonoridPrimary,
    onPrimary = SonoridOnPrimary,
    primaryContainer = SonoridPrimaryContainer,
    onPrimaryContainer = SonoridOnPrimaryContainer,

    secondary = SonoridSecondary,
    onSecondary = SonoridOnSecondary,
    secondaryContainer = SonoridSecondaryContainer,
    onSecondaryContainer = SonoridOnSecondaryContainer,

    tertiary = SonoridTertiary,
    onTertiary = SonoridOnTertiary,
    tertiaryContainer = SonoridTertiaryContainer,
    onTertiaryContainer = SonoridOnTertiaryContainer,

    error = SonoridError,
    onError = SonoridOnError,
    errorContainer = SonoridErrorContainer,
    onErrorContainer = SonoridOnErrorContainer,

    background = SonoridBackground,
    onBackground = SonoridOnBackground,

    surface = SonoridSurface,
    onSurface = SonoridOnSurface,
    surfaceVariant = SonoridSurfaceVariant,
    onSurfaceVariant = SonoridOnSurfaceVariant,

    outline = SonoridOutline,
    outlineVariant = SonoridOutlineVariant
)

private val SonoridLightColorScheme = lightColorScheme(
    primary = SonoridLightPrimary,
    background = SonoridLightBackground,
    onBackground = SonoridLightOnBackground,
    surface = SonoridLightSurface
)

/**
 * Tema de Sonorid. Por defecto siempre oscuro (como Spotify/YT Music),
 * sin depender de Material You dinámico: la identidad visual es propia.
 */
@Composable
fun SonoridTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) SonoridDarkColorScheme else SonoridLightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as? Activity ?: return@SideEffect
            val window = activity.window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = SonoridShapes,
        content = content
    )
}