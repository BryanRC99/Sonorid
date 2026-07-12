// app/src/main/java/com/example/sonorid/ui/player/PlayerViewModel.kt
package com.example.sonorid.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sonorid.domain.model.Song
import com.example.sonorid.playback.MusicController
import com.example.sonorid.playback.PlaybackUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val musicController: MusicController
) : ViewModel() {

    val uiState: StateFlow<PlaybackUiState> = musicController.uiState

    init {
        musicController.connect {
            viewModelScope.launch {
                while (true) {
                    musicController.pollPosition()
                    delay(500)
                }
            }
        }
    }

    fun play(songs: List<Song>, startIndex: Int) = musicController.playQueue(songs, startIndex)
    fun togglePlayPause() = musicController.togglePlayPause()
    fun skipNext() = musicController.skipNext()
    fun skipPrevious() = musicController.skipPrevious()
    fun seekTo(ms: Long) = musicController.seekTo(ms)
    fun toggleShuffle() = musicController.toggleShuffle()
    fun cycleRepeat() = musicController.cycleRepeatMode()

    override fun onCleared() {
        musicController.release()
    }
}