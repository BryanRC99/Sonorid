// app/src/main/java/com/example/sonorid/ui/artist/ArtistInfoViewModel.kt
package com.example.sonorid.ui.artist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sonorid.data.repository.ArtistInfo
import com.example.sonorid.data.repository.ArtistInfoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArtistInfoViewModel @Inject constructor(
    private val repository: ArtistInfoRepository
) : ViewModel() {

    private val _infoMap = MutableStateFlow<Map<String, ArtistInfo?>>(emptyMap())
    val infoMap: StateFlow<Map<String, ArtistInfo?>> = _infoMap.asStateFlow()

    private val requested = mutableSetOf<String>()

    fun request(artistName: String) {
        if (artistName in requested) return
        requested += artistName
        viewModelScope.launch {
            val info = repository.getArtistInfo(artistName)
            _infoMap.value = _infoMap.value + (artistName to info)
        }
    }
}