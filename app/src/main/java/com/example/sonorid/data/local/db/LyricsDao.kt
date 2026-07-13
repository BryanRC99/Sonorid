// app/src/main/java/com/example/sonorid/data/local/db/LyricsDao.kt
package com.example.sonorid.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LyricsDao {
    @Query("SELECT * FROM lyrics_cache WHERE songId = :songId")
    suspend fun get(songId: Long): LyricsCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: LyricsCacheEntity)
}