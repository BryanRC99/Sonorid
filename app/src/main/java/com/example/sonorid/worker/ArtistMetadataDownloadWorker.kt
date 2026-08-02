// app/src/main/java/com/example/sonorid/worker/ArtistMetadataDownloadWorker.kt
package com.example.sonorid.worker

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.sonorid.MainActivity
import com.example.sonorid.data.repository.ArtistInfoRepository
import com.example.sonorid.data.repository.RateLimitException
import com.example.sonorid.domain.repository.MusicRepository
import com.example.sonorid.util.NotificationChannels
import com.example.sonorid.util.NetworkStatus
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Descarga metadatos de artista (imagen, biografía, banner, género, país,
 * año de formación) para toda la biblioteca, en segundo plano. Sigue el
 * mismo patrón que LyricsDownloadWorker: cachea en Room para no repetir
 * peticiones, y se detiene con un motivo claro si se pierde internet o
 * si TheAudioDB responde 429 (límite de peticiones alcanzado).
 */
@HiltWorker
class ArtistMetadataDownloadWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val musicRepository: MusicRepository,
    private val artistInfoRepository: ArtistInfoRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val artists = musicRepository.getAllSongs()
                .map { it.artist }
                .distinct()
                .filter { it.isNotBlank() }
            val total = artists.size
            var found = 0
            var notFound = 0

            setProgress(workDataOf(KEY_INDEX to 0, KEY_TOTAL to total, KEY_TITLE to ""))

            for ((index, artistName) in artists.withIndex()) {
                if (!NetworkStatus.isConnected(applicationContext)) {
                    return Result.failure(
                        workDataOf(
                            KEY_NO_INTERNET to true,
                            KEY_INDEX to index,
                            KEY_TOTAL to total,
                            KEY_FOUND to found,
                            KEY_NOT_FOUND to notFound
                        )
                    )
                }

                setProgress(
                    workDataOf(
                        KEY_INDEX to index,
                        KEY_TOTAL to total,
                        KEY_TITLE to artistName,
                        KEY_FOUND to found,
                        KEY_NOT_FOUND to notFound
                    )
                )

                try {
                    val info = artistInfoRepository.getArtistInfo(artistName)
                    if (info != null) found++ else notFound++
                } catch (e: RateLimitException) {
                    return Result.failure(
                        workDataOf(
                            KEY_RATE_LIMITED to true,
                            KEY_INDEX to index,
                            KEY_TOTAL to total,
                            KEY_FOUND to found,
                            KEY_NOT_FOUND to notFound
                        )
                    )
                }
            }

            showCompletionNotification(found, notFound, total)

            Result.success(
                workDataOf(
                    KEY_INDEX to total,
                    KEY_TOTAL to total,
                    KEY_FOUND to found,
                    KEY_NOT_FOUND to notFound
                )
            )
        } catch (e: Exception) {
            Result.failure(workDataOf(KEY_ERROR to (e.message ?: "Error desconocido")))
        }
    }

    private fun showCompletionNotification(found: Int, notFound: Int, total: Int) {
        val hasPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        if (!hasPermission) return

        val openAppIntent = PendingIntent.getActivity(
            applicationContext, 0,
            Intent(applicationContext, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, NotificationChannels.METADATA_DOWNLOAD_CHANNEL_ID)
            .setContentTitle("Descarga de metadatos completada")
            .setContentText("$found artistas encontrados de $total revisados")
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setAutoCancel(true)
            .setContentIntent(openAppIntent)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(NOTIFICATION_DONE_ID, notification)
    }

    companion object {
        const val WORK_NAME = "bulk_metadata_download"
        const val KEY_INDEX = "index"
        const val KEY_TOTAL = "total"
        const val KEY_TITLE = "title"
        const val KEY_FOUND = "found"
        const val KEY_NOT_FOUND = "not_found"
        const val KEY_NO_INTERNET = "no_internet"
        const val KEY_RATE_LIMITED = "rate_limited"
        const val KEY_ERROR = "error"
        private const val NOTIFICATION_DONE_ID = 4203
    }
}