// app/src/main/java/com/example/sonorid/ui/playlists/AddToPlaylistViewModel.kt
package com.example.sonorid.ui.playlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sonorid.data.local.db.PlaylistEntity
import com.example.sonorid.data.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddToPlaylistViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository
) : ViewModel() {

    val playlists: StateFlow<List<PlaylistEntity>> = playlistRepository.getPlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _membership = MutableStateFlow<Set<Long>>(emptySet())
    val membership: StateFlow<Set<Long>> = _membership.asStateFlow()

    fun loadMembership(songId: Long) {
        viewModelScope.launch {
            _membership.value = playlistRepository.playlistIdsContaining(songId).toSet()
        }
    }

    fun toggle(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            val current = _membership.value
            if (playlistId in current) {
                playlistRepository.removeSong(playlistId, songId)
                _membership.value = current - playlistId
            } else {
                playlistRepository.addSong(playlistId, songId)
                _membership.value = current + playlistId
            }
        }
    }

    fun createPlaylistAndAdd(name: String, songId: Long) {
        viewModelScope.launch {
            val id = playlistRepository.createPlaylist(name)
            playlistRepository.addSong(id, songId)
            _membership.value = _membership.value + id
        }
    }
}