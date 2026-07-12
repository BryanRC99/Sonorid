// data/repository/MusicRepositoryImpl.kt
package com.example.sonorid.data.repository

import com.example.sonorid.data.local.MediaStoreDataSource
import com.example.sonorid.data.local.UserPreferencesRepository
import com.example.sonorid.domain.model.MusicFolder
import com.example.sonorid.domain.model.Song
import com.example.sonorid.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class MusicRepositoryImpl @Inject constructor(
    private val dataSource: MediaStoreDataSource,
    private val userPrefs: UserPreferencesRepository
) : MusicRepository {

    override suspend fun getAllSongs(): List<Song> {
        val selected = userPrefs.selectedFolders.first()
        return dataSource.getAllSongs(selected)
    }

    override suspend fun getAllFolders(): List<MusicFolder> = dataSource.getAllFolders()

    override val selectedFolders: Flow<Set<String>> = userPrefs.selectedFolders

    override suspend fun setSelectedFolders(folders: Set<String>) {
        userPrefs.setSelectedFolders(folders)
    }
}