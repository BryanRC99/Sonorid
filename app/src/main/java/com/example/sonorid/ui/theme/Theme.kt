package com.example.sonorid.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val SonoridColorScheme = darkColorScheme(
    primary = Mint,
    onPrimary = MintOn,
    primaryContainer = Color(0xFF155237),
    onPrimaryContainer = Color(0xFFAAFFD0),
    secondary = TextSecondary,
    background = Ink,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextSecondary,
    surfaceContainerHigh = SurfaceMuted,
    outline = Outline,
    error = Error
)

private val SonoridShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun SonoridTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SonoridColorScheme,
        typography = Typography,
        shapes = SonoridShapes,
        content = content
    )
}
