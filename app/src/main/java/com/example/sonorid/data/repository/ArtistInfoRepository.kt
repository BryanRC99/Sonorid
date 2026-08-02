// app/src/main/java/com/example/sonorid/data/repository/ArtistInfoRepository.kt
package com.example.sonorid.data.repository

import com.example.sonorid.data.local.db.ArtistInfoDao
import com.example.sonorid.data.local.db.ArtistInfoEntity
import com.example.sonorid.data.remote.TheAudioDbApi
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

data class ArtistInfo(
    val imageUrl: String?,
    val genre: String?,
    val biography: String?,
    val bannerUrl: String?,
    val formedYear: String?,
    val country: String?,
    val style: String?
)

class RateLimitException : Exception("HTTP 429 Too Many Requests")

@Singleton
class ArtistInfoRepository @Inject constructor(
    private val dao: ArtistInfoDao,
    private val api: TheAudioDbApi
) {
    suspend fun getArtistInfo(artistName: String): ArtistInfo? {
        val cached = dao.get(artistName)
        if (cached != null) {
            return if (cached.found) {
                ArtistInfo(
                    imageUrl = cached.imageUrl,
                    genre = cached.genre,
                    biography = cached.biography,
                    bannerUrl = cached.bannerUrl,
                    formedYear = cached.formedYear,
                    country = cached.country,
                    style = cached.style
                )
            } else {
                null
            }
        }

        return try {
            val response = api.searchArtist(artistName)
            val match = response.artists?.firstOrNull()
            if (match != null) {
                val imageUrl = match.thumbUrl?.takeIf { it.isNotBlank() }
                    ?: match.fanartUrl?.takeIf { it.isNotBlank() }
                val biography = match.biographyEs?.takeIf { it.isNotBlank() }
                    ?: match.biographyEn?.takeIf { it.isNotBlank() }
                val bannerUrl = match.bannerUrl?.takeIf { it.isNotBlank() }
                val formedYear = match.formedYear?.takeIf { it.isNotBlank() && it != "0" }
                val country = match.country?.takeIf { it.isNotBlank() }
                val style = match.style?.takeIf { it.isNotBlank() }

                dao.upsert(
                    ArtistInfoEntity(
                        artistName = artistName,
                        imageUrl = imageUrl,
                        genre = match.genre,
                        biography = biography,
                        bannerUrl = bannerUrl,
                        formedYear = formedYear,
                        country = country,
                        style = style,
                        found = true
                    )
                )
                ArtistInfo(imageUrl, match.genre, biography, bannerUrl, formedYear, country, style)
            } else {
                dao.upsert(
                    ArtistInfoEntity(
                        artistName = artistName,
                        imageUrl = null,
                        genre = null,
                        biography = null,
                        bannerUrl = null,
                        formedYear = null,
                        country = null,
                        style = null,
                        found = false
                    )
                )
                null
            }
        } catch (e: HttpException) {
            if (e.code() == 429) throw RateLimitException()
            null
        } catch (e: Exception) {
            null
        }
    }
}