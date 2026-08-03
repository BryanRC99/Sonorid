// app/src/main/java/com/example/sonorid/data/repository/ArtistInfoRepository.kt
package com.example.sonorid.data.repository

import com.example.sonorid.data.local.db.ArtistInfoDao
import com.example.sonorid.data.local.db.ArtistInfoEntity
import com.example.sonorid.data.remote.FanartTvApi
import com.example.sonorid.data.remote.MusicBrainzApi
import com.example.sonorid.data.remote.TheAudioDbApi
import com.example.sonorid.di.FanartTvApiKey
import com.example.sonorid.di.MusicBrainzRateLimiter
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

private const val IMAGE_SOURCE_FANART = "fanart"
private const val IMAGE_SOURCE_AUDIODB = "theaudiodb"

/**
 * Cadena de resolución:
 * 1. Texto (biografía, género, país, año, estilo) → siempre TheAudioDB.
 * 2. Imagen → MusicBrainz resuelve el MBID → Fanart.tv trae la imagen.
 *    Si Fanart.tv no tiene nada (o no se pudo resolver el MBID) → TheAudioDB
 *    como respaldo de imagen.
 */
@Singleton
class ArtistInfoRepository @Inject constructor(
    private val dao: ArtistInfoDao,
    private val theAudioDbApi: TheAudioDbApi,
    private val musicBrainzApi: MusicBrainzApi,
    private val fanartTvApi: FanartTvApi,
    private val musicBrainzRateLimiter: MusicBrainzRateLimiter,
    @FanartTvApiKey private val fanartTvApiKey: String
) {
    suspend fun getArtistInfo(artistName: String): ArtistInfo? {
        val cached = dao.get(artistName)
        if (cached != null) {
            return cached.toArtistInfoOrNull()
        }

        val audioDbInfo = fetchTheAudioDbInfo(artistName)
        val fanartImage = fetchFanartImage(artistName)

        val resolvedImage = fanartImage?.imageUrl ?: audioDbInfo?.imageUrl
        val resolvedBanner = fanartImage?.bannerUrl ?: audioDbInfo?.bannerUrl
        val imageSource = when {
            fanartImage?.imageUrl != null -> IMAGE_SOURCE_FANART
            audioDbInfo?.imageUrl != null -> IMAGE_SOURCE_AUDIODB
            else -> null
        }

        // Nada útil encontrado en ninguna fuente: cachea como "no encontrado"
        // para no reintentar en la próxima carga.
        if (resolvedImage == null && audioDbInfo == null) {
            dao.upsert(
                ArtistInfoEntity(
                    artistName = artistName,
                    mbid = fanartImage?.mbid,
                    imageUrl = null,
                    imageSource = null,
                    genre = null,
                    biography = null,
                    bannerUrl = null,
                    formedYear = null,
                    country = null,
                    style = null,
                    found = false
                )
            )
            return null
        }

        val entity = ArtistInfoEntity(
            artistName = artistName,
            mbid = fanartImage?.mbid,
            imageUrl = resolvedImage,
            imageSource = imageSource,
            genre = audioDbInfo?.genre,
            biography = audioDbInfo?.biography,
            bannerUrl = resolvedBanner,
            formedYear = audioDbInfo?.formedYear,
            country = audioDbInfo?.country,
            style = audioDbInfo?.style,
            found = true
        )
        dao.upsert(entity)
        return entity.toArtistInfoOrNull()
    }

    private data class AudioDbInfo(
        val imageUrl: String?,
        val genre: String?,
        val biography: String?,
        val bannerUrl: String?,
        val formedYear: String?,
        val country: String?,
        val style: String?
    )

    private suspend fun fetchTheAudioDbInfo(artistName: String): AudioDbInfo? {
        return try {
            val response = theAudioDbApi.searchArtist(artistName)
            val match = response.artists?.firstOrNull() ?: return null
            val imageUrl = match.thumbUrl?.takeIf { it.isNotBlank() }
                ?: match.fanartUrl?.takeIf { it.isNotBlank() }
            val biography = match.biographyEs?.takeIf { it.isNotBlank() }
                ?: match.biographyEn?.takeIf { it.isNotBlank() }
            AudioDbInfo(
                imageUrl = imageUrl,
                genre = match.genre,
                biography = biography,
                bannerUrl = match.bannerUrl?.takeIf { it.isNotBlank() },
                formedYear = match.formedYear?.takeIf { it.isNotBlank() && it != "0" },
                country = match.country?.takeIf { it.isNotBlank() },
                style = match.style?.takeIf { it.isNotBlank() }
            )
        } catch (e: HttpException) {
            if (e.code() == 429) throw RateLimitException()
            null
        } catch (e: Exception) {
            null
        }
    }

    private data class FanartImageResult(
        val mbid: String?,
        val imageUrl: String?,
        val bannerUrl: String?
    )

    private suspend fun fetchFanartImage(artistName: String): FanartImageResult? {
        if (fanartTvApiKey.isBlank()) return null // sin key configurada, salta directo a TheAudioDB

        val mbid = resolveMbid(artistName) ?: return null

        return try {
            val response = fanartTvApi.getArtistImages(mbid, fanartTvApiKey)
            FanartImageResult(
                mbid = mbid,
                imageUrl = response.artistthumb?.firstOrNull()?.url,
                bannerUrl = response.musicbanner?.firstOrNull()?.url
            )
        } catch (e: Exception) {
            // 404 = Fanart.tv no tiene arte para este MBID; no es un error real
            FanartImageResult(mbid = mbid, imageUrl = null, bannerUrl = null)
        }
    }

    private suspend fun resolveMbid(artistName: String): String? {
        return try {
            val response = musicBrainzRateLimiter.throttled {
                musicBrainzApi.searchArtist(query = artistName)
            }
            response.artists
                .filter { !it.name.isNullOrBlank() }
                .maxByOrNull { it.score ?: 0 }
                ?.id
        } catch (e: Exception) {
            null
        }
    }
}

    private fun ArtistInfoEntity.toArtistInfoOrNull(): ArtistInfo? {
        if (!found) return null
        return ArtistInfo(
            imageUrl = imageUrl,
            genre = genre,
            biography = biography,
            bannerUrl = bannerUrl,
            formedYear = formedYear,
            country = country,
            style = style
        )
    }