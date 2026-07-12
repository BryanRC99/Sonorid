// app/src/main/java/com/example/sonorid/data/local/db/ArtistInfoDao.kt
package com.example.sonorid.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ArtistInfoDao {
    @Query("SELECT * FROM artist_info WHERE artistName = :artistName")
    suspend fun get(artistName: String): ArtistInfoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ArtistInfoEntity)
}