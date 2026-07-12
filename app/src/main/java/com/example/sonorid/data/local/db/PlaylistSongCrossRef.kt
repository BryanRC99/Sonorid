// app/src/main/java/com/example/sonorid/data/local/db/PlaylistSongCrossRef.kt
package com.example.sonorid.data.local.db

import androidx.room.Entity

@Entity(tableName = "playlist_songs", primaryKeys = ["playlistId", "songId"])
data class PlaylistSongCrossRef(
    val playlistId: Long,
    val songId: Long,
    val position: Int,
    val addedAt: Long = System.currentTimeMillis()
)