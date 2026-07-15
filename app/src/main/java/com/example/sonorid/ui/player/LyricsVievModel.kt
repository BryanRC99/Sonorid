// app/src/main/java/com/example/sonorid/ui/player/LyricsViewModel.kt
package com.example.sonorid.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sonorid.data.repository.LyricsRepositoryImpl
import com.example.sonorid.domain.model.Lyrics
import com.example.sonorid.domain.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LyricsViewModel @Inject constructor(
    private val repository: LyricsRepositoryImpl
) : ViewModel() {

    private val _lyrics = MutableStateFlow<Lyrics?>(null)
    val lyrics: StateFlow<Lyrics?> = _lyrics.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var loadedForSongId: Long? = null
    private var loadJob: Job? = null

    fun loadIfNeeded(song: Song) {
        if (loadedForSongId == song.id) return
        loadedForSongId = song.id
        loadJob?.cancel()
        _lyrics.value = null
        _isLoading.value = true

        loadJob = viewModelScope.launch {
            val result = repository.getLyrics(song)
            if (loadedForSongId == song.id) {
                _lyrics.value = result
                _isLoading.value = false
            }
        }
    }
}