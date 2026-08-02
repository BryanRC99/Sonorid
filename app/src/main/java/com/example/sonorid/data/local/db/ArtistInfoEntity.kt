// app/src/main/java/com/example/sonorid/data/local/db/ArtistInfoEntity.kt
package com.example.sonorid.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "artist_info")
data class ArtistInfoEntity(
    @PrimaryKey val artistName: String,
    val imageUrl: String?,
    val genre: String?,
    val biography: String?,
    val bannerUrl: String?,
    val formedYear: String?,
    val country: String?,
    val style: String?,
    val found: Boolean,
    val fetchedAt: Long = System.currentTimeMillis()
)