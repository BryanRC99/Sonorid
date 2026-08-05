package com.example.sonorid.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sonorid.audiofx.AudioEffectsController
import com.example.sonorid.domain.model.AudioEffectsUiState
import com.example.sonorid.playback.MusicController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AudioEffectsViewModel @Inject constructor(
    private val controller: AudioEffectsController,
    musicController: MusicController
) : ViewModel() {

    val uiState: StateFlow<AudioEffectsUiState> = controller.state
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AudioEffectsUiState())

    val hasSongPlaying: StateFlow<Boolean> = musicController.playbackState
        .map { it.currentSong != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        // Cada vez que hay una canción activa, intenta engancharse al
        // audioSessionId más reciente (puede cambiar entre canciones/sesiones).
        musicController.playbackState
            .onEach { if (it.currentSong != null) controller.ensureAttached() }
            .launchIn(viewModelScope)
    }

    fun retryAttach() = controller.ensureAttached()
    fun setEnabled(enabled: Boolean) = controller.setEnabled(enabled)
    fun setBandLevel(bandIndex: Short, level: Short) = controller.setBandLevel(bandIndex, level)
    fun applyPreset(index: Int) = controller.applyPreset(index)
    fun setBassBoost(strength: Int) = controller.setBassBoost(strength)
    fun setVirtualizer(strength: Int) = controller.setVirtualizer(strength)
    fun resetToFlat() = controller.resetToFlat()
}