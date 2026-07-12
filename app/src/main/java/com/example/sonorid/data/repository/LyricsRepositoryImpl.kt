// app/src/main/java/com/example/sonorid/data/repository/LyricsRepositoryImpl.kt
package com.example.sonorid.data.repository

import com.example.sonorid.data.remote.LrcLibApi
import com.example.sonorid.domain.model.Lyrics
import com.example.sonorid.domain.model.LyricLine
import com.example.sonorid.domain.model.Song
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LyricsRepositoryImpl @Inject constructor(
    private val api: LrcLibApi
) {
    suspend fun getLyrics(song: Song): Lyrics? = try {
        val response = api.getLyrics(
            trackName = song.title,
            artistName = song.artist,
            albumName = song.album,
            durationSeconds = (song.duration / 1000).toInt()
        )
        if (response.instrumental) {
            Lyrics(synced = emptyList(), plainText = "Instrumental")
        } else {
            Lyrics(
                synced = response.syncedLyrics?.let { parseLrc(it) } ?: emptyList(),
                plainText = response.plainLyrics
            )
        }
    } catch (e: Exception) {
        null
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