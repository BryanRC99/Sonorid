// app/src/main/java/com/example/sonorid/data/repository/LyricsRepositoryImpl.kt
package com.example.sonorid.data.repository

import android.util.Log
import com.example.sonorid.data.local.db.LyricsCacheEntity
import com.example.sonorid.data.local.db.LyricsDao
import com.example.sonorid.data.remote.LrcLibApi
import com.example.sonorid.domain.model.Lyrics
import com.example.sonorid.domain.model.LyricLine
import com.example.sonorid.domain.model.Song
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LyricsRepositoryImpl @Inject constructor(
    private val api: LrcLibApi,
    private val lyricsDao: LyricsDao
) {
    suspend fun getLyrics(song: Song): Lyrics? {
        // 1. Revisa el caché local primero, evita golpear la API si ya la tenemos.
        lyricsDao.get(song.id)?.let { cached ->
            return cached.toLyricsOrNull()
        }

        // 2. No hay caché: consulta LRCLIB.
        return try {
            val response = api.getLyrics(
                trackName = song.title,
                artistName = song.artist,
                albumName = song.album,
                durationSeconds = (song.duration / 1000).toInt()
            )

            lyricsDao.upsert(
                LyricsCacheEntity(
                    songId = song.id,
                    syncedLyricsRaw = response.syncedLyrics,
                    plainLyrics = response.plainLyrics,
                    instrumental = response.instrumental,
                    found = true
                )
            )

            if (response.instrumental) {
                Lyrics(synced = emptyList(), plainText = "Instrumental")
            } else {
                Lyrics(
                    synced = response.syncedLyrics?.let { parseLrc(it) } ?: emptyList(),
                    plainText = response.plainLyrics
                )
            }
        } catch (e: HttpException) {
            // Loguea el código HTTP exacto para identificar bloqueos (403, 429, etc.)
            Log.e("LyricsRepo", "HTTP ${e.code()} para ${song.title}", e)

            if (e.code() == 404) {
                // LRCLIB confirma que esta canción no tiene letra: cachea para no reintentar.
                lyricsDao.upsert(
                    LyricsCacheEntity(
                        songId = song.id,
                        syncedLyricsRaw = null,
                        plainLyrics = null,
                        instrumental = false,
                        found = false
                    )
                )
            }
            null
        } catch (e: Exception) {
            // Loguea errores genéricos de red (UnknownHostException, SocketTimeoutException, etc.)
            Log.e("LyricsRepo", "Error de red para ${song.title}", e)
            null
        }
    }

    private fun LyricsCacheEntity.toLyricsOrNull(): Lyrics? = when {
        !found -> null
        instrumental -> Lyrics(synced = emptyList(), plainText = "Instrumental")
        else -> Lyrics(
            synced = syncedLyricsRaw?.let { parseLrc(it) } ?: emptyList(),
            plainText = plainLyrics
        )
    }

    private fun parseLrc(raw: String): List<LyricLine> {
        val regex = Regex("""\[(\d{2}):(\d{2})\.(\d{2,3})]\s*(.*)""")
        return raw.lines().mapNotNull { line ->
            val match = regex.find(line) ?: return@mapNotNull null
            val (min, sec, ms, text) = match.destructured
            val millis = min.toLong() * 60_000 + sec.toLong() * 1000 + ms.padEnd(3, '0').toLong()
            LyricLine(millis, text.trim())
        }
    }
}