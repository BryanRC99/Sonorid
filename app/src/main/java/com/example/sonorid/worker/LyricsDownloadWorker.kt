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
import com.example.sonorid.data.repository.LyricsRepositoryImpl
import com.example.sonorid.domain.repository.MusicRepository
import com.example.sonorid.util.NotificationChannels
import com.example.sonorid.util.NetworkStatus
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Descarga letras para toda la biblioteca en segundo plano usando WorkManager.
 * Sigue corriendo aunque el usuario navegue a otra pantalla, y muestra UNA
 * notificación al terminar (no usa servicio en primer plano: eso requiere
 * declarar tipos de servicio y maneja restricciones de fondo muy sensibles
 * en Android 13/14+, y un mal uso ahí puede tumbar el proceso en vez de
 * simplemente fallar el Worker. Para una tarea de red de duración moderada
 * como esta, un Worker normal es suficiente y mucho más robusto).
 */
@HiltWorker
class LyricsDownloadWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val musicRepository: MusicRepository,
    private val lyricsRepository: LyricsRepositoryImpl
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val songs = musicRepository.getAllSongs()
            val total = songs.size
            var found = 0
            var notFound = 0

            setProgress(workDataOf(KEY_INDEX to 0, KEY_TOTAL to total, KEY_TITLE to ""))

            for ((index, song) in songs.withIndex()) {
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
                        KEY_TITLE to song.title,
                        KEY_FOUND to found,
                        KEY_NOT_FOUND to notFound
                    )
                )

                val lyrics = lyricsRepository.getLyrics(song) // ya cachea: saltea lo ya descargado
                if (lyrics != null) found++ else notFound++
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

        val notification = NotificationCompat.Builder(applicationContext, NotificationChannels.LYRICS_DOWNLOAD_CHANNEL_ID)
            .setContentTitle("Descarga de letras completada")
            .setContentText("$found encontradas de $total canciones revisadas")
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setAutoCancel(true)
            .setContentIntent(openAppIntent)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(NOTIFICATION_DONE_ID, notification)
    }

    companion object {
        const val WORK_NAME = "bulk_lyrics_download"
        const val KEY_INDEX = "index"
        const val KEY_TOTAL = "total"
        const val KEY_TITLE = "title"
        const val KEY_FOUND = "found"
        const val KEY_NOT_FOUND = "not_found"
        const val KEY_NO_INTERNET = "no_internet"
        const val KEY_ERROR = "error"
        private const val NOTIFICATION_DONE_ID = 4202
    }
}