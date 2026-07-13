// app/src/main/java/com/example/sonorid/ui/genre/GenreDetailViewModel.kt
package com.example.sonorid.ui.genre

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
class GenreDetailViewModel @Inject constructor(
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

    private var loadedGenre: String? = null

    fun load(genre: String) {
        if (loadedGenre == genre) return
        loadedGenre = genre
        viewModelScope.launch {
            _isLoading.value = true
            _songs.value = repository.getAllSongs()
                .filter { (it.genre ?: "Sin género") == genre }
            _isLoading.value = false
        }
    }

    fun toggleFavorite(songId: Long) {
        viewModelScope.launch { favoritesRepository.toggleFavorite(songId) }
    }
}