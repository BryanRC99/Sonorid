// app/src/main/java/com/example/sonorid/ui/common/DominantColor.kt
package com.example.sonorid.ui.common

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.palette.graphics.Palette
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult

/**
 * Extrae el color dominante "vibrante" de una imagen (portada de álbum,
 * foto de artista, etc.) usando Palette sobre el bitmap ya cacheado por
 * Coil. Devuelve [fallback] mientras carga o si falla la extracción.
 *
 * Acepta cualquier modelo soportado por Coil (Uri, String/URL, etc.).
 * Si [model] es null (ej. artista sin foto en TheAudioDB), devuelve
 * [fallback] de inmediato sin intentar red.
 */
@Composable
fun rememberDominantColor(
    model: Any?,
    fallback: Color
): Color {
    val context = LocalContext.current
    var color by remember(model) { mutableStateOf(fallback) }

    LaunchedEffect(model) {
        if (model == null) {
            color = fallback
            return@LaunchedEffect
        }
        val request = ImageRequest.Builder(context)
            .data(model)
            .allowHardware(false) // Palette necesita un bitmap software-backed
            .build()

        val result = context.imageLoader.execute(request)
        if (result is SuccessResult) {
            val bitmap = (result.drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
            if (bitmap != null) {
                color = extractDominant(bitmap, fallback)
            }
        }
    }

    return color
}

/** Overload conveniente para Uri (portadas de MediaStore). */
@Composable
fun rememberDominantColor(
    artUri: android.net.Uri,
    fallback: Color
): Color = rememberDominantColor(model = artUri as Any, fallback = fallback)

private suspend fun extractDominant(bitmap: Bitmap, fallback: Color): Color =
    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
        try {
            val palette = Palette.from(bitmap).generate()
            val swatch = palette.vibrantSwatch
                ?: palette.mutedSwatch
                ?: palette.dominantSwatch
            swatch?.let { Color(it.rgb) } ?: fallback
        } catch (e: Exception) {
            fallback
        }
    }