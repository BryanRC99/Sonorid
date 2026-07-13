// app/src/main/java/com/example/sonorid/di/DatabaseModule.kt
package com.example.sonorid.di

import android.content.Context
import androidx.room.Room
import com.example.sonorid.data.local.db.ArtistInfoDao
import com.example.sonorid.data.local.db.FavoriteDao
import com.example.sonorid.data.local.db.PlaylistDao
import com.example.sonorid.data.local.db.SonoridDatabase
import com.example.sonorid.data.local.db.LyricsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SonoridDatabase =
        Room.databaseBuilder(context, SonoridDatabase::class.java, "sonorid.db")
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun providePlaylistDao(db: SonoridDatabase): PlaylistDao = db.playlistDao()

    @Provides
    fun provideFavoriteDao(db: SonoridDatabase): FavoriteDao = db.favoriteDao()

    @Provides
    fun provideArtistInfoDao(db: SonoridDatabase): ArtistInfoDao = db.artistInfoDao()

    @Provides
    fun provideLyricsDao(db: SonoridDatabase): LyricsDao = db.lyricsDao()
}