// app/src/main/java/com/example/sonorid/widget/WidgetUpdater.kt
package com.example.sonorid.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.RemoteViews
import androidx.core.graphics.drawable.toBitmap
import coil.imageLoader
import coil.request.ImageRequest
import com.example.sonorid.MainActivity
import com.example.sonorid.R
import com.example.sonorid.domain.model.Song
import com.example.sonorid.playback.PlaybackService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Construye el RemoteViews del widget y lo publica en el sistema. Vive como
 * singleton porque MusicController lo invoca en cada cambio de estado
 * (canción, play/pause) desde cualquier parte de la app.
 */
@Singleton
class WidgetUpdater @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // No hay lifecycle propio (no es una Activity/ViewModel), así que
    // mantenemos un scope manual solo para la carga async de la portada.
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    // Evita que una carga de portada vieja "pise" a una más reciente si el
    // usuario cambia de canción rápido (ej. salta varias veces seguidas).
    private var lastArtRequestSongId: Long? = null

    fun update(song: Song?, isPlaying: Boolean) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val ids = appWidgetManager.getAppWidgetIds(ComponentName(context, SonoridWidgetProvider::class.java))
        if (ids.isEmpty()) return // no hay ningún widget añadido: no perdemos tiempo

        if (song == null) {
            val views = buildViews(title = "Sonorid", artist = "Nada sonando", isPlaying = false, art = null)
            ids.forEach { id -> appWidgetManager.updateAppWidget(id, views) }
            lastArtRequestSongId = null
            return
        }

        // Texto + botones se actualizan de inmediato, sin esperar la portada.
        val views = buildViews(title = song.title, artist = song.artist, isPlaying = isPlaying, art = null)
        ids.forEach { id -> appWidgetManager.updateAppWidget(id, views) }

        lastArtRequestSongId = song.id
        scope.launch {
            val bitmap = loadArt(song.albumArtUri)
            if (lastArtRequestSongId != song.id) return@launch // llegó tarde, ya cambió la canción
            val viewsWithArt = buildViews(title = song.title, artist = song.artist, isPlaying = isPlaying, art = bitmap)
            ids.forEach { id -> appWidgetManager.updateAppWidget(id, viewsWithArt) }
        }
    }

    private suspend fun loadArt(uri: Uri): Bitmap? {
        return try {
            val request = ImageRequest.Builder(context)
                .data(uri)
                .allowHardware(false) // necesitamos un Bitmap software-backed para RemoteViews
                .build()
            context.imageLoader.execute(request).drawable?.toBitmap()
        } catch (e: Exception) {
            null
        }
    }

    private fun buildViews(title: String, artist: String, isPlaying: Boolean, art: Bitmap?): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_sonorid)
        views.setTextViewText(R.id.widget_title, title)
        views.setTextViewText(R.id.widget_artist, artist)

        if (art != null) {
            views.setImageViewBitmap(R.id.widget_art, art)
        } else {
            views.setImageViewResource(R.id.widget_art, R.drawable.widget_ic_album_placeholder)
        }

        views.setImageViewResource(
            R.id.widget_play_pause,
            if (isPlaying) R.drawable.widget_ic_pause else R.drawable.widget_ic_play
        )

        views.setOnClickPendingIntent(R.id.widget_play_pause, actionPendingIntent(SonoridWidgetProvider.ACTION_PLAY_PAUSE))
        views.setOnClickPendingIntent(R.id.widget_previous, actionPendingIntent(SonoridWidgetProvider.ACTION_PREVIOUS))
        views.setOnClickPendingIntent(R.id.widget_next, actionPendingIntent(SonoridWidgetProvider.ACTION_NEXT))
        views.setOnClickPendingIntent(R.id.widget_root, openAppPendingIntent())

        return views
    }

    private fun actionPendingIntent(action: String): PendingIntent {
        val intent = Intent(context, PlaybackService::class.java).apply { this.action = action }
        return PendingIntent.getService(
            context, action.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun openAppPendingIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}