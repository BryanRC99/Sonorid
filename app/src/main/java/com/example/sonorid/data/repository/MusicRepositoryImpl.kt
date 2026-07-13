// app/src/main/java/com/example/sonorid/data/repository/MusicRepositoryImpl.kt
package com.example.sonorid.data.repository

import com.example.sonorid.data.local.MediaStoreDataSource
import com.example.sonorid.data.local.UserPreferencesRepository
import com.example.sonorid.domain.model.MusicFolder
import com.example.sonorid.domain.model.Song
import com.example.sonorid.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepositoryImpl @Inject constructor(
    private val dataSource: MediaStoreDataSource,
    private val userPrefs: UserPreferencesRepository
) : MusicRepository {

    private val cacheMutex = Mutex()
    private var cachedSongs: List<Song>? = null
    private var cachedForFolders: Set<String>? = null

    override suspend fun getAllSongs(): List<Song> {
        val selected = userPrefs.selectedFolders.first()

        cacheMutex.withLock {
            val cached = cachedSongs
            if (cached != null && cachedForFolders == selected) {
                return cached // 👈 evita re-escanear MediaStore si nada cambió
            }
        }

        val fresh = dataSource.getAllSongs(selected)

        cacheMutex.withLock {
            cachedSongs = fresh
            cachedForFolders = selected
        }

        return fresh
    }

    override suspend fun getAllFolders(): List<MusicFolder> = dataSource.getAllFolders()

    override val selectedFolders: Flow<Set<String>> = userPrefs.selectedFolders

    override suspend fun setSelectedFolders(folders: Set<String>) {
        userPrefs.setSelectedFolders(folders)
        cacheMutex.withLock {
            cachedSongs = null // 👈 invalida el caché: cambió qué carpetas se incluyen
            cachedForFolders = null
        }
    }
}