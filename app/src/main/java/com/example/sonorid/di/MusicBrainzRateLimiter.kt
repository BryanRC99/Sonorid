// app/src/main/java/com/example/sonorid/di/MusicBrainzRateLimiter.kt
package com.example.sonorid.di

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * MusicBrainz exige máximo 1 request/segundo. A diferencia del interceptor
 * con Thread.sleep, este limitador usa Mutex + delay: la corrutina que
 * espera turno SUSPENDE en vez de bloquear un hilo del pool de OkHttp,
 * mucho más barato cuando el usuario scrollea rápido por varios artistas.
 */
@Singleton
class MusicBrainzRateLimiter @Inject constructor() {
    private val mutex = Mutex()
    private var lastRequestAtMs = 0L
    private val minIntervalMs = 1100L

    suspend fun <T> throttled(block: suspend () -> T): T = mutex.withLock {
        val elapsed = System.currentTimeMillis() - lastRequestAtMs
        if (elapsed < minIntervalMs) {
            delay(minIntervalMs - elapsed)
        }
        val result = block()
        lastRequestAtMs = System.currentTimeMillis()
        result
    }
}