// app/src/main/java/com/example/sonorid/ui/library/LibraryViewModel.kt
package com.example.sonorid.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sonorid.data.repository.FavoritesRepository
import com.example.sonorid.domain.model.Song
import com.example.sonorid.domain.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: MusicRepository,
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var hasLoaded = false
    private var loadJob: Job? = null

    val favoriteIds: StateFlow<Set<Long>> = favoritesRepository.getFavoritesFlow()
        .map { list -> list.map { it.songId }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    init {
        // Si se guardan nuevas carpetas desde otra pantalla, refresca solo entonces.
        viewModelScope.launch {
            repository.selectedFolders
                .drop(1)
                .collect { loadSongs(forceRefresh = true) }
        }
    }

    fun loadSongs(forceRefresh: Boolean = false) {
        if (!forceRefresh && (hasLoaded || loadJob?.isActive == true)) return

        loadJob = viewModelScope.launch {
            _isLoading.value = true
            try {
                _songs.value = repository.getAllSongs()
                hasLoaded = true
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleFavorite(songId: Long) {
        viewModelScope.launch { favoritesRepository.toggleFavorite(songId) }
    }
}
