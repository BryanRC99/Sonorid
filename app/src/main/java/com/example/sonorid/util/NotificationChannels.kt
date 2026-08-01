package com.example.sonorid.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannels {
    const val LYRICS_DOWNLOAD_CHANNEL_ID = "lyrics_download"

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return

        val channel = NotificationChannel(
            LYRICS_DOWNLOAD_CHANNEL_ID,
            "Descarga de letras",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Progreso y resultado de la descarga masiva de letras"
        }
        manager.createNotificationChannel(channel)
    }
}