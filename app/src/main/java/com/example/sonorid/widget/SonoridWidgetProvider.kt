// app/src/main/java/com/example/sonorid/widget/SonoridWidgetProvider.kt
package com.example.sonorid.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import com.example.sonorid.playback.MusicController
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Se activa cuando el widget se agrega a la pantalla de inicio o el sistema
 * pide refrescarlo. Al ser @AndroidEntryPoint, Hilt inyecta las dependencias
 * automáticamente antes de que se ejecute onUpdate.
 */
@AndroidEntryPoint
class SonoridWidgetProvider : AppWidgetProvider() {

    @Inject lateinit var widgetUpdater: WidgetUpdater
    @Inject lateinit var musicController: MusicController

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // Pinta con el último estado conocido. Si la app nunca se conectó al
        // servicio de reproducción en este proceso (ej. widget recién
        // agregado sin haber abierto la app antes), currentSong es null y
        // se muestra el placeholder "Nada sonando" — se actualizará solo en
        // cuanto el usuario reproduzca algo.
        val state = musicController.playbackState.value
        widgetUpdater.update(state.currentSong, state.isPlaying)
    }

    companion object {
        const val ACTION_PLAY_PAUSE = "com.example.sonorid.widget.ACTION_PLAY_PAUSE"
        const val ACTION_PREVIOUS = "com.example.sonorid.widget.ACTION_PREVIOUS"
        const val ACTION_NEXT = "com.example.sonorid.widget.ACTION_NEXT"
    }
}