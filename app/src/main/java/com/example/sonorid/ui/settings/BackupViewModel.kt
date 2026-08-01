package com.example.sonorid.ui.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sonorid.data.backup.BackupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class BackupUiState {
    object Idle : BackupUiState()
    object Working : BackupUiState()
    data class ExportSuccess(val fileName: String) : BackupUiState()
    data class ImportSuccess(val summary: BackupRepository.ImportSummary) : BackupUiState()
    data class Error(val message: String) : BackupUiState()
}

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val backupRepository: BackupRepository
) : ViewModel() {

    private val _state = MutableStateFlow<BackupUiState>(BackupUiState.Idle)
    val state: StateFlow<BackupUiState> = _state.asStateFlow()

    fun exportTo(uri: Uri) {
        viewModelScope.launch {
            _state.value = BackupUiState.Working
            val result = backupRepository.exportBackup(uri)
            _state.value = result.fold(
                onSuccess = { BackupUiState.ExportSuccess(uri.lastPathSegment ?: "backup.json") },
                onFailure = { BackupUiState.Error(it.message ?: "No se pudo exportar la copia de seguridad") }
            )
        }
    }

    fun importFrom(uri: Uri) {
        viewModelScope.launch {
            _state.value = BackupUiState.Working
            val result = backupRepository.importBackup(uri)
            _state.value = result.fold(
                onSuccess = { BackupUiState.ImportSuccess(it) },
                onFailure = { BackupUiState.Error(it.message ?: "No se pudo restaurar la copia de seguridad") }
            )
        }
    }

    fun reset() {
        _state.value = BackupUiState.Idle
    }
}