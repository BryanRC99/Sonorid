package com.example.sonorid.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val SonoridShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

/** Formas puntuales que no calzan en la escala estándar de M3. */
object SonoridExtraShapes {
    val albumArt = RoundedCornerShape(12.dp)
    val albumArtLarge = RoundedCornerShape(20.dp)
    val pill = RoundedCornerShape(50)
    val bottomSheetTop = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
}