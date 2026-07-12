// app/src/main/java/com/example/sonorid/data/repository/ArtistInfoRepository.kt
package com.example.sonorid.data.repository

import com.example.sonorid.data.local.db.ArtistInfoDao
import com.example.sonorid.data.local.db.ArtistInfoEntity
import com.example.sonorid.data.remote.TheAudioDbApi
import javax.inject.Inject
import javax.inject.Singleton

data class ArtistInfo(
    val imageUrl: String?,
    val genre: String?,
    val biography: String?
)

@Singleton
class ArtistInfoRepository @Inject constructor(
    private val dao: ArtistInfoDao,
    private val api: TheAudioDbApi
) {
    suspend fun getArtistInfo(artistName: String): ArtistInfo? {
        val cached = dao.get(artistName)
        if (cached != null) {
            return if (cached.found) {
                ArtistInfo(cached.imageUrl, cached.genre, cached.biography)
            } else {
                null // ya se buscó antes y no existe en TheAudioDB
            }
        }

        return try {
            val response = api.searchArtist(artistName)
            val match = response.artists?.firstOrNull()
            if (match != null) {
                val imageUrl = match.thumbUrl?.takeIf { it.isNotBlank() }
                    ?: match.fanartUrl?.takeIf { it.isNotBlank() }
                dao.upsert(
                    ArtistInfoEntity(
                        artistName = artistName,
                        imageUrl = imageUrl,
                        genre = match.genre,
                        biography = match.biography,
                        found = true
                    )
                )
                ArtistInfo(imageUrl, match.genre, match.biography)
            } else {
                dao.upsert(
                    ArtistInfoEntity(
                        artistName = artistName,
                        imageUrl = null,
                        genre = null,
                        biography = null,
                        found = false
                    )
                )
                null
            }
        } catch (e: Exception) {
            null // error de red: no cachea, para reintentar la próxima vez
        }
    }
}