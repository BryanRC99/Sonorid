package com.example.sonorid.worker

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LyricsDownloadTracker @Inject constructor() {
    private val _activeRequestId = MutableStateFlow<UUID?>(null)
    val activeRequestId: StateFlow<UUID?> = _activeRequestId.asStateFlow()

    fun track(id: UUID) {
        _activeRequestId.value = id
    }
}