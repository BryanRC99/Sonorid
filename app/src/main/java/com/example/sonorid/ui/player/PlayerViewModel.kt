// app/src/main/java/com/example/sonorid/ui/player/PlayerViewModel.kt
package com.example.sonorid.ui.player

import android.content.IntentSender
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sonorid.data.local.MediaStoreDataSource
import com.example.sonorid.data.repository.FavoritesRepository
import com.example.sonorid.domain.model.MediaActionResult
import com.example.sonorid.domain.model.Song
import com.example.sonorid.domain.repository.MusicRepository
import com.example.sonorid.playback.MusicController
import com.example.sonorid.playback.PlaybackMetaState
import com.example.sonorid.playback.PlaybackProgress
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Acción de MediaStore pendiente de que el usuario apruebe un diálogo de consentimiento. */
private sealed class PendingMediaAction {
    data class Delete(val song: Song) : PendingMediaAction()
    data class Edit(val song: Song, val title: String, val artist: String, val album: String) : PendingMediaAction()
}

sealed class SongActionEvent {
    object Deleted : SongActionEvent()
    object Updated : SongActionEvent()
    data class Error(val message: String) : SongActionEvent()
}

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val musicController: MusicController,
    private val favoritesRepository: FavoritesRepository,
    private val musicRepository: MusicRepository,
    private val mediaStoreDataSource: MediaStoreDataSource
) : ViewModel() {

    val metaState: StateFlow<PlaybackMetaState> = musicController.playbackState
    val progress: StateFlow<PlaybackProgress> = musicController.progress

    val favoriteIds: StateFlow<Set<Long>> = favoritesRepository.getFavoritesFlow()
        .map { list -> list.map { it.songId }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    private val _pendingPermission = MutableStateFlow<IntentSender?>(null)
    val pendingPermission: StateFlow<IntentSender?> = _pendingPermission.asStateFlow()

    private val _events = MutableSharedFlow<SongActionEvent>()
    val events: SharedFlow<SongActionEvent> = _events.asSharedFlow()

    private var pendingAction: PendingMediaAction? = null

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
    fun seekToQueueItem(index: Int) = musicController.seekToQueueItem(index)
    fun addToQueue(song: Song) = musicController.addToQueue(song)

    fun toggleFavorite(songId: Long) {
        viewModelScope.launch { favoritesRepository.toggleFavorite(songId) }
    }

    fun updateSongInfo(song: Song, title: String, artist: String, album: String) {
        viewModelScope.launch {
            val result = mediaStoreDataSource.updateSongMetadata(song.uri, title, artist, album)
            when (result) {
                is MediaActionResult.Success -> {
                    musicRepository.invalidateCache()
                    _events.emit(SongActionEvent.Updated)
                }
                is MediaActionResult.RequiresPermission -> {
                    pendingAction = PendingMediaAction.Edit(song, title, artist, album)
                    _pendingPermission.value = result.intentSender
                }
                is MediaActionResult.Error -> _events.emit(SongActionEvent.Error(result.message))
            }
        }
    }

    fun deleteSong(song: Song) {
        viewModelScope.launch {
            val result = mediaStoreDataSource.deleteSong(song.uri)
            when (result) {
                is MediaActionResult.Success -> {
                    musicRepository.invalidateCache()
                    musicController.removeFromQueue(song.id)
                    _events.emit(SongActionEvent.Deleted)
                }
                is MediaActionResult.RequiresPermission -> {
                    pendingAction = PendingMediaAction.Delete(song)
                    _pendingPermission.value = result.intentSender
                }
                is MediaActionResult.Error -> _events.emit(SongActionEvent.Error(result.message))
            }
        }
    }

    /** Se llama cuando el diálogo de consentimiento del sistema devuelve RESULT_OK. */
    fun onPermissionGranted() {
        val action = pendingAction ?: return
        pendingAction = null
        _pendingPermission.value = null
        viewModelScope.launch {
            when (action) {
                is PendingMediaAction.Delete -> {
                    mediaStoreDataSource.forceDelete(action.song.uri)
                    musicRepository.invalidateCache()
                    musicController.removeFromQueue(action.song.id)
                    _events.emit(SongActionEvent.Deleted)
                }
                is PendingMediaAction.Edit -> {
                    mediaStoreDataSource.forceUpdate(action.song.uri, action.title, action.artist, action.album)
                    musicRepository.invalidateCache()
                    _events.emit(SongActionEvent.Updated)
                }
            }
        }
    }

    fun onPermissionDenied() {
        pendingAction = null
        _pendingPermission.value = null
    }

    override fun onCleared() {
        musicController.release()
    }
}
