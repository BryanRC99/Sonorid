// app/src/main/java/com/example/sonorid/ui/playlists/PlaylistDetailViewModel.kt
package com.example.sonorid.ui.playlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sonorid.data.local.UserPreferencesRepository
import com.example.sonorid.data.repository.FavoritesRepository
import com.example.sonorid.data.repository.PlaylistRepository
import com.example.sonorid.domain.model.PlaylistSongSortOption
import com.example.sonorid.domain.model.Song
import com.example.sonorid.domain.model.sortedByOption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    private val favoritesRepository: FavoritesRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    // Orden "real" (posición en la playlist, u orden de carga en Favoritos):
    // es la fuente de verdad para agregar/quitar canciones.
    private val _rawSongs = MutableStateFlow<List<Song>>(emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _playlistName = MutableStateFlow<String?>(null)
    val playlistName: StateFlow<String?> = _playlistName.asStateFlow()

    val favoriteIds: StateFlow<Set<Long>> = favoritesRepository.getFavoritesFlow()
        .map { list -> list.map { it.songId }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    // 🆕 Orden elegido por el usuario, persistido.
    val sortOption: StateFlow<PlaylistSongSortOption> = userPreferencesRepository.playlistSortOption
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlaylistSongSortOption.CUSTOM)

    // 🆕 Lo que realmente ve la UI: _rawSongs pasado por el criterio de orden.
    val songs: StateFlow<List<Song>> = combine(_rawSongs, sortOption) { list, option ->
        list.sortedByOption(option)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSortOption(option: PlaylistSongSortOption) {
        viewModelScope.launch { userPreferencesRepository.setPlaylistSortOption(option) }
    }

    private var loadedPlaylistId: Long? = null
    private var loadedIsFavorites = false

    fun loadPlaylist(playlistId: Long) {
        if (loadedPlaylistId == playlistId && !loadedIsFavorites) return
        loadedPlaylistId = playlistId
        loadedIsFavorites = false
        viewModelScope.launch {
            _isLoading.value = true
            _playlistName.value = playlistRepository.getPlaylist(playlistId)?.name
            _rawSongs.value = playlistRepository.getSongsForPlaylist(playlistId)
            _isLoading.value = false
        }
    }

    fun loadFavorites() {
        if (loadedIsFavorites) return
        loadedIsFavorites = true
        loadedPlaylistId = null
        _playlistName.value = null
        viewModelScope.launch {
            _isLoading.value = true
            _rawSongs.value = favoritesRepository.getFavoriteSongs()
            _isLoading.value = false
        }
    }

    fun removeSongsFromPlaylist(playlistId: Long, songIds: Set<Long>) {
        viewModelScope.launch {
            songIds.forEach { playlistRepository.removeSong(playlistId, it) }
            _rawSongs.value = _rawSongs.value.filterNot { it.id in songIds }
        }
    }

    fun removeSongsFromFavorites(songIds: Set<Long>) {
        viewModelScope.launch {
            favoritesRepository.removeFavorites(songIds)
            _rawSongs.value = _rawSongs.value.filterNot { it.id in songIds }
        }
    }

    fun toggleFavorite(songId: Long) {
        viewModelScope.launch {
            val wasFavorite = songId in favoriteIds.value
            favoritesRepository.toggleFavorite(songId)
            if (loadedIsFavorites && wasFavorite) {
                _rawSongs.value = _rawSongs.value.filterNot { it.id == songId }
            }
        }
    }

    fun removeFromPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            playlistRepository.removeSong(playlistId, songId)
            _rawSongs.value = _rawSongs.value.filterNot { it.id == songId }
        }
    }

    fun deletePlaylist(playlistId: Long, onDeleted: () -> Unit) {
        viewModelScope.launch {
            playlistRepository.deletePlaylist(playlistId)
            onDeleted()
        }
    }
}