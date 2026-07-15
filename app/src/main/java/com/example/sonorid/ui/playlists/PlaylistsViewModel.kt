// app/src/main/java/com/example/sonorid/ui/playlists/PlaylistsViewModel.kt
package com.example.sonorid.ui.playlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sonorid.data.local.db.PlaylistEntity
import com.example.sonorid.data.repository.PlaylistPreview
import com.example.sonorid.data.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistsViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository
) : ViewModel() {

    val playlists: StateFlow<List<PlaylistEntity>> = playlistRepository.getPlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _previews = MutableStateFlow<Map<Long, PlaylistPreview>>(emptyMap())
    val previews: StateFlow<Map<Long, PlaylistPreview>> = _previews.asStateFlow()

    init {
        // Cada vez que cambia la lista de playlists (crear/borrar), se
        // recalcula la portada-collage y el conteo de canciones de cada una.
        viewModelScope.launch {
            playlists.collect { list ->
                list.forEach { playlist ->
                    if (playlist.id !in _previews.value) {
                        launch {
                            val preview = playlistRepository.getPreview(playlist.id)
                            _previews.value += (playlist.id to preview)
                        }
                    }
                }
            }
        }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch { playlistRepository.createPlaylist(name) }
    }

    /** Vuelve a calcular la portada de una playlist puntual (ej. tras agregar una canción). */
    fun refreshPreview(playlistId: Long) {
        viewModelScope.launch {
            val preview = playlistRepository.getPreview(playlistId)
            _previews.value += (playlistId to preview)
        }
    }
}