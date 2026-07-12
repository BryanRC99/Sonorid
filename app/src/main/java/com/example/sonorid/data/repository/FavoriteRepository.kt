// app/src/main/java/com/example/sonorid/data/repository/FavoritesRepository.kt
package com.example.sonorid.data.repository

import com.example.sonorid.data.local.db.FavoriteDao
import com.example.sonorid.data.local.db.FavoriteEntity
import com.example.sonorid.domain.model.Song
import com.example.sonorid.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoritesRepository @Inject constructor(
    private val favoriteDao: FavoriteDao,
    private val musicRepository: MusicRepository
) {
    fun getFavoritesFlow(): Flow<List<FavoriteEntity>> = favoriteDao.getFavorites()

    suspend fun toggleFavorite(songId: Long) {
        val isFav = favoriteDao.getFavorites().first().any { it.songId == songId }
        if (isFav) favoriteDao.removeFavorite(songId) else favoriteDao.addFavorite(FavoriteEntity(songId))
    }

    suspend fun getFavoriteSongs(): List<Song> {
        val favIds = favoriteDao.getFavorites().first().map { it.songId }.toSet()
        return musicRepository.getAllSongs().filter { it.id in favIds }
    }
}