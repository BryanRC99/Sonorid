// app/src/main/java/com/example/sonorid/playback/MusicController.kt
package com.example.sonorid.playback

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.sonorid.domain.model.Song
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/** Metadata de reproducción: cambia poco (solo al cambiar de canción o alternar shuffle/repeat). */
data class PlaybackMetaState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val queue: List<Song> = emptyList(),
    val shuffleEnabled: Boolean = false,
    val repeatMode: Int = Player.REPEAT_MODE_OFF
)

/** Progreso de reproducción: cambia ~2 veces por segundo. Aislado para no arrastrar recomposiciones. */
data class PlaybackProgress(
    val positionMs: Long = 0L,
    val durationMs: Long = 0L
)

@Singleton
class MusicController @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var controller: MediaController? = null
    private var currentQueue: List<Song> = emptyList()
    private var songsByMediaId: Map<String, Song> = emptyMap()

    private val _playbackState = MutableStateFlow(PlaybackMetaState())
    val playbackState: StateFlow<PlaybackMetaState> = _playbackState.asStateFlow()

    private val _progress = MutableStateFlow(PlaybackProgress())
    val progress: StateFlow<PlaybackProgress> = _progress.asStateFlow()

    fun connect(onReady: () -> Unit = {}) {
        if (controller != null) { onReady(); return }
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val future = MediaController.Builder(context, sessionToken).buildAsync()
        future.addListener({
            controller = future.get()
            attachListener()
            onReady()
        }, MoreExecutors.directExecutor())
    }

    private fun attachListener() {
        controller?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _playbackState.value = _playbackState.value.copy(isPlaying = isPlaying)
            }
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val song = mediaItem?.mediaId?.let(songsByMediaId::get)
                _playbackState.value = _playbackState.value.copy(currentSong = song)
                updateProgress()
            }
            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                _playbackState.value = _playbackState.value.copy(shuffleEnabled = shuffleModeEnabled)
            }
            override fun onRepeatModeChanged(repeatMode: Int) {
                _playbackState.value = _playbackState.value.copy(repeatMode = repeatMode)
            }
            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) = updateProgress()
        })
    }

    /** Llamar periódicamente (ej. cada 500ms) desde el ViewModel para actualizar el progreso */
    fun pollPosition() {
        updateProgress()
    }

    private fun updateProgress() {
        val c = controller ?: return
        val next = PlaybackProgress(
            positionMs = c.currentPosition.coerceAtLeast(0),
            durationMs = c.duration.coerceAtLeast(0)
        )
        if (_progress.value != next) _progress.value = next
    }

    fun playQueue(songs: List<Song>, startIndex: Int) {
        val c = controller ?: return
        currentQueue = songs
        songsByMediaId = songs.associateBy { it.id.toString() }
        val items = songs.map { song ->
            MediaItem.Builder()
                .setMediaId(song.id.toString())
                .setUri(song.uri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(song.title)
                        .setArtist(song.artist)
                        .setAlbumTitle(song.album)
                        .setArtworkUri(song.albumArtUri)
                        .build()
                )
                .build()
        }
        c.setMediaItems(items, startIndex, 0L)
        c.prepare()
        c.play()
        _playbackState.value = _playbackState.value.copy(queue = songs, currentSong = songs[startIndex])
    }

    fun togglePlayPause() {
        val c = controller ?: return
        if (c.isPlaying) c.pause() else c.play()
    }

    fun skipNext() = controller?.seekToNext()
    fun skipPrevious() = controller?.seekToPrevious()
    fun seekTo(positionMs: Long) { controller?.seekTo(positionMs) }
    fun toggleShuffle() { controller?.let { it.shuffleModeEnabled = !it.shuffleModeEnabled } }
    fun cycleRepeatMode() {
        val c = controller ?: return
        c.repeatMode = when (c.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
    }

    fun release() {
        controller?.release()
        controller = null
        currentQueue = emptyList()
        songsByMediaId = emptyMap()
    }
}
