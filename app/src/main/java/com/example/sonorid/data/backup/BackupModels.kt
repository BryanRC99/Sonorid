package com.example.sonorid.data.backup

import kotlinx.serialization.Serializable

/** Referencia liviana a una canción dentro del backup: se guarda el ID de
 * MediaStore (rápido si el dispositivo es el mismo) y también título/artista/
 * álbum, usados como respaldo para volver a encontrar la canción si el ID
 * cambió (ej. reinstalación, otro dispositivo, MediaStore reindexado). */
@Serializable
data class BackupSongRef(
    val songId: Long,
    val title: String,
    val artist: String,
    val album: String
)

@Serializable
data class BackupPlaylist(
    val name: String,
    val songs: List<BackupSongRef>
)

@Serializable
data class SonoridBackup(
    val formatVersion: Int = 1,
    val exportedAt: Long,
    val favorites: List<BackupSongRef>,
    val playlists: List<BackupPlaylist>
)