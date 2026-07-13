// app/src/main/java/com/example/sonorid/data/local/db/LyricsCacheEntity.kt
package com.example.sonorid.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lyrics_cache")
data class LyricsCacheEntity(
    @PrimaryKey val songId: Long,
    val syncedLyricsRaw: String?, // texto LRC crudo, se parsea al leer
    val plainLyrics: String?,
    val instrumental: Boolean,
    val found: Boolean, // false = ya se buscó y no existe en LRCLIB, evita reintentos
    val fetchedAt: Long = System.currentTimeMillis()
)