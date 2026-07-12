// app/src/main/java/com/example/sonorid/data/local/db/PlaylistEntity.kt
package com.example.sonorid.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)