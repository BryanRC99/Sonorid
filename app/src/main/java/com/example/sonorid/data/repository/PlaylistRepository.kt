// app/src/main/java/com/example/sonorid/data/repository/PlaylistRepository.kt
package com.example.sonorid.data.repository

import com.example.sonorid.data.local.db.PlaylistDao
import com.example.sonorid.data.local.db.PlaylistEntity
import com.example.sonorid.data.local.db.PlaylistSongCrossRef
import com.example.sonorid.domain.model.Song
import com.example.sonorid.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistRepository @Inject constructor(
    private val playlistDao: PlaylistDao,
    private val musicRepository: MusicRepository
) {
    fun getPlaylists(): Flow<List<PlaylistEntity>> = playlistDao.getPlaylists()

    fun songCount(playlistId: Long): Flow<Int> = playlistDao.songCountForPlaylist(playlistId)

    suspend fun createPlaylist(name: String): Long = playlistDao.insertPlaylist(PlaylistEntity(name = name))

    suspend fun deletePlaylist(id: Long) = playlistDao.deletePlaylist(id)

    suspend fun renamePlaylist(id: Long, newName: String) = playlistDao.renamePlaylist(id, newName)

    suspend fun addSong(playlistId: Long, songId: Long) {
        val position = playlistDao.nextPosition(playlistId)
        playlistDao.addSongToPlaylist(PlaylistSongCrossRef(playlistId, songId, position))
    }

    suspend fun removeSong(playlistId: Long, songId: Long) =
        playlistDao.removeSongFromPlaylist(playlistId, songId)

    suspend fun playlistIdsContaining(songId: Long): List<Long> =
        playlistDao.playlistIdsContainingSong(songId)

    suspend fun getSongsForPlaylist(playlistId: Long): List<Song> {
        val refs = playlistDao.getSongRefsForPlaylist(playlistId)
        val allSongs = musicRepository.getAllSongs()
        val songsById = allSongs.associateBy { it.id }
        return refs.mapNotNull { songsById[it.songId] }
    }
}