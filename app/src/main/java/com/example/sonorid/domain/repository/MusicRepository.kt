package com.example.sonorid.domain.repository

import com.example.sonorid.domain.model.Song

interface MusicRepository {
    suspend fun getAllSongs(): List<Song>
}