// app/src/main/java/com/example/sonorid/data/local/db/FavoriteEntity.kt
package com.example.sonorid.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val songId: Long,
    val addedAt: Long = System.currentTimeMillis()
)