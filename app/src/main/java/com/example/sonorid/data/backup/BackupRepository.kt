package com.example.sonorid.data.backup

import android.content.Context
import android.net.Uri
import com.example.sonorid.data.local.db.FavoriteDao
import com.example.sonorid.data.local.db.FavoriteEntity
import com.example.sonorid.data.local.db.PlaylistDao
import com.example.sonorid.data.local.db.PlaylistEntity
import com.example.sonorid.data.local.db.PlaylistSongCrossRef
import com.example.sonorid.domain.repository.MusicRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val playlistDao: PlaylistDao,
    private val favoriteDao: FavoriteDao,
    private val musicRepository: MusicRepository
) {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    data class ImportSummary(
        val favoritesRestored: Int,
        val favoritesSkipped: Int,
        val playlistsRestored: Int,
        val songsSkipped: Int
    )

    suspend fun exportBackup(destination: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val songsById = musicRepository.getAllSongs().associateBy { it.id }

            val favoriteRefs = favoriteDao.getFavorites().first().mapNotNull { fav ->
                songsById[fav.songId]?.let { song ->
                    BackupSongRef(song.id, song.title, song.artist, song.album)
                }
            }

            val playlists = playlistDao.getPlaylists().first()
            val playlistBackups = playlists.map { playlist ->
                val refs = playlistDao.getSongRefsForPlaylist(playlist.id)
                val songs = refs.mapNotNull { ref -> songsById[ref.songId] }
                    .map { song -> BackupSongRef(song.id, song.title, song.artist, song.album) }
                BackupPlaylist(name = playlist.name, songs = songs)
            }

            val backup = SonoridBackup(
                exportedAt = System.currentTimeMillis(),
                favorites = favoriteRefs,
                playlists = playlistBackups
            )

            val outputStream = context.contentResolver.openOutputStream(destination)
                ?: return@withContext Result.failure(Exception("No se pudo abrir el archivo de destino"))

            outputStream.use { it.write(json.encodeToString(backup).toByteArray()) }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importBackup(source: Uri): Result<ImportSummary> = withContext(Dispatchers.IO) {
        try {
            val text = context.contentResolver.openInputStream(source)?.use { it.readBytes().decodeToString() }
                ?: return@withContext Result.failure(Exception("No se pudo leer el archivo seleccionado"))

            val backup = json.decodeFromString<SonoridBackup>(text)

            val allSongs = musicRepository.getAllSongs()
            val byId = allSongs.associateBy { it.id }
            val byMetadata = allSongs.associateBy { Triple(it.title, it.artist, it.album) }

            // Primero intenta por ID (mismo dispositivo, más rápido y exacto);
            // si no existe, cae a coincidencia por título+artista+álbum (útil
            // al restaurar en otro dispositivo o tras reindexar MediaStore).
            fun resolve(ref: BackupSongRef): Long? =
                byId[ref.songId]?.id ?: byMetadata[Triple(ref.title, ref.artist, ref.album)]?.id

            var favRestored = 0
            var favSkipped = 0
            for (ref in backup.favorites) {
                val songId = resolve(ref)
                if (songId != null) {
                    favoriteDao.addFavorite(FavoriteEntity(songId))
                    favRestored++
                } else {
                    favSkipped++
                }
            }

            var playlistsRestored = 0
            var songsSkipped = 0
            for (playlistBackup in backup.playlists) {
                val newPlaylistId = playlistDao.insertPlaylist(PlaylistEntity(name = playlistBackup.name))
                var position = 0
                for (ref in playlistBackup.songs) {
                    val songId = resolve(ref)
                    if (songId != null) {
                        playlistDao.addSongToPlaylist(PlaylistSongCrossRef(newPlaylistId, songId, position))
                        position++
                    } else {
                        songsSkipped++
                    }
                }
                playlistsRestored++
            }

            Result.success(ImportSummary(favRestored, favSkipped, playlistsRestored, songsSkipped))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}