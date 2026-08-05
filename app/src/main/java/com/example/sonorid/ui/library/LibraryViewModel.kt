// app/src/main/java/com/example/sonorid/ui/library/LibraryViewModel.kt
package com.example.sonorid.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sonorid.data.local.UserPreferencesRepository
import com.example.sonorid.data.repository.FavoritesRepository
import com.example.sonorid.domain.model.AlbumSortOption
import com.example.sonorid.domain.model.ArtistSortOption
import com.example.sonorid.domain.model.Song
import com.example.sonorid.domain.model.SongSortOption
import com.example.sonorid.domain.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: MusicRepository,
    private val favoritesRepository: FavoritesRepository,
    private val userPreferencesRepository: UserPreferencesRepository
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

    // 🆕 Cada tab (Canciones/Álbumes/Artistas) instancia su propio
    // LibraryViewModel, pero comparten el mismo UserPreferencesRepository:
    // cada pantalla lee solo el flujo que le corresponde.
    val songsSortOption: StateFlow<SongSortOption> = userPreferencesRepository.songsSortOption
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SongSortOption.TITLE_ASC)

    val albumsSortOption: StateFlow<AlbumSortOption> = userPreferencesRepository.albumsSortOption
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AlbumSortOption.TITLE_ASC)

    val artistsSortOption: StateFlow<ArtistSortOption> = userPreferencesRepository.artistsSortOption
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ArtistSortOption.NAME_ASC)

    fun setSongsSortOption(option: SongSortOption) {
        viewModelScope.launch { userPreferencesRepository.setSongsSortOption(option) }
    }

    fun setAlbumsSortOption(option: AlbumSortOption) {
        viewModelScope.launch { userPreferencesRepository.setAlbumsSortOption(option) }
    }

    fun setArtistsSortOption(option: ArtistSortOption) {
        viewModelScope.launch { userPreferencesRepository.setArtistsSortOption(option) }
    }

    init {
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
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleFavorite(songId: Long) {
        viewModelScope.launch {
            try {
                favoritesRepository.toggleFavorite(songId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addToFavorites(songIds: Set<Long>) {
        if (songIds.isEmpty()) return
        viewModelScope.launch {
            try {
                val currentFavorites = favoriteIds.value
                val newFavoritesToAdd = songIds.filterNot { it in currentFavorites }
                if (newFavoritesToAdd.isNotEmpty()) {
                    newFavoritesToAdd.forEach { id -> favoritesRepository.toggleFavorite(id) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun removeFromFavorites(songIds: Set<Long>) {
        if (songIds.isEmpty()) return
        viewModelScope.launch {
            try {
                val currentFavorites = favoriteIds.value
                val favoritesToRemove = songIds.filter { it in currentFavorites }
                favoritesToRemove.forEach { id -> favoritesRepository.toggleFavorite(id) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}