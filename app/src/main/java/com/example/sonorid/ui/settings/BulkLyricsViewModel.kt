package com.example.sonorid.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.sonorid.util.NetworkStatus
import com.example.sonorid.worker.LyricsDownloadTracker
import com.example.sonorid.worker.LyricsDownloadWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class BulkLyricsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val tracker: LyricsDownloadTracker
) : ViewModel() {

    private val workManager = WorkManager.getInstance(context)

    private val _isConnected = MutableStateFlow(NetworkStatus.isConnected(context))
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    // 🛠️ FIX: en vez de observar por "nombre único" (que puede devolver varias
    // entradas históricas sin orden garantizado, causando que "Reintentar" no
    // pareciera hacer nada), rastreamos el ID exacto del último trabajo encolado.
    val workInfo: StateFlow<WorkInfo?> = tracker.activeRequestId
        .flatMapLatest { id ->
            if (id == null) flowOf(null) else workManager.getWorkInfoByIdFlow(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun refreshConnectivity() {
        _isConnected.value = NetworkStatus.isConnected(context)
    }

    fun start() {
        if (!NetworkStatus.isConnected(context)) {
            _isConnected.value = false
            return
        }
        _isConnected.value = true

        val request = OneTimeWorkRequestBuilder<LyricsDownloadWorker>().build()
        // REPLACE: cualquier trabajo anterior con este nombre (terminado o no)
        // se reemplaza por uno nuevo, evitando ambigüedad al reintentar.
        workManager.enqueueUniqueWork(LyricsDownloadWorker.WORK_NAME, ExistingWorkPolicy.REPLACE, request)
        tracker.track(request.id)
    }

    fun cancel() {
        workManager.cancelUniqueWork(LyricsDownloadWorker.WORK_NAME)
    }
}