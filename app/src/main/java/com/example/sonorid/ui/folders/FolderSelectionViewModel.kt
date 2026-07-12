// ui/folders/FolderSelectionViewModel.kt
package com.example.sonorid.ui.folders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sonorid.domain.model.MusicFolder
import com.example.sonorid.domain.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FolderSelectionViewModel @Inject constructor(
    private val repository: MusicRepository
) : ViewModel() {

    private val _folders = MutableStateFlow<List<MusicFolder>>(emptyList())
    val folders: StateFlow<List<MusicFolder>> = _folders.asStateFlow()

    private val _selected = MutableStateFlow<Set<String>>(emptySet())
    val selected: StateFlow<Set<String>> = _selected.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _isLoading.value = true
            _folders.value = repository.getAllFolders()
            _selected.value = repository.selectedFolders.first()
            _isLoading.value = false
        }
    }

    fun toggle(path: String) {
        _selected.value = _selected.value.toMutableSet().apply {
            if (contains(path)) remove(path) else add(path)
        }
    }

    fun selectAll() {
        _selected.value = _folders.value.map { it.path }.toSet()
    }

    fun clearSelection() {
        _selected.value = emptySet()
    }

    fun save(onSaved: () -> Unit) {
        viewModelScope.launch {
            repository.setSelectedFolders(_selected.value)
            onSaved()
        }
    }
}