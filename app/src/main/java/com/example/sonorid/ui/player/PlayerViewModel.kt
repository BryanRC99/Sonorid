// app/src/main/java/com/example/sonorid/ui/player/PlayerViewModel.kt
package com.example.sonorid.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sonorid.domain.model.Song
import com.example.sonorid.playback.MusicController
import com.example.sonorid.playback.PlaybackMetaState
import com.example.sonorid.playback.PlaybackProgress
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val musicController: MusicController
) : ViewModel() {

    val metaState: StateFlow<PlaybackMetaState> = musicController.playbackState
    val progress: StateFlow<PlaybackProgress> = musicController.progress

    init {
        musicController.connect {
            viewModelScope.launch {
                while (isActive) {
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
