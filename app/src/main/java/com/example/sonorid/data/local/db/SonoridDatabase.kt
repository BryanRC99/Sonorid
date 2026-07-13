// app/src/main/java/com/example/sonorid/data/local/db/SonoridDatabase.kt
package com.example.sonorid.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        PlaylistEntity::class,
        PlaylistSongCrossRef::class,
        FavoriteEntity::class,
        ArtistInfoEntity::class,
        LyricsCacheEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class SonoridDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun artistInfoDao(): ArtistInfoDao
    abstract fun lyricsDao(): LyricsDao
}