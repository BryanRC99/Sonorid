// app/src/main/java/com/example/sonorid/ui/album/AlbumDetailViewModel.kt
package com.example.sonorid.ui.album

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sonorid.data.repository.FavoritesRepository
import com.example.sonorid.domain.model.Song
import com.example.sonorid.domain.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlbumDetailViewModel @Inject constructor(
    private val repository: MusicRepository,
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val favoriteIds: StateFlow<Set<Long>> = favoritesRepository.getFavoritesFlow()
        .map { list -> list.map { it.songId }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    private var loadedAlbumId: Long? = null

    fun load(albumId: Long) {
        if (loadedAlbumId == albumId) return
        loadedAlbumId = albumId
        viewModelScope.launch {
            _isLoading.value = true
            _songs.value = repository.getAllSongs()
                .filter { it.albumId == albumId }
                .sortedBy { it.trackNumber }
            _isLoading.value = false
        }
    }

    fun toggleFavorite(songId: Long) {
        viewModelScope.launch { favoritesRepository.toggleFavorite(songId) }
    }
}