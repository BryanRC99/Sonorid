package com.example.sonorid.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SpatialAudioOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sonorid.ui.theme.SonoridSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioEffectsScreen(
    onBack: () -> Unit,
    viewModel: AudioEffectsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val hasSongPlaying by viewModel.hasSongPlaying.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Efectos de sonido") },
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
        if (!state.isSessionReady) {
            NoSessionState(
                hasSongPlaying = hasSongPlaying,
                onRetry = { viewModel.retryAttach() },
                modifier = Modifier.padding(padding).fillMaxSize()
            )
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                EnabledRow(enabled = state.enabled, onToggle = { viewModel.setEnabled(it) })

                if (state.presets.isNotEmpty()) {
                    PresetRow(
                        presets = state.presets,
                        selectedIndex = state.selectedPresetIndex,
                        enabled = state.enabled,
                        onSelect = { viewModel.applyPreset(it) }
                    )
                }

                SectionLabel("Ecualizador")
                EqualizerBands(
                    bands = state.bands,
                    enabled = state.enabled,
                    onBandChange = { index, level -> viewModel.setBandLevel(index, level) }
                )

                if (state.bassBoostSupported || state.virtualizerSupported) {
                    SectionLabel("Extras")
                }
                if (state.bassBoostSupported) {
                    StrengthSliderRow(
                        title = "Refuerzo de graves",
                        subtitle = "Realza las frecuencias bajas",
                        value = state.bassBoostStrength,
                        enabled = state.enabled,
                        onChange = { viewModel.setBassBoost(it) }
                    )
                }
                if (state.virtualizerSupported) {
                    StrengthSliderRow(
                        title = "Sonido envolvente",
                        subtitle = "Simula una sensación de espacio más amplio en audífonos",
                        value = state.virtualizerStrength,
                        enabled = state.enabled,
                        onChange = { viewModel.setVirtualizer(it) }
                    )
                }

                Spacer(Modifier.height(SonoridSpacing.Md))
                TextButton(
                    onClick = { viewModel.resetToFlat() },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(SonoridSpacing.Xs))
                    Text("Restablecer a plano")
                }
                Spacer(Modifier.height(SonoridSpacing.Xl))
            }
        }
    }
}

@Composable
private fun NoSessionState(hasSongPlaying: Boolean, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(SonoridSpacing.Xl)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Equalizer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(30.dp)
                )
            }
            Spacer(Modifier.height(SonoridSpacing.Md))
            Text(
                "Reproduce algo para activar los efectos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(SonoridSpacing.Sm))
            Text(
                "El ecualizador necesita una canción sonando para engancharse a la sesión de audio.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            if (hasSongPlaying) {
                Spacer(Modifier.height(SonoridSpacing.Lg))
                TextButton(onClick = onRetry) { Text("Reintentar") }
            }
        }
    }
}

@Composable
private fun EnabledRow(enabled: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SonoridSpacing.Lg, vertical = SonoridSpacing.Md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.GraphicEq, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(SonoridSpacing.Md))
        Column(modifier = Modifier.weight(1f)) {
            Text("Activar efectos de audio", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(
                "Ecualizador, graves y sonido envolvente",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = enabled, onCheckedChange = onToggle)
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
}

@Composable
private fun PresetRow(
    presets: List<String>,
    selectedIndex: Int,
    enabled: Boolean,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = SonoridSpacing.Lg, vertical = SonoridSpacing.Sm),
        horizontalArrangement = Arrangement.spacedBy(SonoridSpacing.Sm)
    ) {
        presets.forEachIndexed { index, name ->
            FilterChip(
                selected = index == selectedIndex,
                onClick = { onSelect(index) },
                enabled = enabled,
                label = { Text(name) }
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 0.8.sp,
        modifier = Modifier.padding(horizontal = SonoridSpacing.Lg, vertical = SonoridSpacing.Sm)
    )
}

@Composable
private fun EqualizerBands(
    bands: List<com.example.sonorid.domain.model.EqualizerBand>,
    enabled: Boolean,
    onBandChange: (Short, Short) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SonoridSpacing.Lg, vertical = SonoridSpacing.Sm),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        bands.forEach { band ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${band.level / 100}dB",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(SonoridSpacing.Xs))
                Box(modifier = Modifier.height(160.dp).width(36.dp), contentAlignment = Alignment.Center) {
                    VerticalEqSlider(
                        value = band.level.toFloat(),
                        valueRange = band.minLevel.toFloat()..band.maxLevel.toFloat(),
                        enabled = enabled,
                        onValueChange = { onBandChange(band.index, it.toInt().toShort()) },
                        modifier = Modifier.height(36.dp).width(160.dp)
                    )
                }
                Spacer(Modifier.height(SonoridSpacing.Xs))
                Text(
                    text = formatFrequency(band.centerFreqHz),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** Slider vertical: reutiliza el Slider horizontal de M3 rotado 90°, truco
 * estándar de Compose para no reimplementar el gesto de arrastre a mano. */
@Composable
private fun VerticalEqSlider(
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    enabled: Boolean,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        enabled = enabled,
        modifier = modifier
            .graphicsLayer {
                rotationZ = -90f
                transformOrigin = TransformOrigin(0f, 0f)
            }
            .layout { measurable, constraints ->
                val placeable = measurable.measure(
                    Constraints(
                        minWidth = constraints.minHeight,
                        maxWidth = constraints.maxHeight,
                        minHeight = constraints.minWidth,
                        maxHeight = constraints.maxWidth
                    )
                )
                layout(placeable.height, placeable.width) {
                    placeable.place(-placeable.width, 0)
                }
            },
        colors = SliderDefaults.colors(
            thumbColor = MaterialTheme.colorScheme.primary,
            activeTrackColor = MaterialTheme.colorScheme.primary,
            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    )
}

@Composable
private fun StrengthSliderRow(
    title: String,
    subtitle: String,
    value: Int,
    enabled: Boolean,
    onChange: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = SonoridSpacing.Lg, vertical = SonoridSpacing.Sm)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.SpatialAudioOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(SonoridSpacing.Md))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                "${value / 10}%",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onChange(it.toInt()) },
            valueRange = 0f..1000f,
            enabled = enabled,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(horizontal = SonoridSpacing.Lg))
}

private fun formatFrequency(hz: Int): String =
    if (hz >= 1000) "${hz / 1000}kHz" else "${hz}Hz"