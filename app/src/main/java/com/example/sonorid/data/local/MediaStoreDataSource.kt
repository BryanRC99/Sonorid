// app/src/main/java/com/example/sonorid/data/local/MediaStoreDataSource.kt
package com.example.sonorid.data.local

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.sonorid.domain.model.MusicFolder
import com.example.sonorid.domain.model.Song
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaStoreDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val pathColumn: String
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.RELATIVE_PATH
        } else {
            MediaStore.Audio.Media.DATA
        }

    /** Convierte lo que venga en pathColumn a un path de carpeta normalizado tipo "Music/Sub/" */
    private fun normalizeFolderPath(raw: String): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            raw // RELATIVE_PATH ya viene como "Music/Sub/"
        } else {
            val parent = File(raw).parent ?: return raw
            val trimmed = parent.substringAfter("/storage/emulated/0/", parent)
            if (trimmed.endsWith("/")) trimmed else "$trimmed/"
        }
    }

    suspend fun getAllFolders(): List<MusicFolder> = withContext(Dispatchers.IO) {
        val counts = mutableMapOf<String, Int>()
        val projection = arrayOf(pathColumn)
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection, selection, null, null
        )?.use { cursor ->
            val colIndex = cursor.getColumnIndexOrThrow(pathColumn)
            while (cursor.moveToNext()) {
                val raw = cursor.getString(colIndex) ?: continue
                val folderPath = normalizeFolderPath(raw)
                counts[folderPath] = (counts[folderPath] ?: 0) + 1
            }
        }

        counts.map { (path, count) ->
            MusicFolder(
                path = path,
                displayName = path.trimEnd('/').substringAfterLast('/').ifEmpty { path },
                songCount = count
            )
        }.sortedBy { it.displayName.lowercase() }
    }

    suspend fun getAllSongs(selectedFolders: Set<String>): List<Song> = withContext(Dispatchers.IO) {
        val songs = mutableListOf<Song>()

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.TRACK,
            pathColumn
        )

        var selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        var selectionArgs: Array<String>? = null

        // En API 29+ podemos filtrar directo en la query con RELATIVE_PATH.
        if (selectedFolders.isNotEmpty() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val placeholders = selectedFolders.joinToString(" OR ") { "$pathColumn = ?" }
            selection += " AND ($placeholders)"
            selectionArgs = selectedFolders.toTypedArray()
        }

        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection, selection, selectionArgs, sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val trackColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
            val pathColIndex = cursor.getColumnIndexOrThrow(pathColumn)

            while (cursor.moveToNext()) {
                // Filtro manual para API < 29 (RELATIVE_PATH no existe ahí)
                if (selectedFolders.isNotEmpty() && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    val raw = cursor.getString(pathColIndex) ?: continue
                    val folderPath = normalizeFolderPath(raw)
                    if (folderPath !in selectedFolders) continue
                }

                val id = cursor.getLong(idColumn)
                val albumId = cursor.getLong(albumIdColumn)

                val songUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                val albumArtUri = ContentUris.withAppendedId(
                    Uri.parse("content://media/external/audio/albumart"), albumId
                )

                songs += Song(
                    id = id,
                    title = cursor.getString(titleColumn) ?: "Desconocido",
                    artist = cursor.getString(artistColumn) ?: "Artista desconocido",
                    album = cursor.getString(albumColumn) ?: "Álbum desconocido",
                    albumId = albumId,
                    duration = cursor.getLong(durationColumn),
                    uri = songUri,
                    albumArtUri = albumArtUri,
                    trackNumber = cursor.getInt(trackColumn)
                )
            }
        }

        songs
    }
}