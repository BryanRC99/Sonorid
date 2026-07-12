package com.example.sonorid.data.repository

import com.example.sonorid.data.local.MediaStoreDataSource
import com.example.sonorid.domain.model.Song
import com.example.sonorid.domain.repository.MusicRepository
import javax.inject.Inject

class MusicRepositoryImpl @Inject constructor(
    private val dataSource: MediaStoreDataSource
) : MusicRepository {
    override suspend fun getAllSongs(): List<Song> = dataSource.getAllSongs()
}