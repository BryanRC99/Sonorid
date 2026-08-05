// app/src/main/java/com/example/sonorid/playback/PlaybackService.kt
package com.example.sonorid.playback

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import androidx.media3.session.MediaSessionService
import com.example.sonorid.MainActivity
import com.example.sonorid.R
import com.example.sonorid.widget.SonoridWidgetProvider
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@OptIn(UnstableApi::class)
@AndroidEntryPoint
class PlaybackService : MediaSessionService() {

    @Inject lateinit var player: ExoPlayer
    @Inject lateinit var audioEffectsController: com.example.sonorid.audiofx.AudioEffectsController // 🆕
    private var mediaSession: MediaSession? = null

    private val customLayout: List<CommandButton> by lazy {
        listOf(
            CommandButton.Builder(CommandButton.ICON_UNDEFINED)
                .setSessionCommand(SessionCommand(ACTION_TOGGLE_SHUFFLE, Bundle.EMPTY))
                .setDisplayName("Aleatorio")
                .setIconResId(R.drawable.ic_action_shuffle)
                .build(),
            CommandButton.Builder(CommandButton.ICON_UNDEFINED)
                .setSessionCommand(SessionCommand(ACTION_CYCLE_REPEAT, Bundle.EMPTY))
                .setDisplayName("Repetir")
                .setIconResId(R.drawable.ic_action_repeat)
                .build()
        )
    }

    override fun onCreate() {
        super.onCreate()

        // 🆕 Se dispara al tocar la portada/título en la notificación o en el
        // media player de la pantalla de bloqueo. Sin esto, el toque no tiene
        // ningún destino y por eso "no hacía nada" (comportamiento por
        // defecto de MediaSession sin sessionActivity configurado).
        val openAppIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                action = ACTION_OPEN_PLAYER
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        mediaSession = MediaSession.Builder(this, player)
            .setCallback(SonoridSessionCallback())
            .setSessionActivity(openAppIntent)
            .build()
    }

    /** Registra los comandos personalizados (aleatorio/repetir) como
     * disponibles para cualquier controlador que se conecte a la sesión
     * (notificación del sistema, pantalla de bloqueo, Android Auto, etc.)
     * y les asigna los botones extra que aparecerán junto a prev/play/next. */
    private inner class SonoridSessionCallback : MediaSession.Callback {
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            val defaultResult = super.onConnect(session, controller)
            val sessionCommands = defaultResult.availableSessionCommands.buildUpon()
                .add(SessionCommand(ACTION_TOGGLE_SHUFFLE, Bundle.EMPTY))
                .add(SessionCommand(ACTION_CYCLE_REPEAT, Bundle.EMPTY))
                .build()
            return MediaSession.ConnectionResult.accept(
                sessionCommands,
                defaultResult.availablePlayerCommands
            )
        }

        override fun onPostConnect(session: MediaSession, controller: MediaSession.ControllerInfo) {
            session.setCustomLayout(controller, customLayout)
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            when (customCommand.customAction) {
                ACTION_TOGGLE_SHUFFLE -> player.shuffleModeEnabled = !player.shuffleModeEnabled
                ACTION_CYCLE_REPEAT -> player.repeatMode = when (player.repeatMode) {
                    Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                    Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                    else -> Player.REPEAT_MODE_OFF
                }
            }
            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            SonoridWidgetProvider.ACTION_PLAY_PAUSE -> {
                if (player.isPlaying) player.pause() else player.play()
            }
            SonoridWidgetProvider.ACTION_NEXT -> player.seekToNext()
            SonoridWidgetProvider.ACTION_PREVIOUS -> player.seekToPrevious()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player ?: return
        if (!player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        audioEffectsController.release()
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    companion object {
        const val ACTION_OPEN_PLAYER = "com.example.sonorid.action.OPEN_PLAYER"
        private const val ACTION_TOGGLE_SHUFFLE = "com.example.sonorid.TOGGLE_SHUFFLE"
        private const val ACTION_CYCLE_REPEAT = "com.example.sonorid.CYCLE_REPEAT"
    }


}