// app/src/main/java/com/example/sonorid/ui/library/GreetingHeader.kt
package com.example.sonorid.ui.library

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.sonorid.ui.theme.SonoridSpacing
import java.time.LocalTime

/**
 * Saludo dinámico según la hora del día, usado como encabezado del Home.
 * Se calcula una sola vez por composición: no hace falta recomponerlo en
 * vivo, ya que nadie deja la pantalla abierta el tiempo suficiente para
 * que cambie de franja horaria.
 */
@Composable
fun GreetingHeader(modifier: Modifier = Modifier) {
    val greeting = remember { greetingForCurrentTime() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = SonoridSpacing.Lg)
    ) {
        Text(
            text = greeting,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(SonoridSpacing.Sm))
    }
}

private fun greetingForCurrentTime(): String {
    val hour = LocalTime.now().hour
    return when (hour) {
        in 5..11 -> "Buenos días"
        in 12..18 -> "Buenas tardes"
        else -> "Buenas noches"
    }
}