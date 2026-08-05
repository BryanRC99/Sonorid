package com.example.sonorid.playback

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerLaunchController @Inject constructor() {
    private val _expandRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val expandRequests: SharedFlow<Unit> = _expandRequests.asSharedFlow()

    fun requestExpand() {
        _expandRequests.tryEmit(Unit)
    }
}