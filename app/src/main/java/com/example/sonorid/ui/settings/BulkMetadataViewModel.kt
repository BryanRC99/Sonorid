// app/src/main/java/com/example/sonorid/ui/settings/BulkMetadataViewModel.kt
package com.example.sonorid.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.sonorid.util.NetworkStatus
import com.example.sonorid.worker.ArtistMetadataDownloadTracker
import com.example.sonorid.worker.ArtistMetadataDownloadWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class BulkMetadataViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val tracker: ArtistMetadataDownloadTracker
) : ViewModel() {

    private val workManager = WorkManager.getInstance(context)

    private val _isConnected = MutableStateFlow(NetworkStatus.isConnected(context))
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

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

        val request = OneTimeWorkRequestBuilder<ArtistMetadataDownloadWorker>().build()
        workManager.enqueueUniqueWork(
            ArtistMetadataDownloadWorker.WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
        tracker.track(request.id)
    }

    fun cancel() {
        workManager.cancelUniqueWork(ArtistMetadataDownloadWorker.WORK_NAME)
    }
}