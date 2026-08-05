package com.example.sonorid.audiofx

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.example.sonorid.data.local.UserPreferencesRepository
import com.example.sonorid.domain.model.AudioEffectsUiState
import com.example.sonorid.domain.model.EqualizerBand
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Administra Equalizer/BassBoost/Virtualizer de android.media.audiofx,
 * enganchados al audioSessionId del ExoPlayer singleton de la app.
 */
@OptIn(UnstableApi::class)
@Singleton
class AudioEffectsController @Inject constructor(
    private val player: ExoPlayer,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    private val scope = CoroutineScope(Dispatchers.Main.immediate)

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var attachedSessionId: Int = C.AUDIO_SESSION_ID_UNSET

    private val _state = MutableStateFlow(AudioEffectsUiState())
    val state: StateFlow<AudioEffectsUiState> = _state.asStateFlow()

    /** Se llama cada vez que hay una canción sonando */
    fun ensureAttached() {
        val sessionId = player.audioSessionId
        if (sessionId == C.AUDIO_SESSION_ID_UNSET) {
            _state.value = _state.value.copy(isSessionReady = false)
            return
        }
        if (sessionId == attachedSessionId && equalizer != null) return

        releaseEffects()
        attachedSessionId = sessionId

        // Algunos fabricantes no exponen ciertos efectos o fallan si falta MODIFY_AUDIO_SETTINGS
        val eq = runCatching { Equalizer(0, sessionId) }.getOrNull()
        val bb = runCatching { BassBoost(0, sessionId) }.getOrNull()
        val vt = runCatching { Virtualizer(0, sessionId) }.getOrNull()
        equalizer = eq
        bassBoost = bb
        virtualizer = vt

        if (eq == null) {
            _state.value = _state.value.copy(isSessionReady = false)
            return
        }

        val range = runCatching { eq.bandLevelRange }.getOrDefault(shortArrayOf(-1500, 1500))
        val bandCount = runCatching { eq.numberOfBands.toInt() }.getOrDefault(0)
        val presetCount = runCatching { eq.numberOfPresets.toInt() }.getOrDefault(0)
        val presetNames = (0 until presetCount).mapNotNull {
            runCatching { eq.getPresetName(it.toShort()) }.getOrNull()
        }
        val bassSupported = bb?.strengthSupported == true
        val virtSupported = vt?.strengthSupported == true

        scope.launch {
            val prefs = userPreferencesRepository.audioEffectsPreferences.first()

            val bands = (0 until bandCount).mapNotNull { i ->
                val band = i.toShort()
                val centerFreq = runCatching { eq.getCenterFreq(band) / 1000 }.getOrNull() ?: return@mapNotNull null
                val savedLevel = prefs.bandLevels.getOrNull(i)?.toShort()
                    ?: runCatching { eq.getBandLevel(band) }.getOrDefault(0)

                EqualizerBand(
                    index = band,
                    centerFreqHz = centerFreq,
                    level = savedLevel,
                    minLevel = range[0],
                    maxLevel = range[1]
                )
            }

            runCatching { eq.enabled = prefs.enabled }
            bands.forEach { band ->
                runCatching { eq.setBandLevel(band.index, band.level) }
            }

            if (bassSupported) runCatching { bb?.setStrength(prefs.bassBoost.toShort()) }
            if (virtSupported) runCatching { vt?.setStrength(prefs.virtualizer.toShort()) }
            runCatching { bb?.enabled = prefs.enabled && bassSupported }
            runCatching { vt?.enabled = prefs.enabled && virtSupported }

            _state.value = AudioEffectsUiState(
                isSessionReady = true,
                enabled = prefs.enabled,
                bands = bands,
                presets = presetNames,
                selectedPresetIndex = prefs.presetIndex,
                bassBoostSupported = bassSupported,
                bassBoostStrength = prefs.bassBoost,
                virtualizerSupported = virtSupported,
                virtualizerStrength = prefs.virtualizer
            )
        }
    }

    fun setEnabled(enabled: Boolean) {
        runCatching { equalizer?.enabled = enabled }
        runCatching { bassBoost?.let { it.enabled = enabled && _state.value.bassBoostSupported } }
        runCatching { virtualizer?.let { it.enabled = enabled && _state.value.virtualizerSupported } }
        _state.value = _state.value.copy(enabled = enabled)
        persist()
    }

    fun setBandLevel(bandIndex: Short, level: Short) {
        runCatching { equalizer?.setBandLevel(bandIndex, level) }
        _state.value = _state.value.copy(
            bands = _state.value.bands.map { if (it.index == bandIndex) it.copy(level = level) else it },
            selectedPresetIndex = -1
        )
        persist()
    }

    fun applyPreset(presetIndex: Int) {
        val eq = equalizer ?: return
        runCatching {
            eq.usePreset(presetIndex.toShort())
            val bands = _state.value.bands.map { it.copy(level = eq.getBandLevel(it.index)) }
            _state.value = _state.value.copy(bands = bands, selectedPresetIndex = presetIndex)
            persist()
        }
    }

    fun setBassBoost(strength: Int) {
        runCatching { bassBoost?.setStrength(strength.toShort()) }
        _state.value = _state.value.copy(bassBoostStrength = strength)
        persist()
    }

    fun setVirtualizer(strength: Int) {
        runCatching { virtualizer?.setStrength(strength.toShort()) }
        _state.value = _state.value.copy(virtualizerStrength = strength)
        persist()
    }

    fun resetToFlat() {
        val flatBands = _state.value.bands.map { it.copy(level = 0) }
        flatBands.forEach { band ->
            runCatching { equalizer?.setBandLevel(band.index, band.level) }
        }
        runCatching { bassBoost?.setStrength(0) }
        runCatching { virtualizer?.setStrength(0) }
        _state.value = _state.value.copy(
            bands = flatBands,
            bassBoostStrength = 0,
            virtualizerStrength = 0,
            selectedPresetIndex = -1
        )
        persist()
    }

    private fun persist() {
        val s = _state.value
        scope.launch {
            userPreferencesRepository.setAudioEffectsPreferences(
                enabled = s.enabled,
                presetIndex = s.selectedPresetIndex,
                bandLevels = s.bands.map { it.level.toInt() },
                bassBoost = s.bassBoostStrength,
                virtualizer = s.virtualizerStrength
            )
        }
    }

    fun release() = releaseEffects()

    private fun releaseEffects() {
        runCatching { equalizer?.release() }
        runCatching { bassBoost?.release() }
        runCatching { virtualizer?.release() }
        equalizer = null
        bassBoost = null
        virtualizer = null
        attachedSessionId = C.AUDIO_SESSION_ID_UNSET
    }
}