package com.example.sonorid.domain.model

data class EqualizerBand(
    val index: Short,
    val centerFreqHz: Int,
    val level: Short,
    val minLevel: Short,
    val maxLevel: Short
)

data class AudioEffectsUiState(
    val isSessionReady: Boolean = false,
    val enabled: Boolean = false,
    val bands: List<EqualizerBand> = emptyList(),
    val presets: List<String> = emptyList(),
    val selectedPresetIndex: Int = -1,
    val bassBoostSupported: Boolean = false,
    val bassBoostStrength: Int = 0, // 0..1000
    val virtualizerSupported: Boolean = false,
    val virtualizerStrength: Int = 0 // 0..1000
)

data class AudioEffectsPreferences(
    val enabled: Boolean = false,
    val presetIndex: Int = -1,
    val bandLevels: List<Int> = emptyList(),
    val bassBoost: Int = 0,
    val virtualizer: Int = 0
)