// app/src/main/java/com/example/sonorid/di/PlaybackModule.kt
package com.example.sonorid.di

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlaybackModule {

    @Provides
    @Singleton
    fun provideExoPlayer(@ApplicationContext context: Context): ExoPlayer {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        return ExoPlayer.Builder(context)
            // handleAudioFocus = true: el player pide el audio focus al sistema
            // y reacciona a los cambios de ruta (altavoz <-> Bluetooth <-> auriculares).
            // Sin esto, el audio puede quedar "atascado" en la ruta anterior.
            .setAudioAttributes(audioAttributes, /* handleAudioFocus = */ true)
            // Pausa automáticamente si el dispositivo de audio se desconecta
            // (ej. te quitas los audífonos Bluetooth), en vez de sonar por el altavoz.
            .setHandleAudioBecomingNoisy(true)
            .build()
    }
}