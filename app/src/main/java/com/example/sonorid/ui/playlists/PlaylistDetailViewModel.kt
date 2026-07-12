// app/src/main/java/com/example/sonorid/ui/playlists/PlaylistDetailViewModel.kt
package com.example.sonorid.ui.playlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sonorid.data.repository.FavoritesRepository
import com.example.sonorid.data.repository.PlaylistRepository
import com.example.sonorid.domain.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val favoriteIds: StateFlow<Set<Long>> = favoritesRepository.getFavoritesFlow()
        .map { list -> list.map { it.songId }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    private var loadedPlaylistId: Long? = null
    private var loadedIsFavorites = false

    fun loadPlaylist(playlistId: Long) {
        if (loadedPlaylistId == playlistId && !loadedIsFavorites) return
        loadedPlaylistId = playlistId
        loadedIsFavorites = false
        viewModelScope.launch {
            _isLoading.value = true
            _songs.value = playlistRepository.getSongsForPlaylist(playlistId)
            _isLoading.value = false
        }
    }

    fun loadFavorites() {
        if (loadedIsFavorites) return
        loadedIsFavorites = true
        loadedPlaylistId = null
        viewModelScope.launch {
            _isLoading.value = true
            _songs.value = favoritesRepository.getFavoriteSongs()
            _isLoading.value = false
        }
    }

    fun toggleFavorite(songId: Long) {
        viewModelScope.launch { favoritesRepository.toggleFavorite(songId) }
    }

    fun removeFromPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            playlistRepository.removeSong(playlistId, songId)
            _songs.value = _songs.value.filterNot { it.id == songId }
        }
    }
}