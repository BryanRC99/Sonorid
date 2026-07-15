// app/src/main/java/com/example/sonorid/ui/common/ArtistImage.kt
package com.example.sonorid.ui.common

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage

/**
 * Imagen de artista circular. El tamaño lo define [modifier] (ej. desde
 * un grid con fillMaxWidth+aspectRatio) o, si no se especifica ancho/alto
 * en el modifier, el parámetro [size] como fallback para usos puntuales
 * (ej. dentro de una fila de lista).
 */
@Composable
fun ArtistImage(
    artistName: String,
    imageUrl: String?,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    val effectiveModifier = if (modifier == Modifier) {
        modifier.size(size)
    } else {
        modifier
    }

    if (imageUrl.isNullOrBlank()) {
        InitialsAvatar(name = artistName, modifier = effectiveModifier)
    } else {
        SubcomposeAsyncImage(
            model = imageUrl,
            contentDescription = artistName,
            contentScale = ContentScale.Crop,
            modifier = effectiveModifier.clip(CircleShape),
            loading = { InitialsAvatar(name = artistName, modifier = Modifier.fillMaxSize()) },
            error = { InitialsAvatar(name = artistName, modifier = Modifier.fillMaxSize()) }
        )
    }
}