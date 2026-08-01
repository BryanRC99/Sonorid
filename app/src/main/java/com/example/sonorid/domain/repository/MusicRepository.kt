// domain/repository/MusicRepository.kt
package com.example.sonorid.domain.repository

import com.example.sonorid.domain.model.MusicFolder
import com.example.sonorid.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    suspend fun getAllSongs(): List<Song>
    suspend fun getAllFolders(): List<MusicFolder>
    val selectedFolders: Flow<Set<String>>
    suspend fun setSelectedFolders(folders: Set<String>)

    /** Fuerza a que la próxima llamada a [getAllSongs] vuelva a leer MediaStore,
     * usado tras editar o eliminar una canción desde el reproductor. */
    suspend fun invalidateCache()
}